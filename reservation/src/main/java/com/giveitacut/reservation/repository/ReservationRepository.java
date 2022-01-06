package com.giveitacut.reservation.repository;

import com.giveitacut.reservation.domain.Reservation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Reservation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ReservationRepository extends R2dbcRepository<Reservation, Long>, ReservationRepositoryInternal {
    Flux<Reservation> findAllBy(Pageable pageable);

    @Override
    Mono<Reservation> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Reservation> findAllWithEagerRelationships();

    @Override
    Flux<Reservation> findAllWithEagerRelationships(Pageable page);

    @Override
    Mono<Void> deleteById(Long id);

    @Query(
        "SELECT entity.* FROM reservation entity JOIN rel_reservation__service joinTable ON entity.id = joinTable.reservation_id WHERE joinTable.service_id = :id"
    )
    Flux<Reservation> findByService(Long id);

    // just to avoid having unambigous methods
    @Override
    Flux<Reservation> findAll();

    @Override
    Mono<Reservation> findById(Long id);

    @Override
    <S extends Reservation> Mono<S> save(S entity);
}

interface ReservationRepositoryInternal {
    <S extends Reservation> Mono<S> insert(S entity);
    <S extends Reservation> Mono<S> save(S entity);
    Mono<Integer> update(Reservation entity);

    Flux<Reservation> findAll();
    Mono<Reservation> findById(Long id);
    Flux<Reservation> findAllBy(Pageable pageable);
    Flux<Reservation> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Reservation> findOneWithEagerRelationships(Long id);

    Flux<Reservation> findAllWithEagerRelationships();

    Flux<Reservation> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
