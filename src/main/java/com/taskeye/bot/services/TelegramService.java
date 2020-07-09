package com.taskeye.bot.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.taskeye.bot.BotData;
import com.taskeye.bot.TEMessage;
import com.taskeye.bot.TEMessageBuilder;
import com.taskeye.bot.message_text.TelegramMessageText;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TelegramService {

    private static final String API_TOKEN = BotData.TELEGRAM_API_TOKEN;
    private static final String API_URL = "https://api.telegram.org/bot" + API_TOKEN;

    private static final String WEBHOOK_URL = BotData.HOSTING_URL +
            "/telegram-webhook";

    private static final String SET_WEBHOOK_URL = API_URL +
            "/setWebhook?url={url}";
    private static final String ANSWER_CALLBACK_QUERY_URL = API_URL +
            "/answerCallbackQuery?callback_query_id={queryId}";
    private static final String SEND_MESSAGE_URL = API_URL +
            "/sendMessage?chat_id={chat_id}&text={text}&parse_mode={parse_mode}&reply_markup={keyboard}";
    private static final String EDIT_MESSAGE_URL = API_URL +
            "/editMessageText?chat_id={chat_id}&message_id={message_id}&text={text}&" +
            "parse_mode={parse_mode}&reply_markup={keyboard}";

    public static void setWebhook() {
        (new RestTemplate()).getForObject(SET_WEBHOOK_URL, String.class, WEBHOOK_URL);
    }

    public static void answerCallbackQuery(String queryId) {
        try {
            (new RestTemplate())
                    .getForObject(ANSWER_CALLBACK_QUERY_URL, String.class, queryId);
        } catch (Exception ignored) {}
    }

    public static void sendMessage(TEMessage message) {
        String keyboard = "";
        if (message.options != null) {
            keyboard = wrapInlineKeyboard(message);
        }

        Map<String, String> uriParameters = Map.ofEntries(
                Map.entry("chat_id", message.chatId),
                Map.entry("text", message.text),
                Map.entry("parse_mode", "HTML"),
                Map.entry("keyboard", keyboard)
        );

        try {
            (new RestTemplate()).getForObject(SEND_MESSAGE_URL, String.class, uriParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void editMessage(String messageId, TEMessage newMessage) {
        String keyboard = "";
        if (newMessage.options != null) {
            keyboard = wrapInlineKeyboard(newMessage);
        }

        Map<String, String> uriParameters = Map.ofEntries(
                Map.entry("chat_id", newMessage.chatId),
                Map.entry("message_id", messageId),
                Map.entry("text", newMessage.text),
                Map.entry("parse_mode", "HTML"),
                Map.entry("keyboard", keyboard)
        );

        try {
            (new RestTemplate()).getForObject(EDIT_MESSAGE_URL, String.class, uriParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void serviceMessage(String chatId, String type) {
        TEMessageBuilder builder = new TEMessageBuilder().chatId(chatId).platform("telegram");
        switch (type) {
            case "/start":
                builder.text(TelegramMessageText.getStart());
                break;
            case "/help":
                builder.text(TelegramMessageText.getHelp());
                break;
            case "/apology":
                builder.text(TelegramMessageText.getApology());
                break;
            case "/bruh":
                builder.text("*breathes in*\n" +
                        "<em><strong>UNLIMITED OVERENGINEERING WORKS</strong></em>" +
                        "\n<em>i apologize to anyone reading my code</em>");
                break;
            default:
                builder.text(TelegramMessageText.getError());
        }

        sendMessage(builder.build());
    }

    private static String wrapInlineKeyboard(TEMessage message) {
        List<String> wrappedOptions = new ArrayList<>();
        try {
            String keyboardButton;
            for (String option : message.options) {
                keyboardButton = (new ObjectMapper()).writeValueAsString(
                        new InlineKeyboardButton(option, message.sourceTTS + ":" + option));

                wrappedOptions.add("[" + keyboardButton + "]");
            }
            keyboardButton = (new ObjectMapper()).writeValueAsString(
                    new InlineKeyboardButton("cancel", "cancel"));

            wrappedOptions.add("[" + keyboardButton + "]");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }

        return "{\"inline_keyboard\":[" + String.join(",", wrappedOptions) + "]}";
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    private static class InlineKeyboardButton {

        private String text, callback_data;

        @JsonCreator
        public InlineKeyboardButton(@JsonProperty("text") String text,
                                    @JsonProperty("callback_data") String callback_data) {
            this.text = text;
            this.callback_data = callback_data;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getCallback_data() {
            return callback_data;
        }

        public void setCallback_data(String callback_data) {
            this.callback_data = callback_data;
        }

    }

}
