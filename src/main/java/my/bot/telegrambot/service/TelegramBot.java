package my.bot.telegrambot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final String BOT_NAME;
    private final String BOT_TOKEN;
    private Long CHAT_ID;

//    private Map<String, Long[]> familyMap = new HashMap<>();
    private String PREVIOUS_MESSAGE = "";

    public TelegramBot(String botName, String botToken) {
        BOT_NAME = botName;
        BOT_TOKEN = botToken;
        List<BotCommand> listOfCommands = new ArrayList<>();

        listOfCommands.add(new BotCommand("/start", "start bot"));
        listOfCommands.add(new BotCommand("/calculator", "Подсчет"));
//        listofCommands.add(new BotCommand("/family", "вход в семью"));
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            CHAT_ID = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getFirstName();

            if (message.equals("/start")) {
                sendMessage("Привет " + userName + ", теперь ты с нами)");
            }
            if (message.equals("/calculator")) {
                sendMessage("Введите простое выражение через пробел: ");
            }
//            if (message.equals("/family")) {
//                sendMessage("Введите Фамилию, пароль через пробел:");
//            }
            if (!PREVIOUS_MESSAGE.equals("")) {
                if (PREVIOUS_MESSAGE.equals("/calculator")) {
                    String answer = count(message);
                    sendMessage(answer);
                }
//                if (PREVIOUS_MESSAGE.equals("/family")) {
//                    family(message);
//                }
            }

            PREVIOUS_MESSAGE = message;
        }
    }

//    private void family(String message) {
//        String[] mass = message.split(" ");
//
//
//    }


    private String count(String message) {
        String[] mass = message.split(" ");
        String answer;

        int ans = Integer.parseInt(mass[0]);
        for (int i = 1; i < mass.length; i++) {
            if (Objects.equals(mass[i], "+")) {
                ans += Integer.parseInt(mass[i + 1]);
            } else if (Objects.equals(mass[i], "-")) {
                ans -= Integer.parseInt(mass[i + 1]);
            } else if (Objects.equals(mass[i], "*")) {
                ans *= Integer.parseInt(mass[i + 1]);
            } else if (Objects.equals(mass[i], "/")) {
                ans /= Integer.parseInt(mass[i + 1]);
            }
        }
        answer = String.valueOf(ans);

        return answer;
    }

    private void sendMessage(String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(CHAT_ID);
        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
