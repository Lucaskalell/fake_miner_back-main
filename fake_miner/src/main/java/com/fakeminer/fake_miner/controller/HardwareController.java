package com.fakeminer.fake_miner.controller;

import com.fakeminer.fake_miner.service.MonitoramentoHardwareService;
import com.fakeminer.fake_miner.service.MotorMineracaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sistema")
public class HardwareController {

    private final MonitoramentoHardwareService monitoramentoService;
    private final MotorMineracaoService motorMineracao;

    public HardwareController(MonitoramentoHardwareService monitoramentoService, MotorMineracaoService motorMineracao) {
        this.monitoramentoService = monitoramentoService;
        this.motorMineracao = motorMineracao;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> obterStatusCompleto() {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("hardware", monitoramentoService.coletarMetricas());
        resposta.put("mineracao", motorMineracao.gerarStatusAtual());
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/minerador/alternar")
    public ResponseEntity<String> alternarMinerador() {
        motorMineracao.alternarEstado();
        String estado = motorMineracao.isAtivo() ? "LIGADO" : "DESLIGADO";
        return ResponseEntity.ok("Minerador simulado agora está: " + estado);
    }
}
