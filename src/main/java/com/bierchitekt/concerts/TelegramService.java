package com.bierchitekt.concerts;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.pengrad.telegrambot.model.request.ParseMode.HTML;

@Service
public class TelegramService {

    private final TelegramBot bot;

    public TelegramService(@NotEmpty @Value("${telegram.auth}") String telegramAuth) {
        bot = new TelegramBot(telegramAuth);
    }

    public void sendMessage(String channelName, String message) {

        SendMessage request = new SendMessage(channelName, message)
                .parseMode(HTML);
        bot.execute(request);

    }
}
