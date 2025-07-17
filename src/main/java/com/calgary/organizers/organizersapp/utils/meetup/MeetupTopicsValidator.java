package com.calgary.organizers.organizersapp.utils.meetup;

import com.calgary.organizers.organizersapp.config.DomainConfig;
import com.calgary.organizers.organizersapp.service.eventsource.meetup.InvalidGroupParameterException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "domain.meetup", name = "required-topics[0]")
public class MeetupTopicsValidator implements MeetupDomainValidator {

    private final List<String> requiredTopics;

    public MeetupTopicsValidator(DomainConfig props) {
        this.requiredTopics = props.getMeetup().getRequiredTopics();
    }

    @Override
    public void validate(JsonNode rootNode) {
        JsonNode topicsNode = rootNode.at("/data/groupByUrlname/topics");
        List<String> actualNames = new ArrayList<>();
        for (JsonNode topic : topicsNode) {
            actualNames.add(topic.get("name").asText());
        }

        for (String required : requiredTopics) {
            if (!actualNames.contains(required)) {
                throw new InvalidGroupParameterException("Required topic not found. Expected '%s'".formatted(required));
            }
        }
    }
}
