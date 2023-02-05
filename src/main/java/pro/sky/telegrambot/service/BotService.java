package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotService implements UpdatesListener {

    private final TelegramBot telegramBot;
    private final NotificationService notificationService;

    public BotService(TelegramBot telegramBot, NotificationService notificationService) {
        this.telegramBot = telegramBot;
        this.notificationService = notificationService;
        this.telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> list) {
        list.stream()
                .filter(update -> update.message() != null)
                .forEach(this::processUpdate);
        return CONFIRMED_UPDATES_ALL;
    }

    private void processUpdate(Update update) {
        String userMessage = update.message().text();
        if (userMessage.equals("/start")) {
            this.telegramBot.execute(new SendMessage(update.message().chat().id(),
                    "Привет! Я напоминаю о событии!"));
        } else {
            if (this.notificationService.processNotificatiom(update.message().chat().id(), userMessage)) {
                this.telegramBot.execute(new SendMessage(update.message().chat().id(),
                        "Напоминание создано!"));
            } else {
                this.telegramBot.execute(new SendMessage(update.message().chat().id(),
                        "Неправильный формат ввода даты, я принимаю сообщения в формате \"01.01.2022 20:00 Сделать домашнюю работу\""));
            }
        }
    }
}
