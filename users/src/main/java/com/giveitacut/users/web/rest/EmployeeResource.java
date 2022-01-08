package com.giveitacut.users.web.rest;

import com.giveitacut.users.domain.Employee;
import com.giveitacut.users.repository.EmployeeRepository;
import com.giveitacut.users.repository.UserRepository;
import com.giveitacut.users.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.giveitacut.users.domain.Employee}.
 */
@RestController
@RequestMapping("/api")
public class EmployeeResource {

    private final Logger log = LoggerFactory.getLogger(EmployeeResource.class);

    private static final String ENTITY_NAME = "usersEmployee";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EmployeeRepository employeeRepository;

    private final UserRepository userRepository;

    public EmployeeResource(EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@code POST  /employees} : Create a new employee.
     *
     * @param employee the employee to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new employee, or with status {@code 400 (Bad Request)} if the employee has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/employees")
    public Mono<ResponseEntity<Employee>> createEmployee(@Valid @RequestBody Employee employee) throws URISyntaxException {
        log.debug("REST request to save Employee : {}", employee);
        if (employee.getId() != null) {
            throw new BadRequestAlertException("A new employee cannot already have an ID", ENTITY_NAME, "idexists");
        }

        if (employee.getUser() != null) {
            // Save user in case it's new and only exists in gateway
            userRepository.save(employee.getUser());
        }

        return employeeRepository
            .save(employee)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/employees/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /employees/:id} : Updates an existing employee.
     *
     * @param id the id of the employee to save.
     * @param employee the employee to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated employee,
     * or with status {@code 400 (Bad Request)} if the employee is not valid,
     * or with status {@code 500 (Internal Server Error)} if the employee couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/employees/{id}")
    public Mono<ResponseEntity<Employee>> updateEmployee(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Employee employee
    ) throws URISyntaxException {
        log.debug("REST request to update Employee : {}, {}", id, employee);
        if (employee.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, employee.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return employeeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                if (employee.getUser() != null) {
                    // Save user in case it's new and only exists in gateway
                    userRepository.save(employee.getUser());
                }

                return employeeRepository
                    .save(employee)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /employees/:id} : Partial updates given fields of an existing employee, field will ignore if it is null
     *
     * @param id the id of the employee to save.
     * @param employee the employee to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated employee,
     * or with status {@code 400 (Bad Request)} if the employee is not valid,
     * or with status {@code 404 (Not Found)} if the employee is not found,
     * or with status {@code 500 (Internal Server Error)} if the employee couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/employees/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<Employee>> partialUpdateEmployee(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Employee employee
    ) throws URISyntaxException {
        log.debug("REST request to partial update Employee partially : {}, {}", id, employee);
        if (employee.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, employee.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return employeeRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                if (employee.getUser() != null) {
                    // Save user in case it's new and only exists in gateway
                    userRepository.save(employee.getUser());
                }

                Mono<Employee> result = employeeRepository
                    .findById(employee.getId())
                    .map(existingEmployee -> {
                        if (employee.getName() != null) {
                            existingEmployee.setName(employee.getName());
                        }
                        if (employee.getSurname() != null) {
                            existingEmployee.setSurname(employee.getSurname());
                        }
                        if (employee.getPhone() != null) {
                            existingEmployee.setPhone(employee.getPhone());
                        }

                        return existingEmployee;
                    })
                    .flatMap(employeeRepository::save);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /employees} : get all the employees.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of employees in body.
     */
    @GetMapping("/employees")
    public Mono<ResponseEntity<List<Employee>>> getAllEmployees(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of Employees");
        return employeeRepository
            .count()
            .zipWith(employeeRepository.findAllBy(pageable).collectList())
            .map(countWithEntities -> {
                return ResponseEntity
                    .ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            UriComponentsBuilder.fromHttpRequest(request),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2());
            });
    }

    /**
     * {@code GET  /employees/:id} : get the "id" employee.
     *
     * @param id the id of the employee to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the employee, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/employees/{id}")
    public Mono<ResponseEntity<Employee>> getEmployee(@PathVariable Long id) {
        log.debug("REST request to get Employee : {}", id);
        Mono<Employee> employee = employeeRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(employee);
    }

    /**
     * {@code DELETE  /employees/:id} : delete the "id" employee.
     *
     * @param id the id of the employee to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/employees/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteEmployee(@PathVariable Long id) {
        log.debug("REST request to delete Employee : {}", id);
        return employeeRepository
            .deleteById(id)
            .map(result ->
                ResponseEntity
                    .noContent()
                    .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                    .build()
            );
    }
}
