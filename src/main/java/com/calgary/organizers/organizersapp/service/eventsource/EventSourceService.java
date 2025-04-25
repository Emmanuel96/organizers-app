package com.calgary.organizers.organizersapp.service.eventsource;

import com.calgary.organizers.organizersapp.domain.Event;
import com.calgary.organizers.organizersapp.enums.EventSource;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public interface EventSourceService {
    List<Event> fetchEvents(String groupUrlName);

    @Transactional
    void syncEventsForGroup(String groupName);

    EventSource getEventSource();

    void verifyOrganizerParameters(String organizerId);
}
