package com.calgary.organizers.organizersapp.service.eventsource;

import com.calgary.organizers.organizersapp.domain.Event;
import com.calgary.organizers.organizersapp.scheduled.EventEquator;
import com.calgary.organizers.organizersapp.service.EventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class MeetupService {

    private final RestTemplate restTemplate;
    private final EventService eventService;
    private static final String MEETUP_GRAPHQL_API_URL = "https://api.meetup.com/gql";

    public MeetupService(RestTemplate restTemplate, EventService eventService) {
        this.restTemplate = restTemplate;
        this.eventService = eventService;
    }

    public List<Event> fetchEvents(String accessToken, String groupUrlName) {
        HttpHeaders graphqlHeaders = new HttpHeaders();
        graphqlHeaders.set("Authorization", "Bearer " + accessToken);
        graphqlHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Create the GraphQL query
        String graphqlQuery =
            "{ \"query\": \"query ($groupUrlname: String!, $input: ConnectionInput!) { groupByUrlname(urlname: $groupUrlname) { upcomingEvents(input: $input) { edges { node { id title dateTime venue { name address } } } } } }\", \"variables\": { \"groupUrlname\": \"" +
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

        if (graphqlResponse.getStatusCode() == HttpStatus.OK) {
            // Parse the response to extract event data
            ObjectMapper objectMapper = new ObjectMapper();
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
                // event.setevent_(eventNode.get("title").asText());
                event.setEvent_description(eventNode.get("title").asText());
                event.setEvent_date(ZonedDateTime.parse(eventNode.get("dateTime").asText()));
                event.setEvent_group_name(groupUrlName);
                event.setEvent_location(eventNode.at("/venue/address").asText());
                event.setEvent_location(eventNode.at("/venue/address").asText());
                event.setEventId(eventNode.get("id").asText());
                event.setGroupName(groupUrlName);
                event.setDynamic(true);
                eventsToSave.add(event);
            }
            return eventsToSave;
        } else {
            throw new GraphQLException("Failed GraphQL request with code : %s".formatted(graphqlResponse.getStatusCode().toString()));
        }
    }

    @Transactional
    public void syncEventsForGroup(String accessToken, String groupName) {
        List<Event> oldEvents = eventService.getDynamicEventsForGroup(groupName);
        List<Event> newEvents = fetchEvents(accessToken, groupName);
        Map<String, Event> oldEventsMap = oldEvents.stream().collect(Collectors.toMap(Event::getEventId, Function.identity()));
        for (Event newEvent : newEvents) {
            Event e = oldEventsMap.get(newEvent.getEventId());
            if (Objects.nonNull(e)) {
                newEvent.setId(e.getId());
            }
        }
        eventService.saveEvents(newEvents);
        Collection<Event> eventsForRemove = CollectionUtils.removeAll(oldEvents, newEvents, new EventEquator());
        eventService.deleteEvents(eventsForRemove);
    }

    public void verifyGroupParameters(String groupName, String accessToken) {
        HttpHeaders graphqlHeaders = new HttpHeaders();
        graphqlHeaders.set("Authorization", "Bearer " + accessToken);
        graphqlHeaders.setContentType(MediaType.APPLICATION_JSON);
        String graphqlQuery =
            "query GetEventsByGroup($groupUrlname: String!) { " +
            "  groupByUrlname(urlname: $groupUrlname) { " +
            "    id " +
            "    name " +
            "    city " +
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
        String variables = "{ " + "    \"groupUrlname\": \"" + groupName + "\" " + "  }";
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
            var city = rootNode.at("/data/groupByUrlname/city").asText();
            if (!city.equals("Calgary")) {
                throw new InvalidGroupParameterException("Group from wrong city provided. Provided '%s'".formatted(city));
            }
            var topicCategory = rootNode.at("/data/groupByUrlname/topicCategory/name").asText();
            if (!topicCategory.equals("Technology")) {
                throw new InvalidGroupParameterException(
                    "Group from wrong topic category provided. Provided '%s'".formatted(topicCategory)
                );
            }
        } else {
            throw new GraphQLException("Failed GraphQL request with code : %s".formatted(graphqlResponse.getStatusCode().toString()));
        }
    }
}
