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
        String sql = "UPDATE CLIENT SET nom=?, sexe=?, age=?, solde=?, mail=? WHERE numtel=?";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, sexe);
            ps.setInt(3, age);
            ps.setInt(4, solde);
            ps.setString(5, mail);
            ps.setString(6, numtel);
            ps.executeUpdate();
        }
    }

    public void delete(String numtel) throws SQLException {
        String sql = "DELETE FROM CLIENT WHERE numtel=?";
        try (Connection cn = DbUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numtel);
            ps.executeUpdate();
        }
    }

    public Map<String, Object> findByPhone(Connection cn, String numtel) throws SQLException {
        String sql = "SELECT numtel, nom, solde, mail FROM CLIENT WHERE numtel=?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, numtel);
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
