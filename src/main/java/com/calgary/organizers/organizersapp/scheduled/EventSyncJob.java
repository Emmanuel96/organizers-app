package com.calgary.organizers.organizersapp.scheduled;

import com.calgary.organizers.organizersapp.domain.Group;
import com.calgary.organizers.organizersapp.service.GroupService;
import com.calgary.organizers.organizersapp.service.eventsource.EventSourceServiceFactory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EventSyncJob {

    private final EventSourceServiceFactory eventSourceServiceFactory;
    private final GroupService groupService;

    public EventSyncJob(EventSourceServiceFactory eventSourceServiceFactory, GroupService groupService) {
        this.eventSourceServiceFactory = eventSourceServiceFactory;
        this.groupService = groupService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncEvents() {
        int page = 0;
        int size = 100;
        Page<Group> groupPage;
        do {
            Pageable pageable = PageRequest.of(page, size);
            groupPage = groupService.findAll(pageable);
            List<Group> groupNames = groupPage.getContent();
            for (Group group : groupNames) {
                eventSourceServiceFactory.syncEventsForGroup(group);
            }
            page++;
        } while (groupPage.hasNext());
    }
}
