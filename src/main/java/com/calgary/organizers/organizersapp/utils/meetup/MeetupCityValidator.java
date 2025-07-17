package com.calgary.organizers.organizersapp.utils.meetup;

import com.calgary.organizers.organizersapp.service.eventsource.meetup.InvalidGroupParameterException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "domain.meetup", name = "city")
public class MeetupCityValidator implements MeetupDomainValidator {

    @Override
    public void validate(JsonNode rootNode) {
        var city = rootNode.at("/data/groupByUrlname/city").asText();
        if (!city.equals("Calgary")) {
            throw new InvalidGroupParameterException("Group from wrong city provided. Provided '%s'".formatted(city));
        }
    }
}
