package com.calgary.organizers.organizersapp.service;

import com.calgary.organizers.organizersapp.domain.Event;
import com.calgary.organizers.organizersapp.repository.EventRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void saveEvents(List<Event> events) {
        eventRepository.saveAll(events);
    }

    public List<Event> getDynamicEventsForGroup(String groupName) {
        return eventRepository.findAllByDynamicIsTrueAndGroupName(groupName);
    }

    public void deleteEvents(Collection<Event> eventsForRemove) {
        eventRepository.deleteAll(eventsForRemove);
    }
}
