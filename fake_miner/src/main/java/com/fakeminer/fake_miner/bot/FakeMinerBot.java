package com.fakeminer.fake_miner.bot;

import com.fakeminer.fake_miner.service.AlertaService;
import com.fakeminer.fake_miner.service.MonitoramentoHardwareService;
import com.fakeminer.fake_miner.service.MotorMineracaoService;
import com.fakeminer.fake_miner.model.MetricasHardware;
import com.fakeminer.fake_miner.model.StatusMineracao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class FakeMinerBot extends TelegramLongPollingBot {

    private final MonitoramentoHardwareService monitoramentoService;
    private final MotorMineracaoService motorMineracao;
    private final AlertaService alertaService;

    @Value("${telegram.bot.username}")
    private String username;

    public FakeMinerBot(@Value("${telegram.bot.token}") String token,
                        MonitoramentoHardwareService monitoramentoService,
                        MotorMineracaoService motorMineracao, AlertaService alertaService) {
        super(token);
        this.monitoramentoService = monitoramentoService;
        this.motorMineracao = motorMineracao;
        this.alertaService = alertaService;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String comando = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        String resposta = switch (comando) {
            case "/status" -> gerarMensagemStatus();
            case "/temps" -> gerarMensagemTemperatura();
            case "/vigilancia" -> {
                alertaService.responderVigilanciaConstante();
                yield "Vigilancia constante ativada.";
            }
            case "/desligar" -> {
                alertaService.responderDesligarMinerador(motorMineracao);
                yield "Minerador desligado por segurança, identificado situação critica.";
            }
            default -> "Comandos disponíveis:\n/status\n/temps\n/vigilancia\n/desligar";
        };

        enviarResposta(chatId, resposta);
    }


    private String gerarMensagemStatus() {
        MetricasHardware hardware = monitoramentoService.coletarMetricas();
        StatusMineracao mineracao = motorMineracao.gerarStatusAtual();

        return String.format(
                "STATUS DO SISTEMA\n\n" +
                        "CPU: %.2f%% | %.2fGHz\n" +
                        "Temp: %.1fC\n" +
                        "RAM: %.2f%% (%.2fGB/%.2fGB)\n\n" +
                        "Minerador: %s\n" +
                        "Hash Rate: %.2f MH/s\n" +
                        "Consumo: %.2fW\n" +
                        "Lucro: $%.6f\n" +
                        "Meta: %.2f%%",
                hardware.getUsoCpu(),
                hardware.getFrequenciaCpuGhz(),
                hardware.getTemperaturaCpu(),
                hardware.getPercentualMemoriaUsada(),
                hardware.getMemoriaUsadaGb(),
                hardware.getMemoriaTotalGb(),
                mineracao.isAtivo() ? "ATIVO" : "DESLIGADO",
                mineracao.getHashRateAtual(),
                mineracao.getConsumoWatts(),
                mineracao.getLucroEstimadoDolar(),
                mineracao.getProgressoMetaDiaria()
        );
    }

    private String gerarMensagemTemperatura() {
        MetricasHardware hardware = monitoramentoService.coletarMetricas();
        return String.format("Temperatura CPU: %.1fC\nUso CPU: %.2f%%",
                hardware.getTemperaturaCpu(),
                hardware.getUsoCpu());
    }

    private void enviarResposta(String chatId, String texto) {
        SendMessage mensagem = new SendMessage();
        mensagem.setChatId(chatId);
        mensagem.setText(texto);
        try {
            execute(mensagem);
        } catch (TelegramApiException e) {
            log.error("Erro ao responder comando Telegram: {}", e.getMessage());
        }
    }
}