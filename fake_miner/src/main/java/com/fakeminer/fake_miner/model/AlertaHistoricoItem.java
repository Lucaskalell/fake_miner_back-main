package com.fakeminer.fake_miner.model;

import java.time.LocalDateTime;

public record AlertaHistoricoItem(
        String mensagem,
        String severidade,
        LocalDateTime timestamp
) {
    public static AlertaHistoricoItem of(String mensagem, String severidade) {
        return new AlertaHistoricoItem(mensagem, severidade,
                LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
    }
}
