package com.fakeminer.fake_miner.service;


import com.fakeminer.fake_miner.bot.FakeMinerBot;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class TelegramNotificacaoService {
    private final FakeMinerBot bot;

    @Value("${telegram.bot.chat-id}")
    private String chatId;

    public TelegramNotificacaoService(@Lazy FakeMinerBot bot) {
        this.bot = bot;
    }

    public void enviarMensagem(String texto) {
        SendMessage mensagem = new SendMessage();
        mensagem.setChatId(chatId);
        mensagem.setText(texto);
        try {
            bot.execute(mensagem);
        } catch (TelegramApiException e) {
            log.error("Erro ao enviar mensagem no Telegram: {}", e.getMessage());
        }
    }
}
