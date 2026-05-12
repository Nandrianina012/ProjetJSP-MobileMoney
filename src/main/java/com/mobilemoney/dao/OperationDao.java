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
                String envoyeurNumtelDb = (String) envoyeur.get("numtel");
                String recepteurNumtelDb = (String) recepteur.get("numtel");

                int fraisEnvoi = getTransferFee(cn, montant);
                int fraisRetrait = getWithdrawalFee(cn, montant);
                int debitEnv = montant + fraisEnvoi + (payerFraisRetrait ? fraisRetrait : 0);
                int soldeEnv = (int) envoyeur.get("solde");
                if (soldeEnv < debitEnv) {
                    throw new IllegalArgumentException("Solde insuffisant pour l'envoi.");
                }

                updateBalance(cn, envoyeurNumtelDb, -debitEnv);
                updateBalance(cn, recepteurNumtelDb, montant);

                String id = "E-" + UUID.randomUUID().toString().substring(0, 8);
                String sql = "INSERT INTO ENVOI(idEnv, numEnvoyeur, numRecepteur, montant, date_envoi, payer_frais_retrait, raison) VALUES(?,?,?,?,?,?,?)";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, id);
                    ps.setString(2, envoyeurNumtelDb);
                    ps.setString(3, recepteurNumtelDb);
                    ps.setInt(4, montant);
                    ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                    ps.setBoolean(6, payerFraisRetrait);
                    ps.setString(7, raison);
                    ps.executeUpdate();
                }

                cn.commit();

                String from = "noreply@mobilemoney.local";
                String mailEnvoyeur = stringValue(envoyeur.get("mail"));
                String mailRecepteur = stringValue(recepteur.get("mail"));
                int fraisSupportesEnvoyeur = fraisEnvoi + (payerFraisRetrait ? fraisRetrait : 0);
                int totalDebiteEnvoyeur = montant + fraisSupportesEnvoyeur;
                String sujetEnvoyeur = "Notification envoi - " + montant + " Ar";
                String corpsEnvoyeur = "Bonjour,\n\n"
                        + "Votre envoi d'argent a ete enregistre.\n"
                        + "Numero envoyeur : " + envoyeurNumtelDb + "\n"
                        + "Numero destinataire : " + recepteurNumtelDb + "\n"
                        + "Montant envoye : " + montant + " Ar\n"
                        + "Frais d'envoi : " + fraisEnvoi + " Ar\n"
                        + "Frais de retrait pris en charge : " + (payerFraisRetrait ? fraisRetrait + " Ar" : "Non") + "\n"
                        + "Total debite : " + totalDebiteEnvoyeur + " Ar\n\n"
                        + "Merci d'utiliser Mobile Money.";

                String sujetRecepteur = "Notification reception - " + montant + " Ar";
                String corpsRecepteur = "Bonjour,\n\n"
                        + "Vous avez recu un envoi d'argent.\n"
                        + "Numero expediteur : " + envoyeurNumtelDb + "\n"
                        + "Numero destinataire : " + recepteurNumtelDb + "\n"
                        + "Montant recu : " + montant + " Ar\n"
                        + "Frais de retrait a votre charge : " + (payerFraisRetrait ? "0 Ar (pris en charge par l'expediteur)" : fraisRetrait + " Ar") + "\n"
                        + "Montant net au retrait : " + (montant - (payerFraisRetrait ? 0 : fraisRetrait)) + " Ar\n\n"
                        + "Merci d'utiliser Mobile Money.";

                if (!mailEnvoyeur.isBlank()) {
                    MailUtil.sendMail("localhost", 25, from, mailEnvoyeur, sujetEnvoyeur, corpsEnvoyeur);
                }
                if (!mailRecepteur.isBlank()) {
                    MailUtil.sendMail("localhost", 25, from, mailRecepteur, sujetRecepteur, corpsRecepteur);
                }
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
                String numtelDb = (String) client.get("numtel");

                int fraisRetrait = getWithdrawalFee(cn, montant);
                int debit = montant + fraisRetrait;
                int solde = (int) client.get("solde");
                if (solde < debit) {
                    throw new IllegalArgumentException("Solde insuffisant pour ce retrait.");
                }

                updateBalance(cn, numtelDb, -debit);
                String sql = "INSERT INTO RETRAIT(idrecep, numtel, montant, daterecep) VALUES(?,?,?,?)";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, "R-" + UUID.randomUUID().toString().substring(0, 8));
                    ps.setString(2, numtelDb);
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

    /**
     * Totaux cumulés sur toutes les opérations enregistrées (envois + retraits).
     */
    public Map<String, Integer> operationsAggregateAllTime() throws SQLException {
        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM ENVOI) + (SELECT COUNT(*) FROM RETRAIT) AS ops_count, " +
                "(SELECT COUNT(*) FROM ENVOI) AS envois_count, " +
                "(SELECT COALESCE(SUM(montant), 0) FROM ENVOI) AS envois_montant, " +
                "(SELECT COUNT(*) FROM RETRAIT) AS retraits_count, " +
                "(SELECT COALESCE(SUM(montant), 0) FROM RETRAIT) AS retraits_montant";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Map<String, Integer> out = new HashMap<>();
            if (rs.next()) {
                out.put("opsCount", rs.getInt("ops_count"));
                out.put("envoisCount", rs.getInt("envois_count"));
                out.put("envoisMontant", rs.getInt("envois_montant"));
                out.put("retraitsCount", rs.getInt("retraits_count"));
                out.put("retraitsMontant", rs.getInt("retraits_montant"));
            } else {
                out.put("opsCount", 0);
                out.put("envoisCount", 0);
                out.put("envoisMontant", 0);
                out.put("retraitsCount", 0);
                out.put("retraitsMontant", 0);
            }
            return out;
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

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
