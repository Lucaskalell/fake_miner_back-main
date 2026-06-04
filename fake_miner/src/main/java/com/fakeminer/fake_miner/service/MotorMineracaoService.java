package com.fakeminer.fake_miner.service;

import com.fakeminer.fake_miner.model.StatusMineracao;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class MotorMineracaoService {

    private static final double HASH_RATE_BASE = 50.0; // MH/s base
    private static final double CONSUMO_BASE_WATTS = 120.0;
    private static final double PRECO_POR_MH = 0.00005; // Lucro fictício por MH/s

    private final Random random = new Random();
    private boolean mineradorLigado = false;
    private LocalDateTime inicioSessao;
    private double totalHashGerado = 0;

    public void alternarEstado() {
        this.mineradorLigado = !this.mineradorLigado;
        if (this.mineradorLigado) {
            this.inicioSessao = LocalDateTime.now();
            this.totalHashGerado = 0;
        }
    }

    public StatusMineracao gerarStatusAtual() {
        if (!mineradorLigado) {
            return StatusMineracao.builder()
                    .ativo(false)
                    .build();
        }

        // Simula flutuação natural de 5% no hash rate
        double variacao = 1.0 + (random.nextDouble() * 0.1 - 0.05);
        double hashRateAtual = HASH_RATE_BASE * variacao;
        
        // Simula variação no consumo baseado no esforço
        double consumoAtual = CONSUMO_BASE_WATTS * (0.9 + random.nextDouble() * 0.2);
        
        totalHashGerado += hashRateAtual;
        double lucroEstimado = totalHashGerado * PRECO_POR_MH;
        
        return StatusMineracao.builder()
                .ativo(true)
                .hashRateAtual(hashRateAtual)
                .hashRateMedio(HASH_RATE_BASE)
                .consumoWatts(consumoAtual)
                .eficiencia(hashRateAtual / consumoAtual)
                .lucroEstimadoDolar(lucroEstimado)
                .progressoMetaDiaria(Math.min(100, (lucroEstimado / 5.0) * 100)) // Meta de 5 dólares
                .inicioSessao(inicioSessao)
                .build();
    }

    public boolean isAtivo() {
        return mineradorLigado;
    }
}
