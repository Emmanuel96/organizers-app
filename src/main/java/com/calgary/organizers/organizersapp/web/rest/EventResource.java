package com.calgary.organizers.organizersapp.web.rest;

import com.calgary.organizers.organizersapp.domain.Event;
import com.calgary.organizers.organizersapp.repository.EventRepository;
import com.calgary.organizers.organizersapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestTemplate;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.calgary.organizers.organizersapp.domain.Event}.
 */
@RestController
@RequestMapping("/api/events")
@Transactional
public class EventResource {

    @Value("${spring.security.oauth2.client.registration.meetup.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.meetup.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.meetup.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.meetup.token-uri}")
    private String tokenUri;

    private static final Logger LOG = LoggerFactory.getLogger(EventResource.class);

    private static final String ENTITY_NAME = "event";

    private static final String MEETUP_GRAPHQL_API_URL = "https://api.meetup.com/gql";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventRepository eventRepository;

    public EventResource(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * {@code POST  /events} : Create a new event.
     *
     * @param event the event to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new event, or with status {@code 400 (Bad Request)} if the event has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) throws URISyntaxException {
        LOG.debug("REST request to save Event : {}", event);
        if (event.getId() != null) {
            throw new BadRequestAlertException("A new event cannot already have an ID", ENTITY_NAME, "idexists");
        }
        event = eventRepository.save(event);
        return ResponseEntity.created(new URI("/api/events/" + event.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, event.getId().toString()))
            .body(event);
    }

    @PostMapping("/oauth/meetup")
    public ResponseEntity<?> exchangeCodeForTokenAndFetchEvents(@RequestParam String code, @RequestParam String groupUrlName) {
        // Ensure code is provided
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Authorization code is missing");
        }

        RestTemplate restTemplate = new RestTemplate();

        // Set headers for the token request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Populate the body parameters for the token request
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("client_id", clientId);
        requestParams.add("client_secret", clientSecret);
        requestParams.add("grant_type", "authorization_code");
        requestParams.add("redirect_uri", redirectUri);
        requestParams.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestParams, headers);

        try {
            // Step 1: Exchange code for access token
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUri, request, Map.class);

            if (tokenResponse.getStatusCode() == HttpStatus.OK && tokenResponse.getBody() != null) {
                // Get the access token from the response
                String accessToken = (String) tokenResponse.getBody().get("access_token");

                if (accessToken == null || accessToken.isEmpty()) {
                    return ResponseEntity.status(500).body("Failed to retrieve access token.");
                }

                // Step 2: Fetch events using the GraphQL API with the access token
                HttpHeaders graphqlHeaders = new HttpHeaders();
                graphqlHeaders.set("Authorization", "Bearer " + accessToken);
                graphqlHeaders.setContentType(MediaType.APPLICATION_JSON);

                // Create the GraphQL query
                String graphqlQuery =
                    "{ \"query\": \"query ($groupUrlname: String!, $input: ConnectionInput!) { groupByUrlname(urlname: $groupUrlname) { upcomingEvents(input: $input) { edges { node { title dateTime venue { name address } } } } } }\", \"variables\": { \"groupUrlname\": \"" +
                    groupUrlName +
                    "\", \"input\": { \"first\": 10 } } }";

                HttpEntity<String> graphqlRequest = new HttpEntity<>(graphqlQuery, graphqlHeaders);

                // Send request to Meetup GraphQL API
                ResponseEntity<String> graphqlResponse = restTemplate.exchange(
                    MEETUP_GRAPHQL_API_URL,
                    HttpMethod.POST,
                    graphqlRequest,
                    String.class
                );

                // Return the events response from Meetup API
                return ResponseEntity.ok(graphqlResponse.getBody());
            } else {
                return ResponseEntity.status(tokenResponse.getStatusCode()).body(
                    "Failed to retrieve access token. Response code: " + tokenResponse.getStatusCode()
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to retrieve events: " + e.getMessage());
        }
    }

    /**
     * {@code PUT  /events/:id} : Updates an existing event.
     *
     * @param id the id of the event to save.
     * @param event the event to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated event,
     * or with status {@code 400 (Bad Request)} if the event is not valid,
     * or with status {@code 500 (Internal Server Error)} if the event couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable(value = "id", required = false) final Long id, @RequestBody Event event)
        throws URISyntaxException {
        LOG.debug("REST request to update Event : {}, {}", id, event);
        if (event.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, event.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        event = eventRepository.save(event);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, event.getId().toString()))
            .body(event);
    }

    /**
     * {@code PATCH  /events/:id} : Partial updates given fields of an existing event, field will ignore if it is null
     *
     * @param id the id of the event to save.
     * @param event the event to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated event,
     * or with status {@code 400 (Bad Request)} if the event is not valid,
     * or with status {@code 404 (Not Found)} if the event is not found,
     * or with status {@code 500 (Internal Server Error)} if the event couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Event> partialUpdateEvent(@PathVariable(value = "id", required = false) final Long id, @RequestBody Event event)
        throws URISyntaxException {
        LOG.debug("REST request to partial update Event partially : {}, {}", id, event);
        if (event.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, event.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Event> result = eventRepository
            .findById(event.getId())
            .map(existingEvent -> {
                if (event.getEvent_date() != null) {
                    existingEvent.setEvent_date(event.getEvent_date());
                }
                if (event.getEvent_location() != null) {
                    existingEvent.setEvent_location(event.getEvent_location());
                }
                if (event.getEvent_description() != null) {
                    existingEvent.setEvent_description(event.getEvent_description());
                }
                if (event.getEvent_group_name() != null) {
                    existingEvent.setEvent_group_name(event.getEvent_group_name());
                }

                return existingEvent;
            })
            .map(eventRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, event.getId().toString())
        );
    }

    /**
     * {@code GET  /events} : get all the events.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of events in body.
     */
    @GetMapping("")
    public List<Event> getAllEvents() {
        LOG.debug("REST request to get all Events");
        return eventRepository.findAll();
    }

    /**
     * {@code GET  /events/:id} : get the "id" event.
     *
     * @param id the id of the event to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the event, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Event : {}", id);
        Optional<Event> event = eventRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(event);
    }

    /**
     * {@code DELETE  /events/:id} : delete the "id" event.
     *
     * @param id the id of the event to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Event : {}", id);
        eventRepository.deleteById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
