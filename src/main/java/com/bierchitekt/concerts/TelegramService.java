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
    private final String telegramChannel;

    public TelegramService(@NotEmpty @Value("${telegram.auth}") String telegramAuth,
                           @Value("${telegram.channel}") @NotEmpty String telegramChannel) {
        bot = new TelegramBot(telegramAuth);
        this.telegramChannel = telegramChannel;
    }

    public void sendMessage(String message) {

        SendMessage request = new SendMessage(telegramChannel, message)
                .parseMode(HTML);
        bot.execute(request);

    }
}
