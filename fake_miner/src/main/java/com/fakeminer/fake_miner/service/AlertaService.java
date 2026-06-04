package com.fakeminer.fake_miner.service;

import com.fakeminer.fake_miner.model.AlertaHistoricoItem;
import com.fakeminer.fake_miner.model.EstadoAlerta;
import com.fakeminer.fake_miner.model.MetricasHardware;
import com.fakeminer.fake_miner.model.StatusMineracao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class AlertaService {

    private static final double LIMITE_TEMPERATURA = 85.0;
    private static final double LIMITE_CPU = 90.0;
    private static final double LIMITE_RAM = 85.0;
    private static final int COLETAS_VIGILANCIA = 5;

    private EstadoAlerta estadoAtual = EstadoAlerta.NORMAL;
    private int coletasEmVigilancia = 0;
    private final TelegramNotificacaoService telegramService;
    private final List<AlertaHistoricoItem> historico = new ArrayList<>();


    public AlertaService(TelegramNotificacaoService telegramService) {
        this.telegramService = telegramService;
    }

    public void avaliar(MetricasHardware hardware, StatusMineracao mineracao) {
        boolean metricaCritica = isCritico(hardware);

        switch (estadoAtual) {
            case NORMAL -> {
                if (metricaCritica) {
                    estadoAtual = EstadoAlerta.ALERTA_1;
                    registraAlerta(montarAlerta1(hardware, mineracao), "CRITICO");
                }
            }
            case ALERTA_1, VIGILANCIA -> {
                if (metricaCritica) {
                    coletasEmVigilancia++;
                    if (coletasEmVigilancia % COLETAS_VIGILANCIA == 0) {
                        estadoAtual = estadoAtual == EstadoAlerta.ALERTA_1
                                ? EstadoAlerta.VIGILANCIA
                                : EstadoAlerta.ALERTA_2;
                        registraAlerta(montarAtualizacaoVigilancia(hardware), "AVISO");
                    }
                } else {
                    resetar();
                }
            }
            case ALERTA_2 -> {
                if (metricaCritica) {
                    coletasEmVigilancia++;
                    if (coletasEmVigilancia % COLETAS_VIGILANCIA == 0) {
                        estadoAtual = EstadoAlerta.ALERTA_3;
                        registraAlerta(montarAlerta3(), "CRITICO");
                    }
                } else {
                    resetar();

                }
            }
            case ALERTA_3, DESLIGADO -> {
            }
        }
    }

    public void responderVigilanciaConstante() {
        estadoAtual = EstadoAlerta.VIGILANCIA;
        coletasEmVigilancia = 0;
        telegramService.enviarMensagem("Vigilancia constante ativada. Monitorando a cada 25 segundos.");
    }

    public void responderDesligarMinerador(MotorMineracaoService motorMineracao) {
        estadoAtual = EstadoAlerta.DESLIGADO;
        motorMineracao.alternarEstado();
        telegramService.enviarMensagem("Minerador desligado com segurança pelo sistema de alertas.");
    }

    private boolean isCritico(MetricasHardware hardware) {
        return hardware.getTemperaturaCpu() > LIMITE_TEMPERATURA
                || hardware.getUsoCpu() > LIMITE_CPU
                || hardware.getPercentualMemoriaUsada() > LIMITE_RAM;
    }

    private void resetar() {
        estadoAtual = EstadoAlerta.NORMAL;
        coletasEmVigilancia = 0;
        registraAlerta("Sistema voltou ao normal. Monitoramento continuo ativo.", "INFO");
    }

    private String montarAlerta1(MetricasHardware hardware, StatusMineracao mineracao) {
        return String.format(
                "ALERTA - METRICA CRITICA DETECTADA\n\n" +
                        "Temperatura: %.1fC\n" +
                        "CPU: %.2f%%\n" +
                        "RAM: %.2f%%\n\n" +
                        "Minerador: %s\n" +
                        "Hash Rate: %.2f MH/s\n" +
                        "Consumo: %.2fW\n" +
                        "Lucro acumulado: $%.6f\n\n" +
                        "Responda com:\n" +
                        "/vigilancia - Ativar vigilancia constante\n" +
                        "/desligar - Desligar minerador",
                hardware.getTemperaturaCpu(),
                hardware.getUsoCpu(),
                hardware.getPercentualMemoriaUsada(),
                mineracao.isAtivo() ? "ATIVO" : "DESLIGADO",
                mineracao.getHashRateAtual(),
                mineracao.getConsumoWatts(),
                mineracao.getLucroEstimadoDolar()
        );
    }

    private String montarAtualizacaoVigilancia(MetricasHardware hardware) {
        return String.format(
                "VIGILANCIA - ATUALIZACAO\n\n" +
                        "Temperatura: %.1fC\n" +
                        "CPU: %.2f%%\n" +
                        "RAM: %.2f%%\n\n" +
                        "Responda com:\n" +
                        "/vigilancia - Manter vigilancia\n" +
                        "/desligar - Desligar minerador",
                hardware.getTemperaturaCpu(),
                hardware.getUsoCpu(),
                hardware.getPercentualMemoriaUsada()
        );
    }

    private String montarAlerta3() {
        return "ALERTA CRITICO - Terceiro aviso consecutivo.\n\n" +
                "Sistema em risco. Acao necessaria:\n\n" +
                "/desligar - Desligar minerador agora";
    }

    private void registraAlerta(String mensagem, String severidade) {
        historico.add(AlertaHistoricoItem.of(mensagem, severidade));
        telegramService.enviarMensagem(mensagem);
    }

    public List<AlertaHistoricoItem> buscarHistorico() {
        return Collections.unmodifiableList(historico);
    }


    public EstadoAlerta getEstadoAtual() {
        return estadoAtual;
    }
}