package com.mobilemoney.dao;

import com.mobilemoney.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDao {

    public int recetteTotaleOperateur() throws SQLException {
        String sql = "SELECT " +
                "(SELECT COALESCE(SUM(fe.frais_env),0) FROM ENVOI e LEFT JOIN FRAIS_ENVOI fe ON e.montant BETWEEN fe.montant1 AND fe.montant2) + " +
                "(SELECT COALESCE(SUM(fr.frais_rec),0) FROM RETRAIT r LEFT JOIN FRAIS_RECEP fr ON r.montant BETWEEN fr.montant1 AND fr.montant2) AS recette";
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
        String sql = "SELECT date_envoi AS d, raison, montant AS credit, 0 AS debit FROM ENVOI WHERE numRecepteur=? AND date_envoi>=? AND date_envoi<? " +
                "UNION ALL " +
                "SELECT date_envoi AS d, raison, 0 AS credit, montant AS debit FROM ENVOI WHERE numEnvoyeur=? AND date_envoi>=? AND date_envoi<? " +
                "UNION ALL " +
                "SELECT daterecep AS d, 'Retrait' AS raison, 0 AS credit, montant AS debit FROM RETRAIT WHERE numtel=? AND daterecep>=? AND daterecep<? " +
                "ORDER BY d ASC";

        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numtel);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(end));
            ps.setString(4, numtel);
            ps.setDate(5, java.sql.Date.valueOf(start));
            ps.setDate(6, java.sql.Date.valueOf(end));
            ps.setString(7, numtel);
            ps.setDate(8, java.sql.Date.valueOf(start));
            ps.setDate(9, java.sql.Date.valueOf(end));
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
}
