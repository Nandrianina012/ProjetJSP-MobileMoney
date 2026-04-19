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
        execute("INSERT INTO FRAIS_ENVOI(idEnv,montant1,montant2,frais_env) VALUES(?,?,?,?)", id, montant1, montant2, frais);
    }

    public void createReceiveFee(String id, int montant1, int montant2, int frais) throws SQLException {
        execute("INSERT INTO FRAIS_RECEP(idRec,montant1,montant2,frais_rec) VALUES(?,?,?,?)", id, montant1, montant2, frais);
    }

    public void deleteSendFee(String id) throws SQLException {
        execute("DELETE FROM FRAIS_ENVOI WHERE idEnv=?", id);
    }

    public void deleteReceiveFee(String id) throws SQLException {
        execute("DELETE FROM FRAIS_RECEP WHERE idRec=?", id);
    }

    public void updateSendFee(String id, int montant1, int montant2, int frais) throws SQLException {
        execute("UPDATE FRAIS_ENVOI SET montant1=?, montant2=?, frais_env=? WHERE idEnv=?", montant1, montant2, frais, id);
    }

    public void updateReceiveFee(String id, int montant1, int montant2, int frais) throws SQLException {
        execute("UPDATE FRAIS_RECEP SET montant1=?, montant2=?, frais_rec=? WHERE idRec=?", montant1, montant2, frais, id);
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
