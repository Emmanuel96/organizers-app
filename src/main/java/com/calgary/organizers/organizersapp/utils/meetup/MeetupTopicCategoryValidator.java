package com.calgary.organizers.organizersapp.utils.meetup;

import com.calgary.organizers.organizersapp.service.eventsource.meetup.InvalidGroupParameterException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "domain.meetup", name = "required-topic-category")
public class MeetupTopicCategoryValidator implements MeetupDomainValidator {

    @Override
    public void validate(JsonNode rootNode) {
        var topicCategory = rootNode.at("/data/groupByUrlname/topicCategory/name").asText();
        if (!topicCategory.equals("Technology")) {
            throw new InvalidGroupParameterException("Group from wrong topic category provided. Provided '%s'".formatted(topicCategory));
        }
    }
}
