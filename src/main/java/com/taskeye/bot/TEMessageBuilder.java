package com.taskeye.bot;

import java.util.List;

public class TEMessageBuilder {

    private String chatId, text, platform, sourceTTS;
    private List<String> options;

    public TEMessageBuilder() {
    }

    public TEMessageBuilder chatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public TEMessageBuilder text(String text) {
        this.text = text;
        return this;
    }

    public TEMessageBuilder platform(String platform) {
        this.platform = platform;
        return this;
    }

    public TEMessageBuilder sourceTTS(String sourceTTS) {
        this.sourceTTS = sourceTTS;
        return this;
    }

    public TEMessageBuilder options(List<String> options) {
        if (options != null) {
            this.options = List.copyOf(options);
        }
        return this;
    }

    public TEMessage build() throws IllegalArgumentException {
        return new TEMessage(chatId, text, platform, sourceTTS, options);
    }

}
