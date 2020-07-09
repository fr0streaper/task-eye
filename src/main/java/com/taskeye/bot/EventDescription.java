package com.taskeye.bot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EventDescription {

    private String eventType, target;

    @JsonCreator
    public EventDescription(@JsonProperty("eventType") String eventType,
                            @JsonProperty("target") String target){
        this.eventType = eventType;
        this.target = target;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTarget() {
        return target;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setTargets(String target) {
        this.target = target;
    }

}
