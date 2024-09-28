package com.calgary.organizers.organizersapp.domain;

import static com.calgary.organizers.organizersapp.domain.EventTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.calgary.organizers.organizersapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EventTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Event.class);
        Event event1 = getEventSample1();
        Event event2 = new Event();
        assertThat(event1).isNotEqualTo(event2);

        event2.setId(event1.getId());
        assertThat(event1).isEqualTo(event2);

        event2 = getEventSample2();
        assertThat(event1).isNotEqualTo(event2);
    }
}
