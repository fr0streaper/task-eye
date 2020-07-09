package com.taskeye.bot.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskeye.bot.TEMessageBuilder;
import com.taskeye.bot.TTSService;
import com.taskeye.bot.services.TelegramService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class TelegramController {

    /**
     * Handles all of the incoming webhook updates
     * @param updateContent Body of the incoming POST request
     * @return true if no problems were encountered when handling the update, false otherwise
     */
    @PostMapping("/telegram-webhook")
    public ResponseEntity onWebhookUpdate(@RequestBody String updateContent) {
        JsonNode update;
        try {
            update = (new ObjectMapper()).readTree(updateContent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (update.get("callback_query") != null) {
            String updateData = update.get("callback_query").get("data").asText();
            Matcher callbackDataMatcher = Pattern.compile("^(\\w+):(.+)$").matcher(updateData);

            String chatId = update.get("callback_query").get("message").get("chat").get("id").asText();
            if (callbackDataMatcher.find()) {
                String tts = callbackDataMatcher.group(1);
                String response = callbackDataMatcher.group(2);

                TelegramService.editMessage(update
                                .get("callback_query")
                                .get("message")
                                .get("message_id")
                                .asText(),
                        (new TEMessageBuilder())
                                .platform("telegram")
                                .chatId(chatId)
                                .text("<em>Selected</em> " + response)
                                .build());
                TTSService.continueFromStage(tts, "telegram", chatId, "callback", response);
            }
            else if (updateData.equals("cancel")) {
                TelegramService.editMessage(update
                                .get("callback_query")
                                .get("message")
                                .get("message_id")
                                .asText(),
                        (new TEMessageBuilder())
                                .platform("telegram")
                                .chatId(chatId)
                                .text("<em>Action cancelled</em> \uD83D\uDDFF")
                                .build());
            }
            else {
                TelegramService.serviceMessage(chatId, "error");
            }

            TelegramService.answerCallbackQuery(update.get("callback_query").get("id").asText());

            System.out.println("Callback query: [\n\n" + updateContent + "\n\n]");
        }
        else if (update.get("message") != null) {
            JsonNode message = update.get("message");
            String chatId = message.get("chat").get("id").asText();
            String messageText = message.get("text").asText();
            String tts;

            switch (messageText) {
                case "/start":
                case "/help":
                case "/apology":
                case "/bruh":
                    TelegramService.serviceMessage(chatId, messageText);
                    break;
                case "/add":
                case "/list":
                case "/remove":
                    tts = "trello";
                    TTSService.continueFromStage(tts,
                            "telegram",
                            chatId,
                            messageText.substring(1),
                            null);
                    break;
                default:
                    Matcher tokenMatcher = Pattern.compile("^/set_token\\s+(\\w+)\\s+(\\w+)$").matcher(messageText);
                    if (tokenMatcher.find()) {
                        tts = tokenMatcher.group(1).toLowerCase();
                        String token = tokenMatcher.group(2);

                        TTSService.continueFromStage(tts,
                                "telegram",
                                chatId,
                                "token",
                                token);
                    }
                    else {
                        TelegramService.serviceMessage(chatId, messageText);
                    }
            }

            System.out.println("Telegram message: [\n\n" + updateContent + "\n\n]");
        }
        else {
            System.out.println("Telegram update: [\n\n" + updateContent + "\n\n]");
        }

        return ResponseEntity.ok().body(null);
    }

}
