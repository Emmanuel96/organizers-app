package com.calgary.organizers.organizersapp.web.rest;

import static com.calgary.organizers.organizersapp.domain.GroupAsserts.*;
import static com.calgary.organizers.organizersapp.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.calgary.organizers.organizersapp.IntegrationTest;
import com.calgary.organizers.organizersapp.domain.Group;
import com.calgary.organizers.organizersapp.repository.GroupRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link GroupResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class GroupResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_MEETUP_GROUP_NAME = "AAAAAAAAAA";
    private static final String UPDATED_MEETUP_GROUP_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/groups";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGroupMockMvc;

    private Group group;

    private Group insertedGroup;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Group createEntity() {
        return new Group().name(DEFAULT_NAME).meetup_group_name(DEFAULT_MEETUP_GROUP_NAME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Group createUpdatedEntity() {
        return new Group().name(UPDATED_NAME).meetup_group_name(UPDATED_MEETUP_GROUP_NAME);
    }

    @BeforeEach
    public void initTest() {
        group = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedGroup != null) {
            groupRepository.delete(insertedGroup);
            insertedGroup = null;
        }
    }

    @Test
    @Transactional
    void createGroup() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Group
        var returnedGroup = om.readValue(
            restGroupMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(group)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Group.class
        );

        // Validate the Group in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertGroupUpdatableFieldsEquals(returnedGroup, getPersistedGroup(returnedGroup));

        insertedGroup = returnedGroup;
    }

    @Test
    @Transactional
    void createGroupWithExistingId() throws Exception {
        // Create the Group with an existing ID
        group.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGroupMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(group)))
            .andExpect(status().isBadRequest());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllGroups() throws Exception {
        // Initialize the database
        insertedGroup = groupRepository.saveAndFlush(group);

        // Get all the groupList
        restGroupMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(group.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].meetup_group_name").value(hasItem(DEFAULT_MEETUP_GROUP_NAME)));
    }

    @Test
    @Transactional
    void getGroup() throws Exception {
        // Initialize the database
        insertedGroup = groupRepository.saveAndFlush(group);

        // Get the group
        restGroupMockMvc
            .perform(get(ENTITY_API_URL_ID, group.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(group.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.meetup_group_name").value(DEFAULT_MEETUP_GROUP_NAME));
    }

    @Test
    @Transactional
    void getNonExistingGroup() throws Exception {
        // Get the group
        restGroupMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingGroup() throws Exception {
        // Initialize the database
        insertedGroup = groupRepository.saveAndFlush(group);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the group
        Group updatedGroup = groupRepository.findById(group.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedGroup are not directly saved in db
        em.detach(updatedGroup);
        updatedGroup.name(UPDATED_NAME).meetup_group_name(UPDATED_MEETUP_GROUP_NAME);

        restGroupMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedGroup.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedGroup))
            )
            .andExpect(status().isOk());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedGroupToMatchAllProperties(updatedGroup);
    }

    @Test
    @Transactional
    void putNonExistingGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(put(ENTITY_API_URL_ID, group.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(group)))
            .andExpect(status().isBadRequest());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(group))
            )
            .andExpect(status().isBadRequest());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(group)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateGroupWithPatch() throws Exception {
        // Initialize the database
        insertedGroup = groupRepository.saveAndFlush(group);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the group using partial update
        Group partialUpdatedGroup = new Group();
        partialUpdatedGroup.setId(group.getId());

        partialUpdatedGroup.name(UPDATED_NAME);

        restGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGroup.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGroup))
            )
            .andExpect(status().isOk());

        // Validate the Group in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGroupUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedGroup, group), getPersistedGroup(group));
    }

    @Test
    @Transactional
    void fullUpdateGroupWithPatch() throws Exception {
        // Initialize the database
        insertedGroup = groupRepository.saveAndFlush(group);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the group using partial update
        Group partialUpdatedGroup = new Group();
        partialUpdatedGroup.setId(group.getId());

        partialUpdatedGroup.name(UPDATED_NAME).meetup_group_name(UPDATED_MEETUP_GROUP_NAME);

        restGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGroup.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedGroup))
            )
            .andExpect(status().isOk());

        // Validate the Group in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertGroupUpdatableFieldsEquals(partialUpdatedGroup, getPersistedGroup(partialUpdatedGroup));
    }

    @Test
    @Transactional
    void patchNonExistingGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, group.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(group))
            )
            .andExpect(status().isBadRequest());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(group))
            )
            .andExpect(status().isBadRequest());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamGroup() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        group.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGroupMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(group)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Group in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteGroup() throws Exception {
        // Initialize the database
        insertedGroup = groupRepository.saveAndFlush(group);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the group
        restGroupMockMvc
            .perform(delete(ENTITY_API_URL_ID, group.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return groupRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Group getPersistedGroup(Group group) {
        return groupRepository.findById(group.getId()).orElseThrow();
    }

    protected void assertPersistedGroupToMatchAllProperties(Group expectedGroup) {
        assertGroupAllPropertiesEquals(expectedGroup, getPersistedGroup(expectedGroup));
    }

    protected void assertPersistedGroupToMatchUpdatableProperties(Group expectedGroup) {
        assertGroupAllUpdatablePropertiesEquals(expectedGroup, getPersistedGroup(expectedGroup));
    }
}
