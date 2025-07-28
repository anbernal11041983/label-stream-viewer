package br.com.automacaowebia.service;

import br.com.automacaowebia.config.Database;
import br.com.automacaowebia.dto.ProdutoLabelData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProdutoLabelDataService {

    private static final Logger logger = LogManager.getLogger(ProdutoLabelDataService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public ProdutoLabelData findByTemplateId(long templateId) {
        String sql = """
            SELECT codigo_barras,
                   url_qr,
                   sif,
                   sku,
                   data_producao,
                   data_validade,
                   desc_completa,
                   desc_reduzida,
                   peso_kg
              FROM produto_label_data
             WHERE template_id = ?
             ORDER BY criado_em DESC
             LIMIT 1
            """;

        try (Connection conn = Database.getInstance().connectDB(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, templateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String cb = rs.getString("codigo_barras");
                    String qr = rs.getString("url_qr");
                    String sif = rs.getString("sif");
                    String sku = rs.getString("sku");
                    LocalDate dp = parseDate(rs.getString("data_producao"));
                    LocalDate dv = parseDate(rs.getString("data_validade"));
                    String dc = rs.getString("desc_completa");
                    String dr = rs.getString("desc_reduzida");
                    BigDecimal peso = rs.getBigDecimal("peso_kg");

                    return new ProdutoLabelData(
                            cb,
                            qr,
                            sif,
                            sku,
                            dp,
                            dv,
                            dc,
                            dr,
                            peso
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Erro ao buscar ProdutoLabelData para template_id={}", templateId, e);
        }
        return null;
    }

    private LocalDate parseDate(String txt) {
        if (txt == null || txt.isBlank()) {
            return null;
        }
        return LocalDate.parse(txt, DATE_FMT);
    }
}
