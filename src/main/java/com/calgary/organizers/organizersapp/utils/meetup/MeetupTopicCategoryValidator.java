package com.calgary.organizers.organizersapp.utils.meetup;

import com.calgary.organizers.organizersapp.config.DomainConfig;
import com.calgary.organizers.organizersapp.service.eventsource.meetup.InvalidGroupParameterException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "domain.meetup", name = "required-topic-category")
public class MeetupTopicCategoryValidator implements MeetupDomainValidator {

    private final String topicCategory;

    public MeetupTopicCategoryValidator(DomainConfig props) {
        this.topicCategory = props.getMeetup().getRequiredTopicCategory();
    }

    @Override
    public void validate(JsonNode rootNode) {
        var requestTopicCategory = rootNode.at("/data/groupByUrlname/topicCategory/name").asText();
        if (!Objects.equals(requestTopicCategory, topicCategory)) {
            throw new InvalidGroupParameterException(
                "Group from wrong topic category provided. Provided '%s'".formatted(requestTopicCategory)
            );
        }
    }
}
