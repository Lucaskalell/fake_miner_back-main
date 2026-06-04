package com.fakeminer.fake_miner.scheduler;

import com.fakeminer.fake_miner.model.MetricasHardware;
import com.fakeminer.fake_miner.model.StatusMineracao;
import com.fakeminer.fake_miner.service.MonitoramentoHardwareService;
import com.fakeminer.fake_miner.service.MotorMineracaoService;
import com.fakeminer.fake_miner.service.TelegramNotificacaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResumoProgramadoTask {

    private final MonitoramentoHardwareService monitoramentoService;
    private final MotorMineracaoService motorMineracao;
    private final TelegramNotificacaoService telegramService;

    public ResumoProgramadoTask(MonitoramentoHardwareService monitoramentoService,
                                MotorMineracaoService motorMineracao,
                                TelegramNotificacaoService telegramService) {
        this.monitoramentoService = monitoramentoService;
        this.motorMineracao = motorMineracao;
        this.telegramService = telegramService;
    }

    @Scheduled(fixedRate = 3600000)
    //teste 1 minuto = 60000
    //valor real  1 hora = 36000003600000
    public void enviarResumoProgramado() {
        MetricasHardware hardware = monitoramentoService.coletarMetricas();
        StatusMineracao mineracao = motorMineracao.gerarStatusAtual();

        telegramService.enviarMensagem(montarResumo(hardware, mineracao));
        log.info("Resumo programado enviado ao Telegram.");
    }

    private String montarResumo(MetricasHardware hardware, StatusMineracao mineracao) {
        double metaRestante = Math.max(0, 100.0 - mineracao.getProgressoMetaDiaria());
        long uptimeMinutos = hardware.getUptimeSegundos() / 60;
        long horas = uptimeMinutos / 60;
        long minutos = uptimeMinutos % 60;

        return String.format(
                "RESUMO DA HORA\n\n" +
                        "MINERADOR\n" +
                        "Status: %s\n" +
                        "Hash Rate: %.2f MH/s\n" +
                        "Consumo: %.2fW\n" +
                        "Lucro acumulado: $%.6f\n" +
                        "Meta diaria: %.2f%% — faltam %.2f%%\n\n" +
                        "MAQUINA\n" +
                        "CPU: %.2f%% | %.2fGHz\n" +
                        "Temp: %.1fC\n" +
                        "RAM: %.2f%% (%.2fGB/%.2fGB)\n" +
                        "Uptime: %dh %dmin",
                mineracao.isAtivo() ? "ATIVO" : "DESLIGADO",
                mineracao.getHashRateAtual(),
                mineracao.getConsumoWatts(),
                mineracao.getLucroEstimadoDolar(),
                mineracao.getProgressoMetaDiaria(),
                metaRestante,
                hardware.getUsoCpu(),
                hardware.getFrequenciaCpuGhz(),
                hardware.getTemperaturaCpu(),
                hardware.getPercentualMemoriaUsada(),
                hardware.getMemoriaUsadaGb(),
                hardware.getMemoriaTotalGb(),
                horas,
                minutos
        );
    }
}