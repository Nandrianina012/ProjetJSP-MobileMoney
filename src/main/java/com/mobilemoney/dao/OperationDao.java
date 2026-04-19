package com.mobilemoney.dao;

import com.mobilemoney.util.DbUtil;
import com.mobilemoney.util.MailUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OperationDao {
    private final ClientDao clientDao = new ClientDao();

    public void doTransfer(String numEnvoyeur, String numRecepteur, int montant, boolean payerFraisRetrait, String raison) throws SQLException {
        try (Connection cn = DbUtil.getConnection()) {
            cn.setAutoCommit(false);
            try {
                Map<String, Object> envoyeur = clientDao.findByPhone(cn, numEnvoyeur);
                Map<String, Object> recepteur = clientDao.findByPhone(cn, numRecepteur);
                if (envoyeur == null || recepteur == null) {
                    throw new IllegalArgumentException("Numéro envoyeur/récepteur invalide.");
                }

                int fraisEnvoi = getTransferFee(cn, montant);
                int fraisRetrait = getWithdrawalFee(cn, montant);
                int debitEnv = montant + fraisEnvoi + (payerFraisRetrait ? fraisRetrait : 0);
                int soldeEnv = (int) envoyeur.get("solde");
                if (soldeEnv < debitEnv) {
                    throw new IllegalArgumentException("Solde insuffisant pour l'envoi.");
                }

                updateBalance(cn, numEnvoyeur, -debitEnv);
                updateBalance(cn, numRecepteur, montant);

                String id = "E-" + UUID.randomUUID().toString().substring(0, 8);
                String sql = "INSERT INTO ENVOI(idEnv, numEnvoyeur, numRecepteur, montant, date_envoi, payer_frais_retrait, raison) VALUES(?,?,?,?,?,?,?)";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, id);
                    ps.setString(2, numEnvoyeur);
                    ps.setString(3, numRecepteur);
                    ps.setInt(4, montant);
                    ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                    ps.setBoolean(6, payerFraisRetrait);
                    ps.setString(7, raison);
                    ps.executeUpdate();
                }

                cn.commit();

                String from = "noreply@mobilemoney.local";
                MailUtil.sendMail("localhost", 25, from, (String) envoyeur.get("mail"), "Notification envoi",
                        "Vous avez envoye " + montant + " Ar a " + numRecepteur + ". Frais envoi: " + fraisEnvoi + " Ar.");
                MailUtil.sendMail("localhost", 25, from, (String) recepteur.get("mail"), "Notification reception",
                        "Vous avez recu " + montant + " Ar de " + numEnvoyeur + ".");
            } catch (Exception e) {
                cn.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw new IllegalArgumentException(e.getMessage(), e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void doWithdrawal(String numtel, int montant) throws SQLException {
        try (Connection cn = DbUtil.getConnection()) {
            cn.setAutoCommit(false);
            try {
                Map<String, Object> client = clientDao.findByPhone(cn, numtel);
                if (client == null) {
                    throw new IllegalArgumentException("Client introuvable.");
                }

                int fraisRetrait = getWithdrawalFee(cn, montant);
                int debit = montant + fraisRetrait;
                int solde = (int) client.get("solde");
                if (solde < debit) {
                    throw new IllegalArgumentException("Solde insuffisant pour ce retrait.");
                }

                updateBalance(cn, numtel, -debit);
                String sql = "INSERT INTO RETRAIT(idrecep, numtel, montant, daterecep) VALUES(?,?,?,?)";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, "R-" + UUID.randomUUID().toString().substring(0, 8));
                    ps.setString(2, numtel);
                    ps.setInt(3, montant);
                    ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    ps.executeUpdate();
                }
                cn.commit();
            } catch (Exception e) {
                cn.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw new IllegalArgumentException(e.getMessage(), e);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public List<Map<String, Object>> operationsByDate(LocalDate date) throws SQLException {
        String sql = "SELECT idEnv AS id, date_envoi AS d, 'ENVOI' AS type, numEnvoyeur AS principal, numRecepteur AS secondaire, montant FROM ENVOI WHERE DATE(date_envoi)=? " +
                "UNION ALL " +
                "SELECT idrecep AS id, daterecep AS d, 'RETRAIT' AS type, numtel AS principal, '' AS secondaire, montant FROM RETRAIT WHERE DATE(daterecep)=? " +
                "ORDER BY d DESC";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getString("id"));
                    map.put("date", rs.getTimestamp("d"));
                    map.put("type", rs.getString("type"));
                    map.put("principal", rs.getString("principal"));
                    map.put("secondaire", rs.getString("secondaire"));
                    map.put("montant", rs.getInt("montant"));
                    rows.add(map);
                }
                return rows;
            }
        }
    }

    public List<Map<String, Object>> listTransfers() throws SQLException {
        String sql = "SELECT idEnv, numEnvoyeur, numRecepteur, montant, date_envoi, payer_frais_retrait, raison FROM ENVOI ORDER BY date_envoi DESC";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("idEnv", rs.getString("idEnv"));
                row.put("numEnvoyeur", rs.getString("numEnvoyeur"));
                row.put("numRecepteur", rs.getString("numRecepteur"));
                row.put("montant", rs.getInt("montant"));
                row.put("dateEnvoi", rs.getTimestamp("date_envoi"));
                row.put("payerFraisRetrait", rs.getBoolean("payer_frais_retrait"));
                row.put("raison", rs.getString("raison"));
                rows.add(row);
            }
            return rows;
        }
    }

    public List<Map<String, Object>> listWithdrawals() throws SQLException {
        String sql = "SELECT idrecep, numtel, montant, daterecep FROM RETRAIT ORDER BY daterecep DESC";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("idrecep", rs.getString("idrecep"));
                row.put("numtel", rs.getString("numtel"));
                row.put("montant", rs.getInt("montant"));
                row.put("daterecep", rs.getTimestamp("daterecep"));
                rows.add(row);
            }
            return rows;
        }
    }

    public void updateTransfer(String idEnv, String numEnvoyeur, String numRecepteur, int montant, boolean payerFraisRetrait, String raison) throws SQLException {
        String sql = "UPDATE ENVOI SET numEnvoyeur=?, numRecepteur=?, montant=?, payer_frais_retrait=?, raison=? WHERE idEnv=?";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numEnvoyeur);
            ps.setString(2, numRecepteur);
            ps.setInt(3, montant);
            ps.setBoolean(4, payerFraisRetrait);
            ps.setString(5, raison);
            ps.setString(6, idEnv);
            ps.executeUpdate();
        }
    }

    public void deleteTransfer(String idEnv) throws SQLException {
        String sql = "DELETE FROM ENVOI WHERE idEnv=?";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idEnv);
            ps.executeUpdate();
        }
    }

    public void updateWithdrawal(String idRecep, String numtel, int montant) throws SQLException {
        String sql = "UPDATE RETRAIT SET numtel=?, montant=? WHERE idrecep=?";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numtel);
            ps.setInt(2, montant);
            ps.setString(3, idRecep);
            ps.executeUpdate();
        }
    }

    public void deleteWithdrawal(String idRecep) throws SQLException {
        String sql = "DELETE FROM RETRAIT WHERE idrecep=?";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idRecep);
            ps.executeUpdate();
        }
    }

    private int getTransferFee(Connection cn, int montant) throws SQLException {
        String sql = "SELECT frais_env FROM FRAIS_ENVOI WHERE ? BETWEEN montant1 AND montant2 LIMIT 1";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, montant);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    private int getWithdrawalFee(Connection cn, int montant) throws SQLException {
        String sql = "SELECT frais_rec FROM FRAIS_RECEP WHERE ? BETWEEN montant1 AND montant2 LIMIT 1";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, montant);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    private void updateBalance(Connection cn, String numtel, int delta) throws SQLException {
        String sql = "UPDATE CLIENT SET solde = solde + ? WHERE numtel=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, numtel);
            ps.executeUpdate();
        }
    }
}
