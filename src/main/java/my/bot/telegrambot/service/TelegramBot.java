package my.bot.telegrambot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final String BOT_NAME;
    private final String BOT_TOKEN;
    private Long CHAT_ID;


    private String PREVIOUS_MESSAGE = "";
    List<Family> families = new ArrayList<>();

    public TelegramBot(String botName, String botToken) {
        BOT_NAME = botName;
        BOT_TOKEN = botToken;
        List<BotCommand> listOfCommands = new ArrayList<>();

        listOfCommands.add(new BotCommand("/start", "start bot"));
        listOfCommands.add(new BotCommand("/calculator", "Подсчет"));
        listOfCommands.add(new BotCommand("/family", "вход в семью"));
        listOfCommands.add(new BotCommand("/messageFamily", "сообщение семье"));
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
            if (message.equals("/family")) {
                sendMessage("Введите Фамилию, пароль через пробел:");
            }
            if (message.equals("/messageFamily")) {
                sendMessage("Введите сообщение:");
            }
            if (!PREVIOUS_MESSAGE.equals("")) {
                if (PREVIOUS_MESSAGE.equals("/calculator")) {
                    String answer = count(message);
                    sendMessage(answer);
                }
                if (PREVIOUS_MESSAGE.equals("/family")) {
                    family(message);
                }
                if (PREVIOUS_MESSAGE.equals("/messageFamily")) {
                    int idFamily = checkFamily();
                    sendMessageFamily(message, idFamily);
                }
            }

            //TODO delete
            System.out.println("---------------------------------------------------------------------");
            System.out.println(userName + ": " + message);
            System.out.println("---------------------------------------------------------------------");
            PREVIOUS_MESSAGE = message;
        }
    }

    private int checkFamily() {
        for (int i = 0; i < families.size(); i++) {
            Family family = families.get(i);
            Long[] checkId = family.getChatId();
            for (int j = 0; j < checkId.length; j++) {
                if (CHAT_ID.equals(checkId[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void sendMessageFamily(String message, int idFamily) {
        if (idFamily == -1) {
            sendMessage("Вы не в одной группе семьи");
            return;
        }
        Family family = families.get(idFamily);

        Long[] idUserFamily = family.getChatId();
        for (int i = 0; i < idUserFamily.length; i++) {
            if (!CHAT_ID.equals(idUserFamily[i])) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(idUserFamily[i]);
                sendMessage.setText(message);

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    private void family(String message) {
        int j = 0;
        String[] massMessage = message.split(" ");

            for (int i = 0; i < (long) families.size(); i++) {
            Family family = families.get(i);
            if (family.getName().equals(massMessage[0]) && family.getPassword().equals(massMessage[1])) {

                Long[] chatid = family.getChatId();
                Long[] chatIdNew = new Long[family.getChatId().length + 1];
                for (; j < family.getChatId().length; j++) {
                    chatIdNew[j] = chatid[j];
                }
                chatIdNew[j] = CHAT_ID;
                family.setChatId(chatIdNew);
                families.remove(i);
                families.add(family);
                return;
            }
        }

        Family familyNew = new Family(massMessage[0], massMessage[1], CHAT_ID);
        families.add(familyNew);
    }


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
