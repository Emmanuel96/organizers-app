package com.calgary.organizers.organizersapp.utils.meetup;

import com.fasterxml.jackson.databind.JsonNode;

public interface MeetupDomainValidator {
    void validate(JsonNode rootNode);
}
