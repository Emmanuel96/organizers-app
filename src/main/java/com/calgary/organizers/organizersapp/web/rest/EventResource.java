package com.calgary.organizers.organizersapp.web.rest;

import com.calgary.organizers.organizersapp.domain.Event;
import com.calgary.organizers.organizersapp.domain.Group;
import com.calgary.organizers.organizersapp.domain.User;
import com.calgary.organizers.organizersapp.repository.EventRepository;
import com.calgary.organizers.organizersapp.repository.UserRepository;
import com.calgary.organizers.organizersapp.service.EventService;
import com.calgary.organizers.organizersapp.service.eventsource.meetup.MeetupService;
import com.calgary.organizers.organizersapp.service.oauth.ServerFlowProvider;
import com.calgary.organizers.organizersapp.web.rest.errors.BadRequestAlertException;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.calgary.organizers.organizersapp.domain.Event}.
 */
@RestController
@RequestMapping("/api/events")
@Transactional
public class EventResource {

    private static final Logger LOG = LoggerFactory.getLogger(EventResource.class);
    private static final String ENTITY_NAME = "event";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EventRepository eventRepository;
    private final ServerFlowProvider serverFlowProvider;
    private final MeetupService meetupService;
    private final EventService eventService;
    private final UserRepository userRepository;

    public EventResource(
        EventRepository eventRepository,
        ServerFlowProvider serverFlowProvider,
        MeetupService meetupService,
        EventService eventService,
        UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.serverFlowProvider = serverFlowProvider;
        this.meetupService = meetupService;
        this.eventService = eventService;
        this.userRepository = userRepository;
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
    public ResponseEntity<?> exchangeCodeForTokenAndFetchEvents(
        @RequestParam @NotBlank(message = "Authorization code is missing") String code,
        @RequestParam @NotBlank String groupUrlName
    ) {
        // Step 1: Exchange code for access token
        String accessToken = serverFlowProvider.getAccessToken(code);
        // Step 2: Fetch events using the GraphQL API with the access token
        List<Event> meetupEvents = meetupService.fetchEvents(accessToken, groupUrlName);
        // Save all events to the database
        eventService.saveEvents(meetupEvents);
        return ResponseEntity.ok("Events fetched and saved successfully.");
    }

    /**
     * {@code PUT  /events/:id} : Updates an existing event.
     *
     * @param id    the id of the event to save.
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
     * @param id    the id of the event to save.
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
                if (event.getEventGroupName() != null) {
                    existingEvent.setEventGroupName(event.getEventGroupName());
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

        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If the user is not authenticated, just return all events
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return eventRepository.findAll();
        }

        // Otherwise, apply filtering based on the logged-in user
        String currentUserLogin = authentication.getName();
        User currentUser = userRepository.findOneByLogin(currentUserLogin).orElseThrow();

        Set<String> excludedGroupNames = currentUser
            .getExcludedGroups()
            .stream()
            .map(Group::getMeetup_group_name)
            .collect(Collectors.toSet());

        List<Event> events = eventRepository.findAll();

        return events
            .stream()
            .filter(event -> event.getEventGroupName() == null || !excludedGroupNames.contains(event.getEventGroupName()))
            .collect(Collectors.toList());
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
