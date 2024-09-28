package com.calgary.organizers.organizersapp.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class EventTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Event getEventSample1() {
        return new Event()
            .id(1L)
            .event_location("event_location1")
            .event_description("event_description1")
            .event_group_name("event_group_name1");
    }

    public static Event getEventSample2() {
        return new Event()
            .id(2L)
            .event_location("event_location2")
            .event_description("event_description2")
            .event_group_name("event_group_name2");
    }

    public static Event getEventRandomSampleGenerator() {
        return new Event()
            .id(longCount.incrementAndGet())
            .event_location(UUID.randomUUID().toString())
            .event_description(UUID.randomUUID().toString())
            .event_group_name(UUID.randomUUID().toString());
    }
}
