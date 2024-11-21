package com.bierchitekt.concerts;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
public class TelegramService {

    private final TelegramClient telegramClient;

    @Value("${telegram.channel}")
    @NotEmpty
    private String telegramChannel;

    public TelegramService(@NotEmpty @Value("${telegram.auth}") String telegramAuth) {
        this.telegramClient = new OkHttpTelegramClient(telegramAuth);
    }

    public void sendMessage(String message) {
        SendMessage sendMessage = new SendMessage(telegramChannel, message);

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

}
