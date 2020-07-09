package com.taskeye.bot.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskeye.bot.BotData;
import com.taskeye.bot.EventDescription;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TrelloService {

    private static final String API_KEY = BotData.TRELLO_API_KEY;
    private static final String API_URL = "https://api.trello.com/1/members/me";

    private static final String WEBHOOK_URL = BotData.HOSTING_URL +
            "/trello-webhook?platform=%s&id=%s&event_desc=%s";

    private static final String GET_BOARD_URL = "https://api.trello.com/1" + "/boards/{id}?key={key}&token={token}";
    private static final String GET_BOARDS_URL = API_URL + "/boards?fields=name,closed&key={key}&token={token}";
    private static final String SET_WEBHOOK_URL = "https://api.trello.com/1/tokens/{token}/webhooks/?key={key}";
    private static final String GET_WEBHOOKS_URL = API_URL + "/tokens?webhooks=true&key={key}&token={token}";
    private static final String DELETE_WEBHOOK_URL = "https://api.trello.com/1/webhooks/{webhookId}?key={key}&token={token}";

    // Interaction with Trello's REST API

    public static String getBoard(String token, String idModel) {
        Map<String, String> uriParameters = Map.ofEntries(
                Map.entry("key", API_KEY),
                Map.entry("token", token),
                Map.entry("id", idModel)
        );

        String boardData = (new RestTemplate()).getForObject(GET_BOARD_URL, String.class, uriParameters);

        try {
            return (new ObjectMapper()).readTree(boardData).get("name").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Map<String, String> getBoards(String token) {
        Map<String, String> uriParameters = Map.ofEntries(
                Map.entry("key", API_KEY),
                Map.entry("token", token)
        );

        String boardData = (new RestTemplate()).getForObject(GET_BOARDS_URL, String.class, uriParameters);

        Map<String, String> result = new HashMap<>();
        Iterator<JsonNode> data;
        try {
            data = (new ObjectMapper()).readTree(boardData).elements();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
        while (data.hasNext()) {
            JsonNode board = data.next();

            if (board.get("closed").asText().equals("false")) {
                result.put(board.get("name").asText(), board.get("id").asText());
            }
        }

        return result;
    }

    public static void setWebhook(String token,
                                  String idModel,
                                  String platform,
                                  String id,
                                  EventDescription eventDescription) {
        String eventDescriptionJson = null;
        String webhookData = null;
        try {
            eventDescriptionJson = URLEncoder.encode(
                    (new ObjectMapper()).writeValueAsString(eventDescription),
                    StandardCharsets.UTF_8);

            String webhookDescription = eventDescription.getEventType().replace('_', ' ').toLowerCase();
            if (eventDescription.getTarget() != null) {
                webhookDescription += "「" + eventDescription.getTarget() + "」";
            }

            webhookData = (new ObjectMapper())
                    .writeValueAsString(new WebhookData(
                            String.format(WEBHOOK_URL, platform, id, eventDescriptionJson),
                            webhookDescription,
                            idModel));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(webhookData, httpHeaders);

        Map<String, String> uriParameters = Map.ofEntries(
                Map.entry("key", API_KEY),
                Map.entry("token", token)
        );

        (new RestTemplate()).postForObject(
                SET_WEBHOOK_URL,
                request,
                String.class,
                uriParameters);
    }

    public static Map<String, String> getWebhooks(String token) {
        Map<String, String> uriParameters = Map.ofEntries(
                Map.entry("key", API_KEY),
                Map.entry("token", token)
        );

        String webhookData = (new RestTemplate()).getForObject(GET_WEBHOOKS_URL,
                String.class, uriParameters);

        Map<String, String> result = new HashMap<>();
        Iterator<JsonNode> data;
        try {
            data = (new ObjectMapper()).readTree(webhookData).elements();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
        while (data.hasNext()) {
            Iterator<JsonNode> webhooks = data.next().get("webhooks").elements();

            while (webhooks.hasNext()) {
                JsonNode webhook = webhooks.next();
                result.put("「" + getBoard(token, webhook.get("idModel").asText()) + "」 " + webhook.get("description").asText(), webhook.get("id").asText());
            }
        }

        return result;
    }

    public static void deleteWebhook(String token, String webhookId) {
        Map<String, String> uriParameters = Map.ofEntries(
                Map.entry("key", API_KEY),
                Map.entry("token", token),
                Map.entry("webhookId", webhookId)
        );

        (new RestTemplate()).delete(DELETE_WEBHOOK_URL, uriParameters);
        System.out.println("Deleted Trello webhook: " + webhookId);
    }

    // Miscellaneous

    private static class WebhookData {

        private String callbackURL, description, idModel;

        @JsonCreator
        public WebhookData(@JsonProperty("callbackURL") String callbackURL,
                           @JsonProperty("description") String description,
                           @JsonProperty("idModel") String idModel) {
            this.callbackURL = callbackURL;
            this.description = description;
            this.idModel = idModel;
        }

        public String getCallbackURL() {
            return callbackURL;
        }

        public String getDescription() {
            return description;
        }

        public String getIdModel() {
            return idModel;
        }

        public void setCallbackURL(String callbackURL) {
            this.callbackURL = callbackURL;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setIdModel(String idModel) {
            this.idModel = idModel;
        }
    }

}
