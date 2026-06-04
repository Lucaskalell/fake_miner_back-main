package com.fakeminer.fake_miner.service;

import com.fakeminer.fake_miner.model.MetricasHardware;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.util.Random;

@Service
public class MonitoramentoHardwareService {

    private static final double TEMPERATURA_BASE = 45.0;
    private static final double TEMPERATURA_MAXIMA = 95.0;
    private static final Random random = new Random();
    private double temperaturaAtual = TEMPERATURA_BASE;

    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem sistemaOperacional;

    public MonitoramentoHardwareService(SystemInfo sistemaInfo) {
        this.hardware = sistemaInfo.getHardware();
        this.sistemaOperacional = sistemaInfo.getOperatingSystem();
    }

    public MetricasHardware coletarMetricas() {
        CentralProcessor processador = hardware.getProcessor();
        GlobalMemory memoria = hardware.getMemory();

        double usoCpu = processador.getSystemCpuLoad(1000) * 100;
        double temperaturaCpu = calcularTemperaturaSimulada(usoCpu);

        // Frequência média de todos os núcleos em GHz
        long[] frequencias = processador.getCurrentFreq();
        double frequenciaMediaGhz = 0;
        if (frequencias.length > 0) {
            long somaFrequencias = 0;
            for (long freq : frequencias) somaFrequencias += freq;
            frequenciaMediaGhz = (double) somaFrequencias / frequencias.length / 1_000_000_000.0;
        }

        long memoriaDisponivel = memoria.getAvailable();
        long memoriaTotal = memoria.getTotal();
        long memoriaUsada = memoriaTotal - memoriaDisponivel;

        double memoriaUsadaGb = memoriaUsada / (1024.0 * 1024.0 * 1024.0);
        double memoriaTotalGb = memoriaTotal / (1024.0 * 1024.0 * 1024.0);
        double percentualMemoriaUsada = ((double) memoriaUsada / memoriaTotal) * 100;

        double swapUsadaGb = (memoria.getVirtualMemory().getSwapUsed()) / (1024.0 * 1024.0 * 1024.0);

        return MetricasHardware.builder()
                .temperaturaCpu(temperaturaCpu)
                .usoCpu(usoCpu)
                .frequenciaCpuGhz(frequenciaMediaGhz)
                .memoriaUsadaGb(memoriaUsadaGb)
                .memoriaTotalGb(memoriaTotalGb)
                .percentualMemoriaUsada(percentualMemoriaUsada)
                .swapUsadaGb(swapUsadaGb)
                .uptimeSegundos(sistemaOperacional.getSystemUptime())
                .build();
    }


    private double calcularTemperaturaSimulada(double usoCpu) {
        double alvoTemperatura = TEMPERATURA_BASE + (usoCpu / 100.0) * 40.0;
        double variacao = (random.nextDouble() * 2.0) - 1.0;
        temperaturaAtual = temperaturaAtual + (alvoTemperatura - temperaturaAtual) * 0.1 + variacao;
        temperaturaAtual = Math.max(TEMPERATURA_BASE, Math.min(TEMPERATURA_MAXIMA, temperaturaAtual));
        return temperaturaAtual;
    }
}
