package com.calgary.organizers.organizersapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertGroupAllPropertiesEquals(Group expected, Group actual) {
        assertGroupAutoGeneratedPropertiesEquals(expected, actual);
        assertGroupAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertGroupAllUpdatablePropertiesEquals(Group expected, Group actual) {
        assertGroupUpdatableFieldsEquals(expected, actual);
        assertGroupUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertGroupAutoGeneratedPropertiesEquals(Group expected, Group actual) {
        assertThat(expected)
            .as("Verify Group auto generated properties")
            .satisfies(e -> assertThat(e.getId()).as("check id").isEqualTo(actual.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertGroupUpdatableFieldsEquals(Group expected, Group actual) {
        assertThat(expected)
            .as("Verify Group relevant properties")
            .satisfies(e -> assertThat(e.getName()).as("check name").isEqualTo(actual.getName()))
            .satisfies(e -> assertThat(e.getMeetup_group_name()).as("check meetup_group_name").isEqualTo(actual.getMeetup_group_name()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertGroupUpdatableRelationshipsEquals(Group expected, Group actual) {
        // empty method
    }
}
