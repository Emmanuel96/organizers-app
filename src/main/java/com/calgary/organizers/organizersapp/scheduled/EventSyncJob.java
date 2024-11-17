package com.calgary.organizers.organizersapp.scheduled;

import com.calgary.organizers.organizersapp.domain.Group;
import com.calgary.organizers.organizersapp.service.GroupService;
import com.calgary.organizers.organizersapp.service.eventsource.MeetupService;
import com.calgary.organizers.organizersapp.service.oauth.JwtFlowProvider;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EventSyncJob {

    private final MeetupService meetupService;
    private final GroupService groupService;
    private final JwtFlowProvider jwtFlowProvider;

    public EventSyncJob(MeetupService meetupService, GroupService groupService, JwtFlowProvider jwtFlowProvider) {
        this.meetupService = meetupService;
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
        new ArrayList<>();
        do {
            Pageable pageable = PageRequest.of(page, size);
            groupPage = groupService.findAll(pageable);
            List<Group> groupNames = groupPage.getContent();
            for (Group group : groupNames) {
                String groupName = group.getMeetup_group_name();
                meetupService.syncEventsForGroup(accessToken, groupName);
            }
            page++;
        } while (groupPage.hasNext());
    }
}
