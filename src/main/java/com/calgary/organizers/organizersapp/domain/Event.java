package com.calgary.organizers.organizersapp.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * A Event.
 */
@Entity
@Table(name = "event")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "event_date")
    private ZonedDateTime event_date;

    @Column(name = "event_location")
    private String event_location;

    @Column(name = "event_description")
    private String event_description;

    @Column(name = "event_group_name")
    private String event_group_name;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Event id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getEvent_date() {
        return this.event_date;
    }

    public Event event_date(ZonedDateTime event_date) {
        this.setEvent_date(event_date);
        return this;
    }

    public void setEvent_date(ZonedDateTime event_date) {
        this.event_date = event_date;
    }

    public String getEvent_location() {
        return this.event_location;
    }

    public Event event_location(String event_location) {
        this.setEvent_location(event_location);
        return this;
    }

    public void setEvent_location(String event_location) {
        this.event_location = event_location;
    }

    public String getEvent_description() {
        return this.event_description;
    }

    public Event event_description(String event_description) {
        this.setEvent_description(event_description);
        return this;
    }

    public void setEvent_description(String event_description) {
        this.event_description = event_description;
    }

    public String getEvent_group_name() {
        return this.event_group_name;
    }

    public Event event_group_name(String event_group_name) {
        this.setEvent_group_name(event_group_name);
        return this;
    }

    public void setEvent_group_name(String event_group_name) {
        this.event_group_name = event_group_name;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event)) {
            return false;
        }
        return getId() != null && getId().equals(((Event) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Event{" +
            "id=" + getId() +
            ", event_date='" + getEvent_date() + "'" +
            ", event_location='" + getEvent_location() + "'" +
            ", event_description='" + getEvent_description() + "'" +
            ", event_group_name='" + getEvent_group_name() + "'" +
            "}";
    }
}
