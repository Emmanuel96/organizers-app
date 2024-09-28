package com.calgary.organizers.organizersapp.repository;

import com.calgary.organizers.organizersapp.domain.Event;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Event entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {}
