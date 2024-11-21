package com.bierchitekt.concerts;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Service
public class TelegramService {

    private final TelegramClient telegramClient = new OkHttpTelegramClient("xxx");


    public void sendMessage(String message) {
        SendMessage sendMessage = new SendMessage("-xxx", message);

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

}
