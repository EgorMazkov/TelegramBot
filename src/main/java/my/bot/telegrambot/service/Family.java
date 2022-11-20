package my.bot.telegrambot.service;

import static my.bot.telegrambot.service.TelegramBot.*;

public class Family {
    String name;
    String password;
    Long[] chatId;

    public Family(String name, String password) {
        this.name = name;
        this.password = password;
        chatId = new Long[1];
        chatId[0] = getChatIdBot();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long[] getChatId() {
        return chatId;
    }

    public void setChatId(Long[] chatId) {
        this.chatId = chatId;
    }

}
