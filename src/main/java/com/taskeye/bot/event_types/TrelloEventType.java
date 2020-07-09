package com.taskeye.bot.event_types;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public enum TrelloEventType {

    CARD_CREATED("createCard",
            node -> true,
            (node, target) -> true,
            node -> {
                JsonNode data = node.get("action").get("data");
                return String.format("Card \"%s\" added to \"%s\"",
                        data.get("card").get("name").asText(),
                        data.get("list").get("name").asText());
            }),

    CARD_MOVED_TO_LIST("updateCard",
            node -> node.get("action").get("data").get("listBefore") != null &&
                    node.get("action").get("data").get("listAfter") != null,
            (node, target) -> true,
            node -> {
                JsonNode data = node.get("action").get("data");
                return String.format("Card \"%s\" moved from \"%s\" to \"%s\"",
                        data.get("card").get("name").asText(),
                        data.get("listBefore").get("name").asText(),
                        data.get("listAfter").get("name").asText());
            }),

    CARD_COMMENTED("commentCard",
            node -> true,
            (node, target) -> node.get("action").get("data").get("text").asText().contains("@" + target),
            node -> {
                JsonNode data = node.get("action").get("data");
                return String.format("Comment added to the \"%s\" card:\n%s",
                        data.get("card").get("name").asText(),
                        data.get("text").asText());
            }),

    CARD_MEMBER_ADDED("addMemberToCard",
            node -> true,
            (node, target) -> true,
            node -> {
                JsonNode data = node.get("action").get("data");
                return String.format("Member %s added to the \"%s\" card",
                        data.get("member").get("name").asText(),
                        data.get("card").get("name").asText());
            }),

    CARD_MEMBER_REMOVED("removeMemberFromCard",
            node -> true,
            (node, target) -> true,
            node -> {
                JsonNode data = node.get("action").get("data");
                return String.format("Member %s removed from the \"%s\" card",
                        data.get("member").get("name").asText(),
                        data.get("card").get("name").asText());
            }),

    CARD_ATTACHMENT_ADDED("addAttachmentToCard",
            node -> true,
            (node, target) -> true,
            node -> {
                JsonNode data = node.get("action").get("data");
                return String.format("Attachment \"%s\" added to the \"%s\" card",
                        data.get("attachment").get("name").asText(),
                        data.get("card").get("name").asText());
            }),

    CARD_ARCHIVED("updateCard",
            node -> node.get("action").get("data").get("card").get("closed") != null &&
                    node.get("action").get("data").get("card").get("closed").asText().equals("true") &&
                    node.get("action").get("data").get("old").get("closed") != null &&
                    node.get("action").get("data").get("old").get("closed").asText().equals("false"),
            (node, target) -> false,
            node -> {
                JsonNode data = node.get("action").get("data");
                return String.format("Card \"%s\" in \"%s\" archived",
                        data.get("card").get("name").asText(),
                        data.get("list").get("name").asText());
            }),

    CARD_LABEL_ADDED("addLabelToCard",
            node -> true,
            (node, target) -> node.get("action").get("data").get("text").asText().equals(target),
            node -> {
                JsonNode data = node.get("action").get("data");
                return String.format("Label \"%s\" added to the \"%s\" card",
                        data.get("text").asText(),
                        data.get("card").get("name").asText());
            }),

    OTHER("", node -> true, (node, target) -> true, node -> "Something happened (^^ゞ");

    private final String actionType;
    private final Predicate<JsonNode> matchingCondition;
    private final BiPredicate<JsonNode, String> targetCondition;
    private final Function<JsonNode, String> descriptionGenerator;

    TrelloEventType(String actionType,
                    Predicate<JsonNode> matchingCondition,
                    BiPredicate<JsonNode, String> targetCondition,
                    Function<JsonNode, String> descriptionGenerator) {
        this.actionType = actionType;
        this.matchingCondition = matchingCondition;
        this.targetCondition = targetCondition;
        this.descriptionGenerator = descriptionGenerator;
    }

    public static TrelloEventType getType(JsonNode update) {
        return Arrays.stream(TrelloEventType.values())
                .filter(type -> type != OTHER &&
                        update.get("action").get("type").asText().equals(type.actionType) &&
                        type.matchingCondition.test(update))
                .findAny()
                .orElse(OTHER);
    }

    public static boolean matchTarget(JsonNode update, String target) {
        return target == null || getType(update).targetCondition.test(update, target);
    }

    public String getDescription(JsonNode update) {
        return descriptionGenerator.apply(update);
    }
}
