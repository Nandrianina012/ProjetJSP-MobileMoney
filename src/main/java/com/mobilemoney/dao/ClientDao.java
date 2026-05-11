package com.mobilemoney.dao;

import com.mobilemoney.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientDao {

    public List<Map<String, Object>> findAll() throws SQLException {
        String sql = "SELECT numtel, nom, sexe, age, solde, mail FROM CLIENT ORDER BY nom";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(clientRow(rs));
            }
            return rows;
        }
    }

    public List<Map<String, Object>> search(String keyword) throws SQLException {
        String sql = "SELECT numtel, nom, sexe, age, solde, mail FROM CLIENT WHERE nom LIKE ? OR numtel LIKE ? ORDER BY nom";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(clientRow(rs));
                }
                return rows;
            }
        }
    }

    public void create(String numtel, String nom, String sexe, int age, int solde, String mail) throws SQLException {
        if (existsByPhone(numtel)) {
            throw new IllegalArgumentException("Ce numéro de téléphone existe déjà.");
        }
        if (mail != null && !mail.trim().isEmpty() && existsByMail(mail.trim(), null)) {
            throw new IllegalArgumentException("Cette adresse e-mail est déjà utilisée.");
        }
        String sql = "INSERT INTO CLIENT(numtel, nom, sexe, age, solde, mail) VALUES(?,?,?,?,?,?)";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numtel);
            ps.setString(2, nom);
            ps.setString(3, sexe);
            ps.setInt(4, age);
            ps.setInt(5, solde);
            ps.setString(6, mail);
            ps.executeUpdate();
        }
    }

    public void update(String numtel, String nom, String sexe, int age, int solde, String mail) throws SQLException {
        if (mail != null && !mail.trim().isEmpty() && existsByMail(mail.trim(), numtel)) {
            throw new IllegalArgumentException("Cette adresse e-mail est déjà utilisée.");
        }
        String sql = "UPDATE CLIENT SET nom=?, sexe=?, age=?, solde=?, mail=? WHERE numtel=?";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, sexe);
            ps.setInt(3, age);
            ps.setInt(4, solde);
            ps.setString(5, mail);
            ps.setString(6, numtel);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Client introuvable pour mise à jour.");
            }
        }
    }

    public void delete(String numtel) throws SQLException {
        if (hasLinkedOperations(numtel)) {
            throw new IllegalArgumentException(
                    "Suppression impossible: ce client est lié à des envois/retraits."
            );
        }
        String sql = "DELETE FROM CLIENT WHERE numtel=?";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numtel);
            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                throw new IllegalArgumentException("Client introuvable pour suppression.");
            }
        }
    }

    public boolean existsByPhone(String numtel) throws SQLException {
        List<String> variants = phoneVariants(numtel);
        String sql = "SELECT 1 FROM CLIENT WHERE numtel IN (" + placeholders(variants.size()) + ") LIMIT 1";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            bindPhoneVariants(ps, variants);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean hasLinkedOperations(String numtel) throws SQLException {
        String sql = "SELECT 1 FROM ENVOI WHERE numEnvoyeur=? OR numRecepteur=? " +
                "UNION ALL SELECT 1 FROM RETRAIT WHERE numtel=? LIMIT 1";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numtel);
            ps.setString(2, numtel);
            ps.setString(3, numtel);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Map<String, Object> findByPhone(Connection cn, String numtel) throws SQLException {
        List<String> variants = phoneVariants(numtel);
        String sql = "SELECT numtel, nom, solde, mail FROM CLIENT WHERE numtel IN (" + placeholders(variants.size()) + ") LIMIT 1";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            bindPhoneVariants(ps, variants);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("numtel", rs.getString("numtel"));
                    map.put("nom", rs.getString("nom"));
                    map.put("solde", rs.getInt("solde"));
                    map.put("mail", rs.getString("mail"));
                    return map;
                }
            }
        }
        return null;
    }

    private List<String> phoneVariants(String raw) {
        String digits = raw == null ? "" : raw.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("Le numéro de téléphone est obligatoire.");
        }

        Set<String> values = new LinkedHashSet<>();
        values.add(digits);

        if (digits.startsWith("261")) {
            String national = digits.substring(3);
            if (national.length() == 9) {
                values.add("0" + national);
            }
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

    private void bindPhoneVariants(PreparedStatement ps, List<String> variants) throws SQLException {
        for (int i = 0; i < variants.size(); i++) {
            ps.setString(i + 1, variants.get(i));
        }
    }

    private boolean existsByMail(String mail, String excludeNumtel) throws SQLException {
        String sql = "SELECT 1 FROM CLIENT WHERE mail=?"
                + (excludeNumtel != null ? " AND numtel<>?" : "")
                + " LIMIT 1";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, mail);
            if (excludeNumtel != null) {
                ps.setString(2, excludeNumtel);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Map<String, Object> clientRow(ResultSet rs) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        map.put("numtel", rs.getString("numtel"));
        map.put("nom", rs.getString("nom"));
        map.put("sexe", rs.getString("sexe"));
        map.put("age", rs.getInt("age"));
        map.put("solde", rs.getInt("solde"));
        map.put("mail", rs.getString("mail"));
        return map;
    }
}
