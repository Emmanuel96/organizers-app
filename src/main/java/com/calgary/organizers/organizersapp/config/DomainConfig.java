package com.calgary.organizers.organizersapp.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "domain")
public class DomainConfig {

    private final Meetup meetup = new Meetup();
    private final Eventbrite eventbrite = new Eventbrite();

    public Meetup getMeetup() {
        return meetup;
    }

    public Eventbrite getEventbrite() {
        return eventbrite;
    }

    public static class Meetup {

        /**
         * If set, only this city is allowed.
         */
        private String city;

        /**
         * If non‑empty, each of these must appear in the "topics" array.
         */
        private List<String> requiredTopics = new ArrayList<>();

        /**
         * If set, only this topic-category is allowed.
         */
        private String requiredTopicCategory;

        // —— getters & setters —— //

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public List<String> getRequiredTopics() {
            return requiredTopics;
        }

        public void setRequiredTopics(List<String> requiredTopics) {
            this.requiredTopics = requiredTopics;
        }

        public String getRequiredTopicCategory() {
            return requiredTopicCategory;
        }

        public void setRequiredTopicCategory(String requiredTopicCategory) {
            this.requiredTopicCategory = requiredTopicCategory;
        }
    }

    public static class Eventbrite {
        // add Eventbrite‑specific properties here as needed,
        // e.g. apiKey, baseUrl, etc.

        // Example placeholder:
        // private String apiKey;
        //
        // public String getApiKey() { return apiKey; }
        // public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    }
}
