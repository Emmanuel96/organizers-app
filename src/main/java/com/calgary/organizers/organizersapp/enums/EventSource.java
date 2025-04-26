package com.calgary.organizers.organizersapp.enums;

public enum EventSource {
    EVENTBRITE("www.eventbrite.com"),
    MEET_UP("www.meetup.com"),
    PLATFORM("platform"),
    OTHER("other");

    private final String host;

    EventSource(String host) {
        this.host = host;
    }

    /**
     * @return the host string associated with this source, or null for OTHER.
     */
    public String getHost() {
        return host;
    }

    public static EventSource fromString(String input) {
        if (input == null) {
            return OTHER;
        }
        String lower = input.toLowerCase();
        for (EventSource src : values()) {
            if (!src.host.isEmpty() && lower.contains(src.host.toLowerCase())) {
                return src;
            }
        }
        return OTHER;
    }
}
