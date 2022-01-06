package com.giveitacut.reservation.web.rest;

import static com.giveitacut.reservation.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.giveitacut.reservation.IntegrationTest;
import com.giveitacut.reservation.domain.Reservation;
import com.giveitacut.reservation.repository.ReservationRepository;
import com.giveitacut.reservation.service.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link ReservationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class ReservationResourceIT {

    private static final LocalDate DEFAULT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final ZonedDateTime DEFAULT_START_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_START_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_END_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_END_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/reservations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationRepository reservationRepositoryMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Reservation reservation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reservation createEntity(EntityManager em) {
        Reservation reservation = new Reservation().date(DEFAULT_DATE).startTime(DEFAULT_START_TIME).endTime(DEFAULT_END_TIME);
        return reservation;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reservation createUpdatedEntity(EntityManager em) {
        Reservation reservation = new Reservation().date(UPDATED_DATE).startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME);
        return reservation;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll("rel_reservation__service").block();
            em.deleteAll(Reservation.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        reservation = createEntity(em);
    }

    @Test
    void createReservation() throws Exception {
        int databaseSizeBeforeCreate = reservationRepository.findAll().collectList().block().size();
        // Create the Reservation
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeCreate + 1);
        Reservation testReservation = reservationList.get(reservationList.size() - 1);
        assertThat(testReservation.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testReservation.getStartTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(testReservation.getEndTime()).isEqualTo(DEFAULT_END_TIME);
    }

    @Test
    void createReservationWithExistingId() throws Exception {
        // Create the Reservation with an existing ID
        reservation.setId(1L);

        int databaseSizeBeforeCreate = reservationRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = reservationRepository.findAll().collectList().block().size();
        // set the field null
        reservation.setDate(null);

        // Create the Reservation, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkStartTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = reservationRepository.findAll().collectList().block().size();
        // set the field null
        reservation.setStartTime(null);

        // Create the Reservation, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkEndTimeIsRequired() throws Exception {
        int databaseSizeBeforeTest = reservationRepository.findAll().collectList().block().size();
        // set the field null
        reservation.setEndTime(null);

        // Create the Reservation, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllReservations() {
        // Initialize the database
        reservationRepository.save(reservation).block();

        // Get all the reservationList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(reservation.getId().intValue()))
            .jsonPath("$.[*].date")
            .value(hasItem(DEFAULT_DATE.toString()))
            .jsonPath("$.[*].startTime")
            .value(hasItem(sameInstant(DEFAULT_START_TIME)))
            .jsonPath("$.[*].endTime")
            .value(hasItem(sameInstant(DEFAULT_END_TIME)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReservationsWithEagerRelationshipsIsEnabled() {
        when(reservationRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(reservationRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReservationsWithEagerRelationshipsIsNotEnabled() {
        when(reservationRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(reservationRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getReservation() {
        // Initialize the database
        reservationRepository.save(reservation).block();

        // Get the reservation
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, reservation.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(reservation.getId().intValue()))
            .jsonPath("$.date")
            .value(is(DEFAULT_DATE.toString()))
            .jsonPath("$.startTime")
            .value(is(sameInstant(DEFAULT_START_TIME)))
            .jsonPath("$.endTime")
            .value(is(sameInstant(DEFAULT_END_TIME)));
    }

    @Test
    void getNonExistingReservation() {
        // Get the reservation
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewReservation() throws Exception {
        // Initialize the database
        reservationRepository.save(reservation).block();

        int databaseSizeBeforeUpdate = reservationRepository.findAll().collectList().block().size();

        // Update the reservation
        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).block();
        updatedReservation.date(UPDATED_DATE).startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedReservation.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedReservation))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeUpdate);
        Reservation testReservation = reservationList.get(reservationList.size() - 1);
        assertThat(testReservation.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testReservation.getStartTime()).isEqualTo(UPDATED_START_TIME);
        assertThat(testReservation.getEndTime()).isEqualTo(UPDATED_END_TIME);
    }

    @Test
    void putNonExistingReservation() throws Exception {
        int databaseSizeBeforeUpdate = reservationRepository.findAll().collectList().block().size();
        reservation.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, reservation.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchReservation() throws Exception {
        int databaseSizeBeforeUpdate = reservationRepository.findAll().collectList().block().size();
        reservation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamReservation() throws Exception {
        int databaseSizeBeforeUpdate = reservationRepository.findAll().collectList().block().size();
        reservation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateReservationWithPatch() throws Exception {
        // Initialize the database
        reservationRepository.save(reservation).block();

        int databaseSizeBeforeUpdate = reservationRepository.findAll().collectList().block().size();

        // Update the reservation using partial update
        Reservation partialUpdatedReservation = new Reservation();
        partialUpdatedReservation.setId(reservation.getId());

        partialUpdatedReservation.endTime(UPDATED_END_TIME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedReservation.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedReservation))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeUpdate);
        Reservation testReservation = reservationList.get(reservationList.size() - 1);
        assertThat(testReservation.getDate()).isEqualTo(DEFAULT_DATE);
        assertThat(testReservation.getStartTime()).isEqualTo(DEFAULT_START_TIME);
        assertThat(testReservation.getEndTime()).isEqualTo(UPDATED_END_TIME);
    }

    @Test
    void fullUpdateReservationWithPatch() throws Exception {
        // Initialize the database
        reservationRepository.save(reservation).block();

        int databaseSizeBeforeUpdate = reservationRepository.findAll().collectList().block().size();

        // Update the reservation using partial update
        Reservation partialUpdatedReservation = new Reservation();
        partialUpdatedReservation.setId(reservation.getId());

        partialUpdatedReservation.date(UPDATED_DATE).startTime(UPDATED_START_TIME).endTime(UPDATED_END_TIME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedReservation.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedReservation))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeUpdate);
        Reservation testReservation = reservationList.get(reservationList.size() - 1);
        assertThat(testReservation.getDate()).isEqualTo(UPDATED_DATE);
        assertThat(testReservation.getStartTime()).isEqualTo(UPDATED_START_TIME);
        assertThat(testReservation.getEndTime()).isEqualTo(UPDATED_END_TIME);
    }

    @Test
    void patchNonExistingReservation() throws Exception {
        int databaseSizeBeforeUpdate = reservationRepository.findAll().collectList().block().size();
        reservation.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, reservation.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchReservation() throws Exception {
        int databaseSizeBeforeUpdate = reservationRepository.findAll().collectList().block().size();
        reservation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamReservation() throws Exception {
        int databaseSizeBeforeUpdate = reservationRepository.findAll().collectList().block().size();
        reservation.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(reservation))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Reservation in the database
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteReservation() {
        // Initialize the database
        reservationRepository.save(reservation).block();

        int databaseSizeBeforeDelete = reservationRepository.findAll().collectList().block().size();

        // Delete the reservation
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, reservation.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Reservation> reservationList = reservationRepository.findAll().collectList().block();
        assertThat(reservationList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
