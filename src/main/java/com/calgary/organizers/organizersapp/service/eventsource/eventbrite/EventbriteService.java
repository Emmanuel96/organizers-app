package com.calgary.organizers.organizersapp.service.eventsource.eventbrite;

import com.calgary.organizers.organizersapp.domain.Event;
import com.calgary.organizers.organizersapp.domain.Group;
import com.calgary.organizers.organizersapp.enums.EventSource;
import com.calgary.organizers.organizersapp.service.EventService;
import com.calgary.organizers.organizersapp.service.eventsource.EventSourceService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Equator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class EventbriteService implements EventSourceService {

    private final RestTemplate restTemplate;
    private final EventService eventService;
    private final ObjectMapper objectMapper;

    public EventbriteService(RestTemplate restTemplate, EventService eventService, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Event> fetchEvents(String organizerId) {
        String city = null;
        List<Event> results = new ArrayList<>();
        //For this endpoint eventbrite has max page size 30. We fetch first page.
        String idOnlyUrl = "https://www.eventbrite.ca/org/" + organizerId + "/showmore/?page_size=30&type=future";

        ResponseEntity<String> idResponse = restTemplate.getForEntity(idOnlyUrl, String.class);
        if (!idResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to fetch showmore events: " + idResponse.getStatusCode());
        }

        List<String> seriesEventIds;
        try {
            JsonNode root = objectMapper.readTree(idResponse.getBody());
            JsonNode eventsArray = root.path("data").path("events");
            seriesEventIds = new ArrayList<>();
            for (JsonNode e : eventsArray) {
                if (e.get("is_series_parent").asBoolean()) {
                    seriesEventIds.add(e.get("id").asText());
                } else {
                    Event ev = new Event();
                    ev.setOrganizerId(e.path("organizer").path("id").asText());
                    ev.setEventGroupDisplayName(e.path("organizer").path("name").asText());
                    ev.setEventId(e.path("id").asText());
                    ev.setEventTitle(e.path("name").path("text").asText());
                    ev.setEvent_url(e.path("url").asText());
                    ev.setEvent_description(StringUtils.left(e.path("summary").asText(), 255));
                    ev.setEvent_date(ZonedDateTime.parse(e.path("start").path("utc").asText()));
                    ev.setEvent_location(e.path("venue").path("address").path("localized_address_display").asText(null));

                    ev.setEventSource(EventSource.EVENTBRITE);
                    ev.setDynamic(true);

                    city = e.path("venue").path("address").path("city").asText(null);

                    if (Objects.equals(city, "Calgary")) {
                        results.add(ev);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing event IDs JSON", e);
        }

        if (!seriesEventIds.isEmpty()) {
            String idsParam = String.join(",", seriesEventIds);
            String detailUrl = UriComponentsBuilder.fromHttpUrl("https://www.eventbrite.com/api/v3/destination/events/")
                .queryParam("event_ids", idsParam)
                .queryParam("expand", "series,primary_organizer,primary_venue")
                .queryParam("page_size", 50)
                .queryParam("include_parent_events", true)
                .toUriString();

            ResponseEntity<String> detailResponse = restTemplate.getForEntity(detailUrl, String.class);
            if (!detailResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to fetch event details: " + detailResponse.getStatusCode());
            }

            try {
                JsonNode root = objectMapper.readTree(detailResponse.getBody());
                JsonNode detailedEvents = root.path("events");
                for (JsonNode node : detailedEvents) {
                    List<Event> evs = new ArrayList<>();
                    for (JsonNode seriesNode : node.get("series").get("next_dates")) {
                        Event ev = new Event();
                        ev.setOrganizerId(node.path("primary_organizer_id").asText());
                        ev.setEventGroupDisplayName(node.path("primary_organizer").path("name").asText());
                        ev.setEventId(seriesNode.path("id").asText());
                        ev.setEventTitle(node.path("name").asText());
                        ev.setEvent_url(node.path("url").asText().replace(node.get("id").asText(), seriesNode.get("id").asText()));
                        ev.setEvent_description(StringUtils.left(node.path("summary").asText(), 255));
                        ev.setEvent_date(ZonedDateTime.parse(seriesNode.path("start").asText()));
                        ev.setDynamic(true);
                        ev.setEventSource(EventSource.EVENTBRITE);

                        JsonNode venueNode = node.path("primary_venue");
                        String locationString = null;

                        if (!venueNode.isMissingNode() && !venueNode.isNull()) {
                            JsonNode addressNode = venueNode.path("address");
                            if (!addressNode.isMissingNode() && !addressNode.isNull()) {
                                String address1 = addressNode.path("address_1").asText(null);
                                city = addressNode.path("city").asText(null);
                                List<String> addressParts = new ArrayList<>();

                                if (StringUtils.isNotBlank(address1)) addressParts.add(address1);
                                if (StringUtils.isNotBlank(city)) addressParts.add(city);

                                if (!addressParts.isEmpty()) {
                                    locationString = String.join(", ", addressParts);
                                }
                            }

                            if (locationString == null) {
                                locationString = venueNode.path("name").asText(null);
                            }
                        }
                        ev.setEvent_location(locationString);
                        if (Objects.equals(city, "Calgary")) {
                            evs.add(ev);
                        }
                    }
                    results.addAll(evs);
                }
            } catch (JsonProcessingException | DateTimeParseException e) {
                throw new RuntimeException("Error parsing detailed events JSON", e);
            }
        }
        return results;
    }

    @Transactional
    @Override
    public void syncEventsForGroup(String organizerId) {
        List<Event> oldEvents = eventService.getEventsForOrganizerId(organizerId);
        List<Event> newEvents = fetchEvents(organizerId);
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

    @Override
    public EventSource getEventSource() {
        return EventSource.EVENTBRITE;
    }

    @Override
    public void verifyOrganizerParameters(String organizerId) {
        //TODO: add verification logic
    }

    @Override
    public String getOrganizerIdByUrl(String url) {
        Pattern pattern = Pattern.compile("^https?://(?:www\\.)?eventbrite\\.(?:com|ca)/o/[\\w-]+-(\\d+)$");
        Matcher m = pattern.matcher(url);
        if (m.find()) {
            String groupSlug = m.group(1);
            return groupSlug;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Group getOrganizerByOrganizerId(String organizerId) {
        String url = UriComponentsBuilder.fromHttpUrl("https://www.eventbrite.com/api/v3/organizers")
            .queryParam("ids", organizerId)
            .queryParam("expand.organizer", "follow_status")
            .toUriString();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to fetch organizer: " + response.getStatusCode());
        }
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode organizers = root.path("organizers");
            if (!organizers.isArray() || organizers.size() == 0) {
                throw new RuntimeException("No organizer found for id: " + organizerId);
            }

            JsonNode first = organizers.get(0);

            Group group = new Group();
            group.setEventSource(EventSource.EVENTBRITE);
            group.setName(first.path("name").asText());
            group.setOrganizerId(first.path("id").asText());
            return group;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing Eventbrite response", e);
        }
    }
}
