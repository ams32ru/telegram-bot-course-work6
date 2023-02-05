package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationService {
    private static final Pattern NOTIFICATION_PATTERN
            = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+]+)");
    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    public NotificationService(NotificationTaskRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    public boolean processNotificatiom(Long chatId, String message) {
        Matcher messageMatcher = NOTIFICATION_PATTERN.matcher(message);
        if (!messageMatcher.matches()) {
            return false;
        }

        String stringDate = messageMatcher.group(1);
        String notificationText = messageMatcher.group(3);
        try {
            LocalDateTime notificationDate =
                    LocalDateTime.parse(stringDate, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setMessage(message);
            notificationTask.setChatId(chatId);
            notificationTask.setDate(notificationDate);
            repository.save(notificationTask);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotifications() {
        this.repository.findByDate(LocalDateTime.now())
                .forEach(task -> {
                    this.telegramBot
                            .execute(new SendMessage(task.getChatId(), task.getMessage()));
                });
    }
}
