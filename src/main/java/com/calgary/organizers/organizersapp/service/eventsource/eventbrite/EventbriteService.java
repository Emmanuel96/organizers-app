package com.calgary.organizers.organizersapp.service.eventsource.eventbrite;

import com.calgary.organizers.organizersapp.domain.Event;
import com.calgary.organizers.organizersapp.scheduled.EventEquator;
import com.calgary.organizers.organizersapp.service.EventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class EventbriteService {

    private final RestTemplate restTemplate;
    private final EventService eventService;

    public EventbriteService(RestTemplate restTemplate, EventService eventService) {
        this.restTemplate = restTemplate;
        this.eventService = eventService;
    }

    public List<Event> fetchEvents(String eventbriteOrganizerId) {
        // Eventbrite endpoint which returns a JSON response
        final String url = "https://www.eventbrite.ca/org/" + eventbriteOrganizerId + "/showmore/?page_size=100&type=future";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                // Navigate to the events array in the JSON response
                JsonNode eventsArray = rootNode.path("data").path("events");
                List<Event> events = new ArrayList<>();

                // Iterate over each event node in the events array
                for (JsonNode eventNode : eventsArray) {
                    Event event = new Event();

                    event.setEventbriteOrganizerId(eventNode.get("organizer").get("id").asText());
                    event.setEventGroupDisplayName(eventNode.get("organizer").get("name").asText());
                    event.setEventId(eventNode.get("id").asText());
                    event.setEventTitle(eventNode.get("name").get("text").asText());
                    event.setEvent_description(StringUtils.left(eventNode.get("description").get("text").asText(), 255));
                    event.setEvent_url(eventNode.get("url").asText());
                    event.setDynamic(true);

                    // Parse and set the event date from the "start.utc" field
                    if (eventNode.has("start") && eventNode.get("start").has("utc")) {
                        try {
                            ZonedDateTime eventDate = ZonedDateTime.parse(eventNode.get("start").get("utc").asText());
                            event.setEvent_date(eventDate);
                        } catch (DateTimeParseException e) {
                            // Log or handle the parsing error if necessary
                            System.err.println("Error parsing event date for event id " + event.getEventId() + ": " + e.getMessage());
                        }
                    }

                    // Set the event location from the venue's address, if available
                    if (eventNode.has("venue")) {
                        JsonNode venueNode = eventNode.get("venue");
                        if (venueNode.has("address") && venueNode.get("address").has("localized_address_display")) {
                            event.setEvent_location(venueNode.get("address").get("localized_address_display").asText());
                        }
                    }

                    events.add(event);
                }
                return events;
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing JSON response: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("Failed request with code: " + response.getStatusCode());
        }
    }

    @Transactional
    public void syncEventsForGroup(String eventbriteOrganizerId) {
        List<Event> oldEvents = eventService.getEventsForEventbriteOrganizerId(eventbriteOrganizerId);
        List<Event> newEvents = fetchEvents(eventbriteOrganizerId);
        Map<String, Event> oldEventsMap = oldEvents.stream().collect(Collectors.toMap(Event::getEventId, Function.identity(), (x, y) -> x));
        for (Event newEvent : newEvents) {
            Event e = oldEventsMap.get(newEvent.getEventId());
            if (Objects.nonNull(e)) {
                newEvent.setId(e.getId());
            }
        }
        eventService.saveEvents(newEvents);
        Collection<Event> eventsForRemove = CollectionUtils.removeAll(
            oldEvents,
            newEvents,
            new Equator<>() {
                @Override
                public boolean equate(Event o1, Event o2) {
                    return o1.getId().equals(o2.getId());
                }

                @Override
                public int hash(Event o) {
                    return o.hashCode();
                }
            }
        );
        eventService.deleteEvents(eventsForRemove);
    }
}
