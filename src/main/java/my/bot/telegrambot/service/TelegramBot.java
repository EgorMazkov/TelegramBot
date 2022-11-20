package my.bot.telegrambot.service;

import lombok.var;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final String BOT_NAME;
    private final String BOT_TOKEN;
    private static Long CHAT_ID;
    private String PREVIOUS_MESSAGE = "";
    private final List<Family> families = new ArrayList<>();
    private String firstName;
    private String userName;

    public TelegramBot(String botName, String botToken) {
        BOT_NAME = botName;
        BOT_TOKEN = botToken;
        List<BotCommand> listOfCommands = new ArrayList<>();

        listOfCommands.add(new BotCommand("/start", "start bot"));
        listOfCommands.add(new BotCommand("/calculator", "Подсчет"));
//        listOfCommands.add(new BotCommand("/family", "вход в семью"));
//        listOfCommands.add(new BotCommand("/messageFamily", "сообщение семье"));


        try {
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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
            if (PREVIOUS_MESSAGE.equals(message) && CHAT_ID.equals(update.getMessage().getChatId())) {
                return;
            }
            CHAT_ID = update.getMessage().getChatId();
            firstName = update.getMessage().getChat().getFirstName();
            userName = update.getMessage().getChat().getUserName();
            if (message.equals("/start")) {
                sendMessage("Привет " + firstName + ", теперь ты с нами)", message);
            }
            if (message.equals("/calculator")) {
                sendMessage("Введите простое выражение через пробел: ", message);
            }
            if (message.equals("Создать или войти в группу Семья")) {
                sendMessage("Введите Фамилию, пароль через пробел:", message);
            }
            if (message.equals("Семья")) {
                sendMessage("Вы перешли в группу семьи", message);
            }
            if (message.equals("Написать сообщение семье")) {
                sendMessage("Введите сообщение", "Семья");
            }
            if (message.equals("Какие продукты нужно купить")) {
                sendMessage("Введите продукты через пробел", message);
            }
            if (!PREVIOUS_MESSAGE.equals("")) {
                if (PREVIOUS_MESSAGE.equals("/calculator")) {
                    String answer = count(message);
                    sendMessage(answer, message);
                }
                if (PREVIOUS_MESSAGE.equals("Написать сообщение семье")) {
                    sendMessageFamily(message, PREVIOUS_MESSAGE);
                }
                if (PREVIOUS_MESSAGE.equals("Создать или войти в группу Семья")) {
                    family(message);
                }
                if (PREVIOUS_MESSAGE.equals("Какие продукты нужно купить")) {
                    sendMessageFamily(message, PREVIOUS_MESSAGE);
                }
            }
            //TODO delete
            System.out.println("---------------------------------------------------------------------");
            System.out.println(firstName + ": " + message + ", id: " + CHAT_ID + ", Имя пользователя: " + userName);
            System.out.println("---------------------------------------------------------------------");
            PREVIOUS_MESSAGE = message;

        } else if (update.hasCallbackQuery()) {
            String messageCallback = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String name = update.getCallbackQuery().getMessage().getChat().getFirstName();
            EditMessageText mes = new EditMessageText();

            if (messageCallback.equals("yes")) {
                sendMessageFamily(name + " может купить продукты", "start");
            } else if (messageCallback.equals("no")) {
                sendMessageFamily(name + " не может купить продукты", "start");
            }

            try {
                mes.setChatId(chatId);
                mes.setText(splitMessage(PREVIOUS_MESSAGE));
                mes.setMessageId((int) messageId);
                execute(mes);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }


        }
    }

    private String splitMessage(String message) {
        String[] products = message.split(" ");
        message = "Нужно купить продукты: \n";
        for (String product : products) {
            message += product + "\n";
        }
        return message;
    }

    private void sendMessageFamily(String message, String command) {
        if (command.equals("Какие продукты нужно купить")) {
            message = splitMessage(message);
        }

        Family family = families.get(checkFamily());
        if (checkFamily() == -1) {
            sendMessage("Вы не добавились не в одну группу", "Start");
            return;
        }

        Long[] idUserFamily = family.getChatId();
        for (Long aLong : idUserFamily) {
            if (!CHAT_ID.equals(aLong)) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(aLong);
                sendMessage.setText(message);

                if (command.equals("Какие продукты нужно купить")) {
                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> lListInline = new ArrayList<>();
                    List<InlineKeyboardButton> listInline = new ArrayList<>();

                    var yesButton = new InlineKeyboardButton();

                    yesButton.setText("Я могу купить");
                    yesButton.setCallbackData("yes");

                    var noButton = new InlineKeyboardButton();

                    noButton.setText("Я не могу купить");
                    noButton.setCallbackData("no");

                    listInline.add(yesButton);
                    listInline.add(noButton);

                    lListInline.add(listInline);

                    inlineKeyboardMarkup.setKeyboard(lListInline);

                    sendMessage.setReplyMarkup(inlineKeyboardMarkup);

                }

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
                sendMessage("Вы вошли в группу: " + family.getName(), message);
                sendMessageFamily("К группе добавился человек: " + firstName, "Семья");
                return;
            }
        }

        Family familyNew = new Family(massMessage[0], massMessage[1]);
        families.add(familyNew);
        sendMessage("Была создана группа: " + familyNew.getName(), message);
    }

    public static Long getChatIdBot() {
        return CHAT_ID;
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

    private void sendMessage(String message, String command) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(CHAT_ID);
        sendMessage.setText(message);

        if (command.equals("Семья")) {
            ReplyKeyboardMarkup keyboardMarkup = keyboardFamily();
            sendMessage.setReplyMarkup(keyboardMarkup);
        } else {
            ReplyKeyboardMarkup keyboardStart = keyboardStart();
            sendMessage.setReplyMarkup(keyboardStart);
        }

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private ReplyKeyboardMarkup keyboardFamily() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add("Написать сообщение семье");
        row.add("Какие продукты нужно купить");

        keyboardRowList.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRowList);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup keyboardStart() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        if (checkFamily() == -1) {
            row.add("Создать или войти в группу Семья");
        } else {
            row.add("Семья");
        }
        row.add("Калькулятор");

        keyboardRowList.add(row);

//        if (userName.length() != 0) {
//            if (userName.equals("ghumbert")) {
//                row = new KeyboardRow();
//
//                row.add("Stop bot");
//                row.add("information all families");
//
//                keyboardRowList.add(row);
//            }
//        }

        replyKeyboardMarkup.setKeyboard(keyboardRowList);

        return replyKeyboardMarkup;
    }

    public int checkFamily() {
        for (int i = 0; i < families.size(); i++) {
            Family family = families.get(i);
            Long[] checkId = family.getChatId();
            for (Long aLong : checkId) {
                if (getChatIdBot().equals(aLong)) {
                    return i;
                }
            }
        }
        return -1;
    }

}
