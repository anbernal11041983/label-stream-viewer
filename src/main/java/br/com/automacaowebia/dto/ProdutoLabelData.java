package br.com.automacaowebia.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoLabelData(
        String codigoBarras,
        String urlQrCode,
        String sif,
        String sku,
        LocalDate dataProducao,
        LocalDate dataValidade,
        String descCompleta,
        String descReduzida,
        BigDecimal pesoKg) {}