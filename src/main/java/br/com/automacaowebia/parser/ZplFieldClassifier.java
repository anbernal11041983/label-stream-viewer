package br.com.automacaowebia.parser;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.*;

final class ZplFieldClassifier {

    // formatos básicos
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern EAN13 = Pattern.compile("^\\d{13}$");
    private static final Pattern DATA = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");
    private static final Pattern PESO = Pattern.compile("(\\d+\\.\\d{3})kg");

    // tags dentro da URL do QR‑Code (podem vir em qualquer ordem)
    private static final Pattern P_SKU = Pattern.compile("<SKU>(\\d+)</SKU>");
    private static final Pattern P_SIF = Pattern.compile("<SIF>(\\d+)</SIF>");

    // SKU alternativo dentro de “C.P.381224”
    private static final Pattern P_CP_SKU = Pattern.compile("C\\.P\\.(\\d{4,})");

    // critérios de descrição
    private static final int MIN_DESC_COMPLETA = 20;   // ≥ 20 caracteres
    private static final int MIN_DESC_REDUZIDA = 4;    // ≥ 4 caracteres

    static void feed(Map<String, String> out, String texto) {

        /* === 1. Código de barras EAN‑13 =================================== */
        if (EAN13.matcher(texto).matches()) {
            out.put("codigoBarras", texto);
            return;
        }

        /* === 2. URL do QR‑Code (SKU + SIF em qualquer ordem) ============== */
        if (texto.startsWith("http")) {
            out.put("urlQrCode", texto);

            Matcher mSku = P_SKU.matcher(texto);
            if (mSku.find()) {
                out.put("sku", mSku.group(1));
            }

            Matcher mSif = P_SIF.matcher(texto);
            if (mSif.find()) {
                out.put("sif", mSif.group(1));
            }

            return;
        }

        /* === 3. Datas ===================================================== */
        if (DATA.matcher(texto).matches()) {
            if (!out.containsKey("dataProd")) {                     // 1ª data → produção
                out.put("dataProd", texto);
            } else if (!out.containsKey("dataVal") // 2ª data → validade
                    && !texto.equals(out.get("dataProd"))) {
                out.put("dataVal", texto);
            }
            return;
        }

        /* === 4. Peso ====================================================== */
        Matcher kg = PESO.matcher(texto);
        if (kg.find()) {
            out.put("peso", kg.group(1));
            return;
        }

        /* === 5. SKU extra em “C.P.381224” ================================ */
        Matcher mCp = P_CP_SKU.matcher(texto);
        if (mCp.find() && !out.containsKey("sku")) {
            out.put("sku", mCp.group(1));
            return; // ainda pode ser descrição, mas geralmente não é
        }

        /* === 6. Descrições =============================================== */
        // Aceita só letras maiúsculas e espaços (descarta underscores, dígitos, etc.)
        boolean letrasEspacos = texto.matches("[A-Z ÂÁÄÀÃÉÊÍÓÔÕÚÇ]+( [A-ZÂÁÄÀÃÉÊÍÓÔÕÚÇ]+)*");

        if (letrasEspacos) {
            if (!out.containsKey("descCompleta") && texto.length() >= MIN_DESC_COMPLETA) {
                out.put("descCompleta", texto.trim());
                return;
            }
            if (!out.containsKey("descReduzida")
                    && texto.length() >= MIN_DESC_REDUZIDA
                    && texto.length() < MIN_DESC_COMPLETA) {
                out.put("descReduzida", texto.trim());
            }
        }
    }

    /* Conversão segura para BigDecimal */
    static BigDecimal peso(String raw) {
        return raw == null ? BigDecimal.ZERO : new BigDecimal(raw);
    }

    private ZplFieldClassifier() {
    }
}
