package com.giveitacut.reservation.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.giveitacut.reservation.IntegrationTest;
import com.giveitacut.reservation.domain.Service;
import com.giveitacut.reservation.repository.ServiceRepository;
import com.giveitacut.reservation.service.EntityManager;
import java.time.Duration;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link ServiceResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class ServiceResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Long DEFAULT_DURATION = 1L;
    private static final Long UPDATED_DURATION = 2L;

    private static final Float DEFAULT_PRICE = 1F;
    private static final Float UPDATED_PRICE = 2F;

    private static final String ENTITY_API_URL = "/api/services";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Service service;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Service createEntity(EntityManager em) {
        Service service = new Service().name(DEFAULT_NAME).description(DEFAULT_DESCRIPTION).duration(DEFAULT_DURATION).price(DEFAULT_PRICE);
        return service;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Service createUpdatedEntity(EntityManager em) {
        Service service = new Service().name(UPDATED_NAME).description(UPDATED_DESCRIPTION).duration(UPDATED_DURATION).price(UPDATED_PRICE);
        return service;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Service.class).block();
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
        service = createEntity(em);
    }

    @Test
    void createService() throws Exception {
        int databaseSizeBeforeCreate = serviceRepository.findAll().collectList().block().size();
        // Create the Service
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeCreate + 1);
        Service testService = serviceList.get(serviceList.size() - 1);
        assertThat(testService.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testService.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testService.getDuration()).isEqualTo(DEFAULT_DURATION);
        assertThat(testService.getPrice()).isEqualTo(DEFAULT_PRICE);
    }

    @Test
    void createServiceWithExistingId() throws Exception {
        // Create the Service with an existing ID
        service.setId(1L);

        int databaseSizeBeforeCreate = serviceRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = serviceRepository.findAll().collectList().block().size();
        // set the field null
        service.setName(null);

        // Create the Service, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkDescriptionIsRequired() throws Exception {
        int databaseSizeBeforeTest = serviceRepository.findAll().collectList().block().size();
        // set the field null
        service.setDescription(null);

        // Create the Service, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkDurationIsRequired() throws Exception {
        int databaseSizeBeforeTest = serviceRepository.findAll().collectList().block().size();
        // set the field null
        service.setDuration(null);

        // Create the Service, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkPriceIsRequired() throws Exception {
        int databaseSizeBeforeTest = serviceRepository.findAll().collectList().block().size();
        // set the field null
        service.setPrice(null);

        // Create the Service, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllServices() {
        // Initialize the database
        serviceRepository.save(service).block();

        // Get all the serviceList
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
            .value(hasItem(service.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION))
            .jsonPath("$.[*].duration")
            .value(hasItem(DEFAULT_DURATION.intValue()))
            .jsonPath("$.[*].price")
            .value(hasItem(DEFAULT_PRICE.doubleValue()));
    }

    @Test
    void getService() {
        // Initialize the database
        serviceRepository.save(service).block();

        // Get the service
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, service.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(service.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION))
            .jsonPath("$.duration")
            .value(is(DEFAULT_DURATION.intValue()))
            .jsonPath("$.price")
            .value(is(DEFAULT_PRICE.doubleValue()));
    }

    @Test
    void getNonExistingService() {
        // Get the service
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewService() throws Exception {
        // Initialize the database
        serviceRepository.save(service).block();

        int databaseSizeBeforeUpdate = serviceRepository.findAll().collectList().block().size();

        // Update the service
        Service updatedService = serviceRepository.findById(service.getId()).block();
        updatedService.name(UPDATED_NAME).description(UPDATED_DESCRIPTION).duration(UPDATED_DURATION).price(UPDATED_PRICE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedService.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedService))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
        Service testService = serviceList.get(serviceList.size() - 1);
        assertThat(testService.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testService.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testService.getDuration()).isEqualTo(UPDATED_DURATION);
        assertThat(testService.getPrice()).isEqualTo(UPDATED_PRICE);
    }

    @Test
    void putNonExistingService() throws Exception {
        int databaseSizeBeforeUpdate = serviceRepository.findAll().collectList().block().size();
        service.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, service.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchService() throws Exception {
        int databaseSizeBeforeUpdate = serviceRepository.findAll().collectList().block().size();
        service.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamService() throws Exception {
        int databaseSizeBeforeUpdate = serviceRepository.findAll().collectList().block().size();
        service.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateServiceWithPatch() throws Exception {
        // Initialize the database
        serviceRepository.save(service).block();

        int databaseSizeBeforeUpdate = serviceRepository.findAll().collectList().block().size();

        // Update the service using partial update
        Service partialUpdatedService = new Service();
        partialUpdatedService.setId(service.getId());

        partialUpdatedService.duration(UPDATED_DURATION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedService.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedService))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
        Service testService = serviceList.get(serviceList.size() - 1);
        assertThat(testService.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testService.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testService.getDuration()).isEqualTo(UPDATED_DURATION);
        assertThat(testService.getPrice()).isEqualTo(DEFAULT_PRICE);
    }

    @Test
    void fullUpdateServiceWithPatch() throws Exception {
        // Initialize the database
        serviceRepository.save(service).block();

        int databaseSizeBeforeUpdate = serviceRepository.findAll().collectList().block().size();

        // Update the service using partial update
        Service partialUpdatedService = new Service();
        partialUpdatedService.setId(service.getId());

        partialUpdatedService.name(UPDATED_NAME).description(UPDATED_DESCRIPTION).duration(UPDATED_DURATION).price(UPDATED_PRICE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedService.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedService))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
        Service testService = serviceList.get(serviceList.size() - 1);
        assertThat(testService.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testService.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testService.getDuration()).isEqualTo(UPDATED_DURATION);
        assertThat(testService.getPrice()).isEqualTo(UPDATED_PRICE);
    }

    @Test
    void patchNonExistingService() throws Exception {
        int databaseSizeBeforeUpdate = serviceRepository.findAll().collectList().block().size();
        service.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, service.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchService() throws Exception {
        int databaseSizeBeforeUpdate = serviceRepository.findAll().collectList().block().size();
        service.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamService() throws Exception {
        int databaseSizeBeforeUpdate = serviceRepository.findAll().collectList().block().size();
        service.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(service))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Service in the database
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteService() {
        // Initialize the database
        serviceRepository.save(service).block();

        int databaseSizeBeforeDelete = serviceRepository.findAll().collectList().block().size();

        // Delete the service
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, service.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Service> serviceList = serviceRepository.findAll().collectList().block();
        assertThat(serviceList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
