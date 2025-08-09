package com.calgary.organizers.organizersapp.service.eventsource.meetup;

import com.calgary.organizers.organizersapp.domain.Event;
import com.calgary.organizers.organizersapp.domain.Group;
import com.calgary.organizers.organizersapp.enums.EventSource;
import com.calgary.organizers.organizersapp.scheduled.EventEquator;
import com.calgary.organizers.organizersapp.service.EventService;
import com.calgary.organizers.organizersapp.service.eventsource.EventSourceService;
import com.calgary.organizers.organizersapp.utils.meetup.MeetupDomainValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class MeetupService implements EventSourceService {

    private final RestTemplate restTemplate;
    private final EventService eventService;
    private final ObjectMapper objectMapper;
    private final List<MeetupDomainValidator> validators;
    private static final String MEETUP_GRAPHQL_API_URL = "https://api.meetup.com/gql";

    public MeetupService(
        RestTemplate restTemplate,
        EventService eventService,
        ObjectMapper objectMapper,
        List<MeetupDomainValidator> validators
    ) {
        this.restTemplate = restTemplate;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
        this.validators = validators;
    }

    @Override
    public List<Event> fetchEvents(String groupUrlName) {
        HttpHeaders graphqlHeaders = new HttpHeaders();
        graphqlHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Create the GraphQL query
        String graphqlQuery =
            "{ \"query\": \"query ($groupUrlname: String!, $input: ConnectionInput!) { " +
            "  groupByUrlname(urlname: $groupUrlname) { " +
            "    name " +
            "    upcomingEvents(input: $input) { " +
            "      edges { " +
            "        node { " +
            "          id " +
            "          title " +
            "          description " +
            "          dateTime " +
            "          venue { " +
            "            name " +
            "            address " +
            "          } " +
            "        } " +
            "      } " +
            "    } " +
            "  } " +
            "}\", " +
            "\"variables\": { " +
            "  \"groupUrlname\": \"" +
            groupUrlName +
            "\", " +
            "  \"input\": { \"first\": 10 } " +
            "} }";

        HttpEntity<String> graphqlRequest = new HttpEntity<>(graphqlQuery, graphqlHeaders);

        // Send request to Meetup GraphQL API
        ResponseEntity<String> graphqlResponse = restTemplate.exchange(
            MEETUP_GRAPHQL_API_URL,
            HttpMethod.POST,
            graphqlRequest,
            String.class
        );

        if (graphqlResponse.getStatusCode() == HttpStatus.OK) {
            // Parse the response to extract event data
            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(graphqlResponse.getBody());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            JsonNode eventsNode = rootNode.at("/data/groupByUrlname/upcomingEvents/edges");

            List<Event> eventsToSave = new ArrayList<>();
            for (JsonNode edge : eventsNode) {
                JsonNode eventNode = edge.get("node");
                Event event = new Event();
                event.setEvent_url("https://www.meetup.com/" + groupUrlName + "/events/" + eventNode.get("id").asText());
                event.setEventTitle(eventNode.get("title").asText());
                event.setEvent_description(StringUtils.left(eventNode.get("description").asText(), 255));
                event.setEvent_date(ZonedDateTime.parse(eventNode.get("dateTime").asText()));
                event.setOrganizerId(groupUrlName);
                event.setEvent_location(eventNode.at("/venue/address").asText());
                event.setEvent_location(eventNode.at("/venue/address").asText());
                event.setEventId(eventNode.get("id").asText());
                event.setEventGroupDisplayName(rootNode.at("/data/groupByUrlname/name").asText());
                event.setEventSource(EventSource.MEET_UP);
                event.setDynamic(true);
                eventsToSave.add(event);
            }
            return eventsToSave;
        } else {
            throw new GraphQLException("Failed GraphQL request with code : %s".formatted(graphqlResponse.getStatusCode().toString()));
        }
    }

    private String eventKey(Event e) {
        if (e.getEventId() != null && !e.getEventId().isBlank()) return e.getEventId();
        // Fallback composite key to avoid null collisions
        return String.join(
            "|",
            String.valueOf(e.getOrganizerId()),
            String.valueOf(e.getEvent_date()),
            String.valueOf(e.getEvent_location())
        );
    }

    @Transactional
    @Override
    public void syncEventsForGroup(String groupName) {
        List<Event> oldEvents = eventService.getEventsForOrganizerId(groupName);
        List<Event> newEvents = fetchEvents(groupName);
        Map<String, Event> oldEventsMap = oldEvents
            .stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    this::eventKey,
                    java.util.function.Function.identity(),
                    (a, b) -> a, // keep first on duplicate
                    java.util.LinkedHashMap::new
                )
            );
        ZonedDateTime now = ZonedDateTime.now();

        // If you also build a map for new/incoming events, do the same, optionally preferring the newer value:
        Map<String, Event> newEventsMap = newEvents
            .stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    this::eventKey,
                    java.util.function.Function.identity(),
                    (a, b) -> b, // keep newer on duplicate
                    java.util.LinkedHashMap::new
                )
            );

        // When syncing, reuse the same key to carry over DB ids:
        for (Event ne : newEvents) {
            Event existing = oldEventsMap.get(eventKey(ne));
            if (existing != null) {
                ne.setId(existing.getId());
            }
        }
        eventService.saveEvents(newEvents);

        Collection<Event> eventsForRemove = oldEvents
            .stream()
            .filter(event -> event.getEvent_date().isAfter(now))
            .filter(event -> newEvents.stream().noneMatch(newEvent -> new EventEquator().equate(event, newEvent)))
            .collect(Collectors.toList());

        eventService.deleteEvents(eventsForRemove);
    }

    @Override
    public EventSource getEventSource() {
        return EventSource.MEET_UP;
    }

    @Override
    public void verifyOrganizerParameters(String organizerId) {
        HttpHeaders graphqlHeaders = new HttpHeaders();
        graphqlHeaders.setContentType(MediaType.APPLICATION_JSON);
        String graphqlQuery =
            "query GetEventsByGroup($groupUrlname: String!) { " +
            "  groupByUrlname(urlname: $groupUrlname) { " +
            "    id " +
            "    name " +
            "    city " +
            "    topics { " +
            "        id " +
            "        name " +
            "        } " +
            "  topicCategory { " +
            "        id " +
            "        urlkey " +
            "        name " +
            "        color " +
            "        imageUrl " +
            "        defaultTopic { " +
            "            name " +
            "        } " +
            "    }" +
            "  } " +
            "} ";
        String variables = "{ " + "    \"groupUrlname\": \"" + organizerId + "\" " + "  }";
        String restGraphqlQuery = "{ \"query\": \"" + graphqlQuery + "\", \"variables\": " + variables + "}";
        HttpEntity<String> graphqlRequest = new HttpEntity<>(restGraphqlQuery, graphqlHeaders);

        ResponseEntity<String> graphqlResponse = restTemplate.exchange(
            MEETUP_GRAPHQL_API_URL,
            HttpMethod.POST,
            graphqlRequest,
            String.class
        );

        if (graphqlResponse.getStatusCode() == HttpStatus.OK) {
            JsonNode rootNode = null;
            try {
                rootNode = new ObjectMapper().readTree(graphqlResponse.getBody());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            for (var validator : validators) {
                validator.validate(rootNode);
            }
        } else {
            throw new GraphQLException("Failed GraphQL request with code : %s".formatted(graphqlResponse.getStatusCode().toString()));
        }
    }

    @Override
    public String getOrganizerIdByUrl(String url) {
        Pattern p = Pattern.compile("https?://(?:www\\.)?meetup\\.com/([^/]+)/.*");
        Matcher m = p.matcher(url);
        if (m.find()) {
            String groupSlug = m.group(1);
            System.out.println(groupSlug);
            return groupSlug;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Group getOrganizerByOrganizerId(String organizerId) {
        HttpHeaders graphqlHeaders = new HttpHeaders();
        graphqlHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Create the GraphQL query
        String graphqlQuery =
            "{ \"query\": \"query ($groupUrlname: String!) { " +
            "  groupByUrlname(urlname: $groupUrlname) { " +
            "    name " +
            "  } " +
            "}\", " +
            "\"variables\": { " +
            "  \"groupUrlname\": \"" +
            organizerId +
            "\", " +
            "  \"input\": { \"first\": 10 } " +
            "} }";

        HttpEntity<String> graphqlRequest = new HttpEntity<>(graphqlQuery, graphqlHeaders);

        ResponseEntity<String> graphqlResponse = restTemplate.exchange(
            MEETUP_GRAPHQL_API_URL,
            HttpMethod.POST,
            graphqlRequest,
            String.class
        );

        if (graphqlResponse.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Request for organizer with id " + organizerId + " failed");
        }
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(graphqlResponse.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Group group = new Group();
        group.setEventSource(EventSource.MEET_UP);
        group.setName(rootNode.at("/data/groupByUrlname/name").asText());
        group.setOrganizerId(organizerId);
        return group;
    }
}
