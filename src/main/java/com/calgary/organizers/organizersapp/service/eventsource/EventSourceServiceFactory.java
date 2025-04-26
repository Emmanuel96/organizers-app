package com.calgary.organizers.organizersapp.service.eventsource;

import com.calgary.organizers.organizersapp.domain.Group;
import com.calgary.organizers.organizersapp.enums.EventSource;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EventSourceServiceFactory {

    private final Map<EventSource, EventSourceService> eventSourceServiceMap = new EnumMap<>(EventSource.class);

    public EventSourceServiceFactory(List<EventSourceService> eventSourceServices) {
        for (EventSourceService eventSourceService : eventSourceServices) {
            eventSourceServiceMap.put(eventSourceService.getEventSource(), eventSourceService);
        }
    }

    public void syncEventsForGroup(Group meetupGroupName) {
        eventSourceServiceMap.get(meetupGroupName.getEventSource()).syncEventsForGroup(meetupGroupName.getOrganizerId());
    }

    public void verifyOrganizerParameters(Group group) {
        eventSourceServiceMap.get(group.getEventSource()).verifyOrganizerParameters(group.getOrganizerId());
    }

    public String getOrganizerIdByUrl(EventSource eventSource, String url) {
        return eventSourceServiceMap.get(eventSource).getOrganizerIdByUrl(url);
    }

    public Group getOrganizerByOrganizerId(EventSource eventSource, String organizerId) {
        return eventSourceServiceMap.get(eventSource).getOrganizerByOrganizerId(organizerId);
    }
}
