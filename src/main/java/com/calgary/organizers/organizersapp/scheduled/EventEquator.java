package com.calgary.organizers.organizersapp.scheduled;

import com.calgary.organizers.organizersapp.domain.Event;
import java.util.Objects;
import org.apache.commons.collections4.Equator;

public class EventEquator implements Equator<Event> {

    @Override
    public boolean equate(Event o1, Event o2) {
        return Objects.equals(o1.getEventId(), o2.getEventId());
    }

    @Override
    public int hash(Event o) {
        return Objects.hash(o.getEventId());
    }
}
