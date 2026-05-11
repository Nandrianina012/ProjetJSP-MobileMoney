package com.mobilemoney.dao;

import com.mobilemoney.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeeDao {
    public List<Map<String, Object>> listSendFees() throws SQLException {
        return list("SELECT idEnv AS id, montant1, montant2, frais_env AS frais FROM FRAIS_ENVOI ORDER BY montant1");
    }

    public List<Map<String, Object>> listReceiveFees() throws SQLException {
        return list("SELECT idRec AS id, montant1, montant2, frais_rec AS frais FROM FRAIS_RECEP ORDER BY montant1");
    }

    public void createSendFee(String id, int montant1, int montant2, int frais) throws SQLException {
        validateFeeRange("FRAIS_ENVOI", "idEnv", id, montant1, montant2, frais);
        execute("INSERT INTO FRAIS_ENVOI(idEnv,montant1,montant2,frais_env) VALUES(?,?,?,?)", id, montant1, montant2, frais);
    }

    public void createReceiveFee(String id, int montant1, int montant2, int frais) throws SQLException {
        validateFeeRange("FRAIS_RECEP", "idRec", id, montant1, montant2, frais);
        execute("INSERT INTO FRAIS_RECEP(idRec,montant1,montant2,frais_rec) VALUES(?,?,?,?)", id, montant1, montant2, frais);
    }

    public void deleteSendFee(String id) throws SQLException {
        execute("DELETE FROM FRAIS_ENVOI WHERE idEnv=?", id);
    }

    public void deleteReceiveFee(String id) throws SQLException {
        execute("DELETE FROM FRAIS_RECEP WHERE idRec=?", id);
    }

    public void updateSendFee(String id, int montant1, int montant2, int frais) throws SQLException {
        validateFeeRange("FRAIS_ENVOI", "idEnv", id, montant1, montant2, frais);
        execute("UPDATE FRAIS_ENVOI SET montant1=?, montant2=?, frais_env=? WHERE idEnv=?", montant1, montant2, frais, id);
    }

    public void updateReceiveFee(String id, int montant1, int montant2, int frais) throws SQLException {
        validateFeeRange("FRAIS_RECEP", "idRec", id, montant1, montant2, frais);
        execute("UPDATE FRAIS_RECEP SET montant1=?, montant2=?, frais_rec=? WHERE idRec=?", montant1, montant2, frais, id);
    }

    private void validateFeeRange(String table, String idColumn, String id, int montant1, int montant2, int frais) throws SQLException {
        if (montant1 < 0 || montant2 < 0 || frais < 0) {
            throw new IllegalArgumentException("Les montants et les frais doivent etre positifs.");
        }
        if (montant1 > montant2) {
            throw new IllegalArgumentException("Le montant minimum doit etre inferieur ou egal au montant maximum.");
        }

        String sql = "SELECT 1 FROM " + table + " WHERE NOT (montant2 < ? OR montant1 > ?) AND " + idColumn + " <> ? LIMIT 1";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, montant1);
            ps.setInt(2, montant2);
            ps.setString(3, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Cette tranche chevauche une tranche existante.");
                }
            }
        }
    }

    private List<Map<String, Object>> list(String sql) throws SQLException {
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getString("id"));
                map.put("montant1", rs.getInt("montant1"));
                map.put("montant2", rs.getInt("montant2"));
                map.put("frais", rs.getInt("frais"));
                rows.add(map);
            }
            return rows;
        }
    }

    private void execute(String sql, Object... args) throws SQLException {
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            ps.executeUpdate();
        }
    }
}
