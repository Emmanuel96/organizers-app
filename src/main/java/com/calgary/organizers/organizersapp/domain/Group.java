package com.calgary.organizers.organizersapp.domain;

import com.calgary.organizers.organizersapp.enums.EventSource;
import jakarta.persistence.*;
import java.io.Serializable;

/**
 * A Group.
 */
@Entity
@Table(name = "jhi_group")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "meetup_group_name")
    private String meetup_group_name;

    @Column(name = "event_source")
    @Enumerated(EnumType.STRING)
    private EventSource eventSource;

    @Column(name = "eventbrite_organizer_id")
    private String eventbriteOrganizerId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Group id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Group name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeetup_group_name() {
        return this.meetup_group_name;
    }

    public Group meetup_group_name(String meetup_group_name) {
        this.setMeetup_group_name(meetup_group_name);
        return this;
    }

    public void setMeetup_group_name(String meetup_group_name) {
        this.meetup_group_name = meetup_group_name;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    public EventSource getEventSource() {
        return eventSource;
    }

    public void setEventSource(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    public String getEventbriteOrganizerId() {
        return eventbriteOrganizerId;
    }

    public void setEventbriteOrganizerId(String eventbriteOrganizerId) {
        this.eventbriteOrganizerId = eventbriteOrganizerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Group)) {
            return false;
        }
        return getId() != null && getId().equals(((Group) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Group{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", meetup_group_name='" + getMeetup_group_name() + "'" +
            "}";
    }
}
