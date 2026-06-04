package com.fakeminer.fake_miner.controller;

import com.fakeminer.fake_miner.model.AlertaHistoricoItem;
import com.fakeminer.fake_miner.model.MetricasHardware;
import com.fakeminer.fake_miner.model.StatusMineracao;
import com.fakeminer.fake_miner.service.AlertaService;
import com.fakeminer.fake_miner.service.MonitoramentoHardwareService;
import com.fakeminer.fake_miner.service.MotorMineracaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1")
public class MetricasController {

    private final MonitoramentoHardwareService monitoramentoService;
    private final MotorMineracaoService motorMineracao;
    private final AlertaService alertaService;

    public MetricasController(
            MonitoramentoHardwareService monitoramentoService,
            MotorMineracaoService motorMineracao,
            AlertaService alertaService
    ) {
        this.monitoramentoService = monitoramentoService;
        this.motorMineracao = motorMineracao;
        this.alertaService = alertaService;
    }

    @GetMapping("/metricas")
    public ResponseEntity<SystemSnapshotResponse> getSystemSnapshot() {
        MetricasHardware hardware = monitoramentoService.coletarMetricas();
        StatusMineracao mineracao = motorMineracao.gerarStatusAtual();
        return ResponseEntity.ok(new SystemSnapshotResponse(hardware, mineracao));
    }

    @GetMapping("/metricas/hardware")
    public ResponseEntity<MetricasHardware> getHardwareMetrics() {
        return ResponseEntity.ok(monitoramentoService.coletarMetricas());
    }

    @GetMapping("/metricas/minerador")
    public ResponseEntity<StatusMineracao> getMineradorStatus() {
        return ResponseEntity.ok(motorMineracao.gerarStatusAtual());
    }

    @PostMapping("/minerador/alternar")
    public ResponseEntity<StatusMineracao> alternarMinerador() {
        motorMineracao.alternarEstado();
        return ResponseEntity.ok(motorMineracao.gerarStatusAtual());
    }
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaHistoricoItem>> getAlertas() {
        return ResponseEntity.ok(alertaService.buscarHistorico());
    }

    public record SystemSnapshotResponse(MetricasHardware hardware, StatusMineracao mineracao) {}
}