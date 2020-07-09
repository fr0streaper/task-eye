package com.taskeye.bot.services;

import com.taskeye.bot.BotData;
import com.taskeye.bot.EventDescription;
import com.taskeye.bot.MessageService;
import com.taskeye.bot.TEMessageBuilder;
import com.taskeye.bot.event_types.TrelloEventType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrelloConfigService {

    private static final Map<String, Map<String, String>> userStates = new HashMap<>();
    private static final Map<String, String> userTokens = new HashMap<>();

    private static void setStage(String userId, UIStage stage) {
        userStates.get(userId).replace("stage", stage.toString());
    }

    private static void sendMessage(TEMessageBuilder builder, String text, List<String> options) {
        MessageService.dispatch(builder.text(text).options(options).build());
    }

    public static void continueFromStage(String platform, String chatId, String stage, String response) {
        String userId = platform + "/" + chatId;
        TEMessageBuilder messageBase = (new TEMessageBuilder())
                .platform(platform)
                .chatId(chatId)
                .sourceTTS("trello");

        if (stage.equals("token")) {
            userTokens.put(userId, response);
        }
        if (!userStates.containsKey(userId) && !stage.matches("add|list|remove")) {
            if (!stage.equals("token")) {
                sendMessage(messageBase,
                        "Sorry, the tracker creation session seems to have expired. Please start over",
                        null);
            }
            return;
        }

        if (stage.matches("add|list|remove")) {
            stage = stage.toUpperCase().concat("_START");
        }
        else {

            stage = userStates.get(userId).get("stage");
        }
        UIStage uiStage = UIStage.valueOf(stage);

        switch (uiStage) {
            case ADD_START:
            case LIST_START:
            case REMOVE_START:
                userStates.remove(userId);
                userStates.put(userId,
                        new HashMap<>(Map.of("stage", uiStage.getNext().toString())));

                if (!userTokens.containsKey(userId)) {
                    requestToken(platform, chatId);
                }
                else {
                    continueFromStage(platform, chatId, "skip", null);
                }

                break;

            case ADD_TOKEN_INPUT:
                sendMessage(messageBase,
                        "Please select the board to track:",
                        List.copyOf(TrelloService.getBoards(userTokens.get(userId)).keySet()));
                setStage(userId, uiStage.getNext());
                break;

            case ADD_BOARD_SELECTION:
                userStates.get(userId).put("idModel",
                        TrelloService.getBoards(userTokens.get(userId)).get(response));
                sendMessage(messageBase,
                        "Please choose the type of notifications to get:",
                        Arrays.stream(TrelloEventType.values())
                                .map(type -> type
                                        .toString()
                                        .replace('_', ' ')
                                        .toLowerCase())
                                .collect(Collectors.toList()));
                setStage(userId, uiStage.getNext());
                break;

            case ADD_EVENT_TYPE_SELECTION:
                userStates.get(userId).put("eventType", response.toUpperCase().replace(' ', '_'));
                Map<String, String> state = userStates.get(userId);
                try {
                    TrelloService.setWebhook(
                            userTokens.get(userId),
                            state.get("idModel"),
                            platform,
                            chatId,
                            new EventDescription(state.get("eventType"), null));
                    sendMessage(messageBase, "Tracker successfully created \uD83D\uDDFF", null);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(messageBase, "Something went wrong during tracker creation. Perhaps you already have one of these? \uD83E\uDD14", null);
                }
                setStage(userId, uiStage.getNext());
                break;

            case LIST_TOKEN_INPUT:
                userStates.get(userId).put("token", response);
                try {
                    sendMessage(messageBase,
                            "Trackers you have set up for Trello:\n\n" + String.join("\n",
                                    TrelloService.getWebhooks(userTokens.get(userId)).keySet()),
                            null);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(messageBase, "Something went wrong during tracker inspection. Please try again later", null);
                }
                setStage(userId, uiStage.getNext());
                break;

            case REMOVE_TOKEN_INPUT:
                sendMessage(messageBase,
                        "Please select the tracker to remove:",
                        List.copyOf(TrelloService.getWebhooks(userTokens.get(userId)).keySet()));
                setStage(userId, uiStage.getNext());
                break;

            case REMOVE_TRACKER_SELECTION:
                userStates.get(userId).put("webhookId",
                        TrelloService.getWebhooks(userTokens.get(userId)).get(response));
                try {
                    TrelloService.deleteWebhook(userTokens.get(userId),
                            userStates.get(userId).get("webhookId"));
                    sendMessage(messageBase,
                            "Tracker successfully deleted \uD83D\uDDFF",
                            null);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(messageBase, "Something went wrong during tracker deletion. Please try again later", null);
                }
                setStage(userId, uiStage.getNext());
                break;

            case FINISH:
                userStates.remove(userId);
        }
    }

    public static void requestToken(String platform, String chatId) {
        MessageService.dispatch((new TEMessageBuilder())
                .platform(platform)
                .chatId(chatId)
                .sourceTTS("trello")
                .text("Please get your Trello token <a href=\"https://trello.com/1/authorize?" +
                        "expiration=never&" +
                        "name=TaskEyeToken&" +
                        "scope=read,write,account&" +
                        "response_type=token&" +
                        "key=" + BotData.TRELLO_API_KEY + "\">here</a> " +
                        "and input it with a <code>/set_token trello [your token]</code> command, " +
                        "or use #trello_token to quickly find and use the previously used ones")
                .build());
    }

    private enum UIStage {
        FINISH(null),

        ADD_EVENT_TYPE_SELECTION(FINISH),
        ADD_BOARD_SELECTION(ADD_EVENT_TYPE_SELECTION),
        ADD_TOKEN_INPUT(ADD_BOARD_SELECTION),
        ADD_START(ADD_TOKEN_INPUT),

        LIST_TOKEN_INPUT(FINISH),
        LIST_START(LIST_TOKEN_INPUT),

        REMOVE_TRACKER_SELECTION(FINISH),
        REMOVE_TOKEN_INPUT(REMOVE_TRACKER_SELECTION),
        REMOVE_START(REMOVE_TOKEN_INPUT);

        private final UIStage next;

        UIStage(UIStage next) {
            this.next = next;
        }

        public UIStage getNext() {
            return next;
        }
    }

}
