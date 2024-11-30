package com.calgary.organizers.organizersapp.service;

import com.calgary.organizers.organizersapp.domain.Group;
import com.calgary.organizers.organizersapp.repository.GroupRepository;
import com.calgary.organizers.organizersapp.service.eventsource.MeetupService;
import com.calgary.organizers.organizersapp.service.oauth.JwtFlowProvider;
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
    private final JwtFlowProvider jwtFlowProvider;
    private final MeetupService meetupService;

    public GroupService(GroupRepository groupRepository, JwtFlowProvider jwtFlowProvider, MeetupService meetupService) {
        this.groupRepository = groupRepository;
        this.jwtFlowProvider = jwtFlowProvider;
        this.meetupService = meetupService;
    }

    /**
     * Save a group.
     *
     * @param group the entity to save.
     * @return the persisted entity.
     */
    public Group save(Group group) {
        LOG.debug("Request to save Group : {}", group);
        Group savedGroup = groupRepository.save(group);
        String accessToken = jwtFlowProvider.getAccessToken();
        meetupService.syncEventsForGroup(accessToken, savedGroup.getMeetup_group_name());
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
                if (group.getMeetup_group_name() != null) {
                    existingGroup.setMeetup_group_name(group.getMeetup_group_name());
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
        groupRepository.deleteById(id);
    }
}
