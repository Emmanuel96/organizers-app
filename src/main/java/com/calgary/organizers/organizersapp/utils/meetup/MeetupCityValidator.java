package com.calgary.organizers.organizersapp.utils.meetup;

import com.calgary.organizers.organizersapp.config.DomainConfig;
import com.calgary.organizers.organizersapp.service.eventsource.meetup.InvalidGroupParameterException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "domain.meetup", name = "city")
public class MeetupCityValidator implements MeetupDomainValidator {

    private final String city;

    public MeetupCityValidator(DomainConfig props) {
        this.city = props.getMeetup().getCity();
    }

    @Override
    public void validate(JsonNode rootNode) {
        var requestCity = rootNode.at("/data/groupByUrlname/city").asText();
        if (!Objects.equals(requestCity, city)) {
            throw new InvalidGroupParameterException("Group from wrong city provided. Provided '%s'".formatted(city));
        }
    }
}
