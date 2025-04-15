package com.calgary.organizers.organizersapp.scheduled;

import com.calgary.organizers.organizersapp.domain.Group;
import com.calgary.organizers.organizersapp.service.GroupService;
import com.calgary.organizers.organizersapp.service.eventsource.eventbrite.EventbriteService;
import com.calgary.organizers.organizersapp.service.eventsource.meetup.MeetupService;
import com.calgary.organizers.organizersapp.service.oauth.JwtFlowProvider;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EventSyncJob {

    private final MeetupService meetupService;
    private final EventbriteService eventbriteService;
    private final GroupService groupService;
    private final JwtFlowProvider jwtFlowProvider;

    public EventSyncJob(
        MeetupService meetupService,
        EventbriteService eventbriteService,
        GroupService groupService,
        JwtFlowProvider jwtFlowProvider
    ) {
        this.meetupService = meetupService;
        this.eventbriteService = eventbriteService;
        this.groupService = groupService;
        this.jwtFlowProvider = jwtFlowProvider;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncEvents() {
        //TODO: token could expire if is very long, need token cache.
        String accessToken = jwtFlowProvider.getAccessToken();
        int page = 0;
        int size = 100;
        Page<Group> groupPage;
        do {
            Pageable pageable = PageRequest.of(page, size);
            groupPage = groupService.findAll(pageable);
            List<Group> groupNames = groupPage.getContent();
            for (Group group : groupNames) {
                if (Objects.nonNull(group.getMeetup_group_name())) {
                    meetupService.syncEventsForGroup(accessToken, group.getMeetup_group_name());
                }
                //TODO: Maybe we need to split this job into two separate ones.
                if (Objects.nonNull(group.getEventbriteOrganizerId())) {
                    eventbriteService.syncEventsForGroup(group.getEventbriteOrganizerId());
                }
            }
            page++;
        } while (groupPage.hasNext());
    }
}
