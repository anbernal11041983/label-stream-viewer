package br.com.automacaowebia.parser;

import br.com.automacaowebia.dto.ProdutoLabelData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.*;

public final class ZplParser {

    private static final Pattern CMD = Pattern.compile("\\^(?<cmd>[A-Z]{2})(?<args>[^\\^]*)");
    private static final Pattern FD_FIELD =
        Pattern.compile("\\^FD(.*?)\\^FS", Pattern.DOTALL);

    public static ProdutoLabelData parse(Path zplFile) throws IOException {
        String zpl = Files.readString(zplFile, StandardCharsets.UTF_8);  // Java 11 API :contentReference[oaicite:2]{index=2}
        Map<String, String> bucket = new HashMap<>();

        Matcher m = CMD.matcher(zpl);
        while (m.find()) {
            String cmd = m.group("cmd");
            if (!"FD".equals(cmd)) {
                continue; // só queremos ^FD
            }
            int end = zpl.indexOf("^FS", m.end()); // ^FS encerra o campo :contentReference[oaicite:3]{index=3}
            if (end < 0) {
                continue;               // malformado
            }
            String texto = zpl.substring(m.end(), end).trim();
            ZplFieldClassifier.feed(bucket, texto);
        }
        return buildDto(bucket);
    }

    private static LocalDate parseDateOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalDate.parse(raw, ZplFieldClassifier.FMT);
    }

    private static ProdutoLabelData buildDto(Map<String, String> v) {
        return new ProdutoLabelData(
                v.get("codigoBarras"),
                v.get("urlQrCode"),
                v.get("sif"),
                v.get("sku"),
                parseDateOrNull(v.get("dataProd")),
                parseDateOrNull(v.get("dataVal")),
                v.get("descCompleta"),
                v.get("descReduzida"),
                ZplFieldClassifier.peso(v.get("peso"))
        );
    }

    public static ProdutoLabelData parseFromString(String raw) {
        // 1) Mantém só o trecho ZPL (entre ^XA e ^XZ). Se não existir, usa tudo.
        int ini = raw.indexOf("^XA");
        int fim = raw.lastIndexOf("^XZ");
        String zpl = (ini >= 0 && fim > ini) ? raw.substring(ini, fim) : raw;

        Map<String, String> bucket = new HashMap<>();

        // 2) Captura cada campo ^FD ... ^FS
        Matcher m = FD_FIELD.matcher(zpl);
        while (m.find()) {
            String texto = m.group(1).trim();
            if (!texto.isEmpty()) {
                ZplFieldClassifier.feed(bucket, texto);
            }
        }
        return buildDto(bucket);
    }

    private ZplParser() {
    }
}
