package com.giveitacut.reservation.repository;

import com.giveitacut.reservation.domain.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Service entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ServiceRepository extends R2dbcRepository<Service, Long>, ServiceRepositoryInternal {
    Flux<Service> findAllBy(Pageable pageable);

    // just to avoid having unambigous methods
    @Override
    Flux<Service> findAll();

    @Override
    Mono<Service> findById(Long id);

    @Override
    <S extends Service> Mono<S> save(S entity);
}

interface ServiceRepositoryInternal {
    <S extends Service> Mono<S> insert(S entity);
    <S extends Service> Mono<S> save(S entity);
    Mono<Integer> update(Service entity);

    Flux<Service> findAll();
    Mono<Service> findById(Long id);
    Flux<Service> findAllBy(Pageable pageable);
    Flux<Service> findAllBy(Pageable pageable, Criteria criteria);
}
