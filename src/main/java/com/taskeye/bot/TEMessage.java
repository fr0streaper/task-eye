package com.taskeye.bot;

import java.util.List;

public class TEMessage {

    public String chatId, text, platform, sourceTTS;
    public List<String> options;

    public TEMessage(String chatId, String text, String platform, String sourceTTS, List<String> options)
            throws IllegalArgumentException {
        if (chatId == null || chatId.isEmpty() ||
                text == null || text.isEmpty() ||
                platform == null || platform.isEmpty()) {
            throw new IllegalArgumentException("TEMessage construction failed: chatId, text and " +
                    "platform cannot be null or empty");
        }

        this.chatId = chatId;
        this.text = text;
        this.platform = platform;
        this.sourceTTS = sourceTTS;
        this.options = options;
    }
}
