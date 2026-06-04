package com.fakeminer.fake_miner.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StatusMineracao {
    private boolean ativo;
    private double hashRateAtual; // MH/s
    private double hashRateMedio; // MH/s
    private double consumoWatts;
    private double eficiencia; // MH/s por Watt
    private double lucroEstimadoDolar;
    private double progressoMetaDiaria; // Porcentagem
    private LocalDateTime inicioSessao;
}
