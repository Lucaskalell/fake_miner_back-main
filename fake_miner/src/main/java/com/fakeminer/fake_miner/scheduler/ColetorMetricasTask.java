package com.fakeminer.fake_miner.scheduler;

import com.fakeminer.fake_miner.model.MetricasHardware;
import com.fakeminer.fake_miner.model.StatusMineracao;
import com.fakeminer.fake_miner.service.AlertaService;
import com.fakeminer.fake_miner.service.MonitoramentoHardwareService;
import com.fakeminer.fake_miner.service.MotorMineracaoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ColetorMetricasTask {

    private final MonitoramentoHardwareService monitoramentoService;
    private final MotorMineracaoService motorMineracao;
    private final AlertaService alertaService;

    public ColetorMetricasTask(
            MonitoramentoHardwareService monitoramentoService,
            MotorMineracaoService motorMineracao,
            AlertaService alertaService
    ) {
        this.monitoramentoService = monitoramentoService;
        this.motorMineracao = motorMineracao;
        this.alertaService = alertaService;
        
        // Inicia o minerador automaticamente
        if (!this.motorMineracao.isAtivo()) {
            this.motorMineracao.alternarEstado();
        }
    }

    @Scheduled(fixedRate = 5000)
    public void executarColeta() {
        MetricasHardware hardware = monitoramentoService.coletarMetricas();
        StatusMineracao mineracao = motorMineracao.gerarStatusAtual();
        
        exibirHardware(hardware);
        exibirMineracao(mineracao);
        alertaService.avaliar(hardware, mineracao);
    }

    private void exibirHardware(MetricasHardware metricas) {
        String tempDisplay = metricas.getTemperaturaCpu() > 0 
                ? String.format("%.1f°C", metricas.getTemperaturaCpu()) 
                : "N/A (Requer Admin/Driver)";

        log.info("--- Monitoramento de Hardware ---");
        log.info("CPU: {}% | Freq: {}GHz | Temp: {}",
                String.format("%.2f", metricas.getUsoCpu()), 
                String.format("%.2f", metricas.getFrequenciaCpuGhz()),
                tempDisplay);
        log.info("RAM: {}% ({}GB/{}GB) | Swap: {}GB", 
                String.format("%.2f", metricas.getPercentualMemoriaUsada()),
                String.format("%.2f", metricas.getMemoriaUsadaGb()),
                String.format("%.2f", metricas.getMemoriaTotalGb()),
                String.format("%.2f", metricas.getSwapUsadaGb()));
    }

    private void exibirMineracao(StatusMineracao status) {
        if (!status.isAtivo()) {
            log.info("--- Motor de Mineração: DESLIGADO ---");
            return;
        }

        log.info("--- Motor de Mineração: ATIVO ---");
        log.info("Hash Rate: {} MH/s | Consumo: {}W", 
                String.format("%.2f", status.getHashRateAtual()),
                String.format("%.2f", status.getConsumoWatts()));
        log.info("Lucro Estimado: ${} | Meta: {}%", 
                String.format("%.6f", status.getLucroEstimadoDolar()),
                String.format("%.2f", status.getProgressoMetaDiaria()));
        log.info("---------------------------");
    }
}
