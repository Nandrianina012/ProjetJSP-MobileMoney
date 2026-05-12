package com.mobilemoney.dao;

import com.mobilemoney.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportDao {

    public int recetteTotaleOperateur() throws SQLException {
        // Recette = frais d'envoi (chaque ENVOI) + frais de retrait (chaque RETRAIT)
        // + frais de retrait encaissés via ENVOI lorsque l'expéditeur paie aussi les frais de retrait du destinataire
        // (pas de ligne RETRAIT dans ce cas, donc absent du 2e sous-total sans ce complément).
        String sql = "SELECT " +
                "(SELECT COALESCE(SUM(fe.frais_env),0) FROM ENVOI e LEFT JOIN FRAIS_ENVOI fe ON e.montant BETWEEN fe.montant1 AND fe.montant2) + " +
                "(SELECT COALESCE(SUM(fr.frais_rec),0) FROM RETRAIT r LEFT JOIN FRAIS_RECEP fr ON r.montant BETWEEN fr.montant1 AND fr.montant2) + " +
                "(SELECT COALESCE(SUM(fr2.frais_rec),0) FROM ENVOI e2 LEFT JOIN FRAIS_RECEP fr2 ON e2.montant BETWEEN fr2.montant1 AND fr2.montant2 WHERE e2.payer_frais_retrait = 1) "
                + "AS recette";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("recette");
            }
            return 0;
        }
    }

    public List<Map<String, Object>> monthlyStatement(String numtel, int year, int month) throws SQLException {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);
        List<String> variants = phoneVariants(numtel);
        String inClause = placeholders(variants.size());
        String sql = "SELECT date_envoi AS d, raison, montant AS credit, 0 AS debit FROM ENVOI WHERE numRecepteur IN (" + inClause + ") AND date_envoi>=? AND date_envoi<? " +
                "UNION ALL " +
                "SELECT date_envoi AS d, raison, 0 AS credit, montant AS debit FROM ENVOI WHERE numEnvoyeur IN (" + inClause + ") AND date_envoi>=? AND date_envoi<? " +
                "UNION ALL " +
                "SELECT daterecep AS d, 'Retrait' AS raison, 0 AS credit, montant AS debit FROM RETRAIT WHERE numtel IN (" + inClause + ") AND daterecep>=? AND daterecep<? " +
                "ORDER BY d ASC";

        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int idx = 1;
            for (String v : variants) ps.setString(idx++, v);
            ps.setDate(idx++, java.sql.Date.valueOf(start));
            ps.setDate(idx++, java.sql.Date.valueOf(end));
            for (String v : variants) ps.setString(idx++, v);
            ps.setDate(idx++, java.sql.Date.valueOf(start));
            ps.setDate(idx++, java.sql.Date.valueOf(end));
            for (String v : variants) ps.setString(idx++, v);
            ps.setDate(idx++, java.sql.Date.valueOf(start));
            ps.setDate(idx, java.sql.Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("date", rs.getTimestamp("d"));
                    row.put("raison", rs.getString("raison"));
                    row.put("debit", rs.getInt("debit"));
                    row.put("credit", rs.getInt("credit"));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    public Map<String, Object> findClientSummary(String numtel) throws SQLException {
        List<String> variants = phoneVariants(numtel);
        String sql = "SELECT numtel, nom, age, sexe, solde FROM CLIENT WHERE numtel IN (" + placeholders(variants.size()) + ") LIMIT 1";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int idx = 1;
            for (String v : variants) ps.setString(idx++, v);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("numtel", rs.getString("numtel"));
                    row.put("nom", rs.getString("nom"));
                    row.put("age", rs.getInt("age"));
                    row.put("sexe", rs.getString("sexe"));
                    row.put("solde", rs.getInt("solde"));
                    return row;
                }
                return null;
            }
        }
    }

    private List<String> phoneVariants(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        Set<String> values = new LinkedHashSet<>();
        values.add(digits);
        if (digits.startsWith("261")) {
            String national = digits.substring(3);
            if (national.length() == 9) values.add("0" + national);
            values.add(national);
        } else if (digits.startsWith("0") && digits.length() == 10) {
            String national = digits.substring(1);
            values.add(national);
            values.add("261" + national);
        } else if (digits.length() == 9) {
            values.add("0" + digits);
            values.add("261" + digits);
        }
        return new ArrayList<>(values);
    }

    private String placeholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(", ");
            sb.append("?");
        }
        return sb.toString();
    }
}
