package my.bot.telegrambot.app;

import my.bot.telegrambot.service.TelegramBot;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class TelegramBotApplication {

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot("task_telegram_bot", "5649659739:AAHhq_WRXaS-dxj_j7AxlyWE6wQbvavDUsM"));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
