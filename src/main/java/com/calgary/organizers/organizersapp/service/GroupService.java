package com.calgary.organizers.organizersapp.service;

import com.calgary.organizers.organizersapp.domain.Event;
import com.calgary.organizers.organizersapp.domain.Group;
import com.calgary.organizers.organizersapp.enums.EventSource;
import com.calgary.organizers.organizersapp.repository.GroupRepository;
import com.calgary.organizers.organizersapp.service.eventsource.EventSourceServiceFactory;
import com.calgary.organizers.organizersapp.web.rest.dto.CheckUrlDto;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.calgary.organizers.organizersapp.domain.Group}.
 */
@Service
@Transactional
public class GroupService {

    private static final Logger LOG = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final EventService eventService;
    private final EventSourceServiceFactory eventSourceServiceFactory;

    public GroupService(GroupRepository groupRepository, EventService eventService, EventSourceServiceFactory eventSourceServiceFactory) {
        this.groupRepository = groupRepository;
        this.eventService = eventService;
        this.eventSourceServiceFactory = eventSourceServiceFactory;
    }

    /**
     * Save a group.
     *
     * @param group the entity to save.
     * @return the persisted entity.
     */
    @Transactional
    public Group save(Group group) {
        LOG.debug("Request to save Group : {}", group);
        eventSourceServiceFactory.verifyOrganizerParameters(group);
        Group savedGroup = groupRepository.save(group);
        eventSourceServiceFactory.syncEventsForGroup(savedGroup);
        return savedGroup;
    }

    /**
     * Update a group.
     *
     * @param group the entity to save.
     * @return the persisted entity.
     */
    public Group update(Group group) {
        LOG.debug("Request to update Group : {}", group);
        return groupRepository.save(group);
    }

    /**
     * Partially update a group.
     *
     * @param group the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Group> partialUpdate(Group group) {
        LOG.debug("Request to partially update Group : {}", group);

        return groupRepository
            .findById(group.getId())
            .map(existingGroup -> {
                if (group.getName() != null) {
                    existingGroup.setName(group.getName());
                }
                if (group.getOrganizerId() != null) {
                    existingGroup.setOrganizerId(group.getOrganizerId());
                }

                return existingGroup;
            })
            .map(groupRepository::save);
    }

    /**
     * Get all the groups.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<Group> findAll(Pageable pageable) {
        LOG.debug("Request to get all Groups");
        return groupRepository.findAll(pageable);
    }

    /**
     * Get one group by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Group> findOne(Long id) {
        LOG.debug("Request to get Group : {}", id);
        return groupRepository.findById(id);
    }

    /**
     * Delete the group by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Group : {}", id);
        Group group = groupRepository.findById(id).orElseThrow();
        List<Event> eventsOfGroup = eventService.getEventsForOrganizerId(group.getOrganizerId());
        eventService.deleteEvents(eventsOfGroup);
        groupRepository.deleteById(id);
    }

    public Group checkGroup(CheckUrlDto checkUrlDto) {
        URI uri = URI.create(checkUrlDto.url());
        String host = uri.getHost().toLowerCase();
        EventSource eventSource = EventSource.fromString(host);
        String organizerId = eventSourceServiceFactory.getOrganizerIdByUrl(eventSource, checkUrlDto.url());
        return eventSourceServiceFactory.getOrganizerByOrganizerId(eventSource, organizerId);
    }
}
