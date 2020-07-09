package com.taskeye.bot.message_text;

public class TelegramMessageText {

    private static final String START = "Hello and thank you for using TaskEye :)\n\n" +
            "Missing none of your task-tracking notifications since time immemorial \uD83D\uDDFF\n\n" +
            "To start using TaskEye, please refer to /help";

    private static final String HELP = "The following commands are available:\n\n" +
            "/help - view this message\n\n" +

            "<strong>Editing</strong>\n" +
            "/set_token [system] [token] - set the task-tracking system token\n" +
            "<em>Using one of these commands will reset any tracker editing progress</em>\n" +
            "/add - add a new tracker\n" +
            "/list - view the list of your trackers\n" +
            "/remove - remove one of your trackers\n\n" +

            "<strong>Miscellaneous</strong>\n" +
            "/bruh - unveil the true nature of this <strike>Noble Phantasm</strike> bot\n" +
            "/apology - view the reasoning behind questionable UI and possible errors\n\n" +

            "<strong>IMPORTANT:</strong> <em>Please don't change the URL, provided for token acquisition.</em>\n" +
            "If the token doesn't have enough permissions or is expired, you won't be able to use the bot fully. " +
            "You can make sure the bot doesn't do anything unnecessary or " +
            "malicious on its <a href=\"#\">Github</a> page. " +
            "If, however, your token does change, removal of associated trackers will require you to manually " +
            "delete the webhook in your TTS settings\n" +
            "We also encourage you to remove the unused trackers, even if you no " +
            "longer get any notifications from them";

    private static final String APOLOGY = "In an attempt to not store <strong>any</strong> of " +
            "your personal information on our server (for purposes of security), " +
            "we had to make some sacrifices in the user experience, so we ask " +
            "for your understanding.\nThe server is also regularly restarted at 13:37 UTC, and " +
            "any unfinished trackers being created are lost when it happens. We apologize for any inconveniences " +
            "it causes.";

    private static final String ERROR = "Something went wrong \uD83E\uDD14\n" +
            "If you were in the middle of creating a tracker, please try to start over";

    public static String getStart() {
        return START;
    }

    public static String getHelp() {
        return HELP;
    }

    public static String getApology() {
        return APOLOGY;
    }

    public static String getError() {
        return ERROR;
    }
}
