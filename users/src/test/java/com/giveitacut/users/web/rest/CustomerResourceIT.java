package com.giveitacut.users.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.giveitacut.users.IntegrationTest;
import com.giveitacut.users.domain.Customer;
import com.giveitacut.users.repository.CustomerRepository;
import com.giveitacut.users.repository.UserRepository;
import com.giveitacut.users.service.EntityManager;
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
 * Integration tests for the {@link CustomerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient
@WithMockUser
class CustomerResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_SURNAME = "AAAAAAAAAA";
    private static final String UPDATED_SURNAME = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_GENDER = "AAAAAAAAAA";
    private static final String UPDATED_GENDER = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/customers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Customer customer;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Customer createEntity(EntityManager em) {
        Customer customer = new Customer()
            .name(DEFAULT_NAME)
            .surname(DEFAULT_SURNAME)
            .phone(DEFAULT_PHONE)
            .email(DEFAULT_EMAIL)
            .gender(DEFAULT_GENDER);
        return customer;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Customer createUpdatedEntity(EntityManager em) {
        Customer customer = new Customer()
            .name(UPDATED_NAME)
            .surname(UPDATED_SURNAME)
            .phone(UPDATED_PHONE)
            .email(UPDATED_EMAIL)
            .gender(UPDATED_GENDER);
        return customer;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Customer.class).block();
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
        customer = createEntity(em);
    }

    @Test
    void createCustomer() throws Exception {
        int databaseSizeBeforeCreate = customerRepository.findAll().collectList().block().size();
        // Create the Customer
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeCreate + 1);
        Customer testCustomer = customerList.get(customerList.size() - 1);
        assertThat(testCustomer.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testCustomer.getSurname()).isEqualTo(DEFAULT_SURNAME);
        assertThat(testCustomer.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(testCustomer.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testCustomer.getGender()).isEqualTo(DEFAULT_GENDER);
    }

    @Test
    void createCustomerWithExistingId() throws Exception {
        // Create the Customer with an existing ID
        customer.setId(1L);

        int databaseSizeBeforeCreate = customerRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().collectList().block().size();
        // set the field null
        customer.setName(null);

        // Create the Customer, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkSurnameIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().collectList().block().size();
        // set the field null
        customer.setSurname(null);

        // Create the Customer, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkPhoneIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().collectList().block().size();
        // set the field null
        customer.setPhone(null);

        // Create the Customer, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkEmailIsRequired() throws Exception {
        int databaseSizeBeforeTest = customerRepository.findAll().collectList().block().size();
        // set the field null
        customer.setEmail(null);

        // Create the Customer, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllCustomers() {
        // Initialize the database
        customerRepository.save(customer).block();

        // Get all the customerList
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
            .value(hasItem(customer.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].surname")
            .value(hasItem(DEFAULT_SURNAME))
            .jsonPath("$.[*].phone")
            .value(hasItem(DEFAULT_PHONE))
            .jsonPath("$.[*].email")
            .value(hasItem(DEFAULT_EMAIL))
            .jsonPath("$.[*].gender")
            .value(hasItem(DEFAULT_GENDER));
    }

    @Test
    void getCustomer() {
        // Initialize the database
        customerRepository.save(customer).block();

        // Get the customer
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, customer.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(customer.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.surname")
            .value(is(DEFAULT_SURNAME))
            .jsonPath("$.phone")
            .value(is(DEFAULT_PHONE))
            .jsonPath("$.email")
            .value(is(DEFAULT_EMAIL))
            .jsonPath("$.gender")
            .value(is(DEFAULT_GENDER));
    }

    @Test
    void getNonExistingCustomer() {
        // Get the customer
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewCustomer() throws Exception {
        // Initialize the database
        customerRepository.save(customer).block();

        int databaseSizeBeforeUpdate = customerRepository.findAll().collectList().block().size();

        // Update the customer
        Customer updatedCustomer = customerRepository.findById(customer.getId()).block();
        updatedCustomer.name(UPDATED_NAME).surname(UPDATED_SURNAME).phone(UPDATED_PHONE).email(UPDATED_EMAIL).gender(UPDATED_GENDER);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedCustomer.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedCustomer))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
        Customer testCustomer = customerList.get(customerList.size() - 1);
        assertThat(testCustomer.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCustomer.getSurname()).isEqualTo(UPDATED_SURNAME);
        assertThat(testCustomer.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testCustomer.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testCustomer.getGender()).isEqualTo(UPDATED_GENDER);
    }

    @Test
    void putNonExistingCustomer() throws Exception {
        int databaseSizeBeforeUpdate = customerRepository.findAll().collectList().block().size();
        customer.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, customer.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchCustomer() throws Exception {
        int databaseSizeBeforeUpdate = customerRepository.findAll().collectList().block().size();
        customer.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamCustomer() throws Exception {
        int databaseSizeBeforeUpdate = customerRepository.findAll().collectList().block().size();
        customer.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateCustomerWithPatch() throws Exception {
        // Initialize the database
        customerRepository.save(customer).block();

        int databaseSizeBeforeUpdate = customerRepository.findAll().collectList().block().size();

        // Update the customer using partial update
        Customer partialUpdatedCustomer = new Customer();
        partialUpdatedCustomer.setId(customer.getId());

        partialUpdatedCustomer.name(UPDATED_NAME).gender(UPDATED_GENDER);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCustomer.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedCustomer))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
        Customer testCustomer = customerList.get(customerList.size() - 1);
        assertThat(testCustomer.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCustomer.getSurname()).isEqualTo(DEFAULT_SURNAME);
        assertThat(testCustomer.getPhone()).isEqualTo(DEFAULT_PHONE);
        assertThat(testCustomer.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testCustomer.getGender()).isEqualTo(UPDATED_GENDER);
    }

    @Test
    void fullUpdateCustomerWithPatch() throws Exception {
        // Initialize the database
        customerRepository.save(customer).block();

        int databaseSizeBeforeUpdate = customerRepository.findAll().collectList().block().size();

        // Update the customer using partial update
        Customer partialUpdatedCustomer = new Customer();
        partialUpdatedCustomer.setId(customer.getId());

        partialUpdatedCustomer.name(UPDATED_NAME).surname(UPDATED_SURNAME).phone(UPDATED_PHONE).email(UPDATED_EMAIL).gender(UPDATED_GENDER);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCustomer.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedCustomer))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
        Customer testCustomer = customerList.get(customerList.size() - 1);
        assertThat(testCustomer.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testCustomer.getSurname()).isEqualTo(UPDATED_SURNAME);
        assertThat(testCustomer.getPhone()).isEqualTo(UPDATED_PHONE);
        assertThat(testCustomer.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testCustomer.getGender()).isEqualTo(UPDATED_GENDER);
    }

    @Test
    void patchNonExistingCustomer() throws Exception {
        int databaseSizeBeforeUpdate = customerRepository.findAll().collectList().block().size();
        customer.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, customer.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchCustomer() throws Exception {
        int databaseSizeBeforeUpdate = customerRepository.findAll().collectList().block().size();
        customer.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamCustomer() throws Exception {
        int databaseSizeBeforeUpdate = customerRepository.findAll().collectList().block().size();
        customer.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(customer))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Customer in the database
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteCustomer() {
        // Initialize the database
        customerRepository.save(customer).block();

        int databaseSizeBeforeDelete = customerRepository.findAll().collectList().block().size();

        // Delete the customer
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, customer.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Customer> customerList = customerRepository.findAll().collectList().block();
        assertThat(customerList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
