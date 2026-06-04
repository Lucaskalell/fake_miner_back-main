package com.fakeminer.fake_miner.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MetricasHardware {
    private double temperaturaCpu;
    private double usoCpu;
    private double frequenciaCpuGhz;
    private double memoriaUsadaGb;
    private double memoriaTotalGb;
    private double percentualMemoriaUsada;
    private double swapUsadaGb;
    private long uptimeSegundos;
}
