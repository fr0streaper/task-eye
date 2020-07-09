package com.taskeye.bot;

import com.taskeye.bot.services.TrelloConfigService;

public class TTSService {

    public static void continueFromStage(String sourceTTS,
                                         String platform,
                                         String chatId,
                                         String stage,
                                         String response) {
        switch (sourceTTS) {
            case "trello":
                TrelloConfigService.continueFromStage(platform, chatId, stage, response);
                break;
            case "jira":
                // Plug for a possible future feature
                System.err.println("No Jira tracker handler defined");
        }
    }

}
