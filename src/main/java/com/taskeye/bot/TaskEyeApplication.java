package com.taskeye.bot;

import com.taskeye.bot.services.TelegramService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskEyeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskEyeApplication.class, args);

		TelegramService.setWebhook();
	}

}
