package com.taskeye.bot;

import com.taskeye.bot.TEMessage;
import com.taskeye.bot.services.TelegramService;

public class MessageService {

    public static void dispatch(TEMessage message) {
        switch (message.platform) {
            case "telegram":
                TelegramService.sendMessage(message);
                return;
            case "vk":
                // Plug for a possible future feature
                System.err.println("No VK message handler defined");
        }
    }

}
