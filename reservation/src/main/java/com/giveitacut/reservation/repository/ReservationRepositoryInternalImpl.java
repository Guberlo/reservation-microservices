package com.giveitacut.reservation.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import com.giveitacut.reservation.domain.Reservation;
import com.giveitacut.reservation.domain.Service;
import com.giveitacut.reservation.repository.rowmapper.ReservationRowMapper;
import com.giveitacut.reservation.service.EntityManager;
import com.giveitacut.reservation.service.EntityManager.LinkTable;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive custom repository implementation for the Reservation entity.
 */
@SuppressWarnings("unused")
class ReservationRepositoryInternalImpl implements ReservationRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final ReservationRowMapper reservationMapper;

    private static final Table entityTable = Table.aliased("reservation", EntityManager.ENTITY_ALIAS);

    private static final EntityManager.LinkTable serviceLink = new LinkTable("rel_reservation__service", "reservation_id", "service_id");

    public ReservationRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        ReservationRowMapper reservationMapper
    ) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public Flux<Reservation> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<Reservation> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Reservation> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = ReservationSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);

        String select = entityManager.createSelect(selectFrom, Reservation.class, pageable, criteria);
        String alias = entityTable.getReferenceName().getReference();
        String selectWhere = Optional
            .ofNullable(criteria)
            .map(crit ->
                new StringBuilder(select)
                    .append(" ")
                    .append("WHERE")
                    .append(" ")
                    .append(alias)
                    .append(".")
                    .append(crit.toString())
                    .toString()
            )
            .orElse(select); // TODO remove once https://github.com/spring-projects/spring-data-jdbc/issues/907 will be fixed
        return db.sql(selectWhere).map(this::process);
    }

    @Override
    public Flux<Reservation> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<Reservation> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    @Override
    public Mono<Reservation> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Reservation> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Reservation> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Reservation process(Row row, RowMetadata metadata) {
        Reservation entity = reservationMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends Reservation> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends Reservation> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity).flatMap(savedEntity -> updateRelations(savedEntity));
        } else {
            return update(entity)
                .map(numberOfUpdates -> {
                    if (numberOfUpdates.intValue() <= 0) {
                        throw new IllegalStateException("Unable to update Reservation with id = " + entity.getId());
                    }
                    return entity;
                })
                .then(updateRelations(entity));
        }
    }

    @Override
    public Mono<Integer> update(Reservation entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }

    @Override
    public Mono<Void> deleteById(Long entityId) {
        return deleteRelations(entityId)
            .then(r2dbcEntityTemplate.delete(Reservation.class).matching(query(where("id").is(entityId))).all().then());
    }

    protected <S extends Reservation> Mono<S> updateRelations(S entity) {
        Mono<Void> result = entityManager
            .updateLinkTable(serviceLink, entity.getId(), entity.getServices().stream().map(Service::getId))
            .then();
        return result.thenReturn(entity);
    }

    protected Mono<Void> deleteRelations(Long entityId) {
        return entityManager.deleteFromLinkTable(serviceLink, entityId);
    }
}
