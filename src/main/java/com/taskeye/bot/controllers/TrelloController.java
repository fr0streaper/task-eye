package com.taskeye.bot.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskeye.bot.*;
import com.taskeye.bot.event_types.TrelloEventType;
import com.taskeye.bot.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
public class TrelloController {

    /**
     * Returns response code 200 to any HEAD requests. Trello performs such a check whenever
     * a webhook is created, hence an endpoint
     * @return true
     */
    @RequestMapping(path = "/trello-webhook", method = RequestMethod.HEAD)
    public ResponseEntity webhookRegisterResponse() {
        return ResponseEntity.ok().body(null);
    }

    /**
     * Handles all of the incoming webhook updates
     * @param updateContent Body of the incoming POST request
     * @param platform Platform (e.g. Telegram) to which the update is sent. Manually set at webhook creation
     * @param id ID of the user on the specified platform. Manually set at webhook creation
     * @param event_desc Description of the event being monitored, responsible for filtering out
     *                   extraneous updates. Manually set at webhook creation
     * @return Response code 200 if no problems were encountered when handling the update, 400 otherwise
     */
    @PostMapping("/trello-webhook")
    public ResponseEntity onWebhookUpdate(@RequestBody String updateContent,
                                          @RequestParam(value = "platform", required = true) String platform,
                                          @RequestParam(value = "id", required = true) String id,
                                          @RequestParam(value = "event_desc", required = true) String event_desc) {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode update;
        EventDescription eventDescription;
        try {
            update = objectMapper.readTree(updateContent);
            eventDescription = objectMapper.readValue(URLDecoder.decode(event_desc, StandardCharsets.UTF_8), EventDescription.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (isUpdateMatching(update, eventDescription)) {
            MessageService.dispatch((new TEMessageBuilder())
                    .platform(platform)
                    .chatId(id)
                    .text("Ｔｒｅｌｌｏ" +
                            String.format(" 「%s」 ", update.get("model").get("name").asText()) +
                            "\n" +
                            TrelloEventType.valueOf(eventDescription.getEventType()).getDescription(update))
                    .build());
        }

        System.out.println("Trello update: [\n\n" + updateContent + "\n\n]");

        return ResponseEntity.ok().body(null);
    }

    private static boolean isUpdateMatching(JsonNode update, EventDescription eventDescription) {
        return TrelloEventType.getType(update).equals(TrelloEventType.valueOf(eventDescription.getEventType())) &&
                TrelloEventType.matchTarget(update, eventDescription.getTarget());
    }

}
