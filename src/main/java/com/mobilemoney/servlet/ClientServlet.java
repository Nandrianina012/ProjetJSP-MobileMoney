package com.mobilemoney.servlet;

import com.mobilemoney.dao.ClientDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;

@WebServlet("/clients")
public class ClientServlet extends HttpServlet {
    private final ClientDao clientDao = new ClientDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q");
        try {
            if (q != null && !q.isBlank()) {
                req.setAttribute("clients", clientDao.search(q.trim()));
            } else {
                req.setAttribute("clients", clientDao.findAll());
            }
        } catch (SQLException e) {
            req.setAttribute("clients", Collections.emptyList());
            req.setAttribute("error", "Connexion a la base indisponible. Verifiez MySQL (localhost:3306) et db.properties.");
        }
        req.getRequestDispatcher("/clients/list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String action = req.getParameter("action");
        try {
            String successMessage;
            if ("create".equals(action)) {
                String sexe = normalizeSexe(req.getParameter("sexe"));
                String mail = normalizeMail(req.getParameter("mail"));
                String numtel = normalizeNumtel(req.getParameter("numtel"));
                clientDao.create(
                        numtel,
                        req.getParameter("nom"),
                        sexe,
                        Integer.parseInt(req.getParameter("age")),
                        Integer.parseInt(req.getParameter("solde")),
                        mail
                );
                successMessage = "Ajout avec succes !";
            } else if ("update".equals(action)) {
                String sexe = normalizeSexe(req.getParameter("sexe"));
                String mail = normalizeMail(req.getParameter("mail"));
                String numtel = normalizeNumtel(req.getParameter("numtel"));
                String originalNumtelRaw = req.getParameter("originalNumtel");
                String originalNumtel = (originalNumtelRaw == null || originalNumtelRaw.trim().isEmpty())
                        ? numtel
                        : normalizeNumtel(originalNumtelRaw);
                clientDao.update(
                        originalNumtel,
                        req.getParameter("nom"),
                        sexe,
                        Integer.parseInt(req.getParameter("age")),
                        Integer.parseInt(req.getParameter("solde")),
                        mail
                );
                successMessage = "Modification avec succes !";
            } else if ("delete".equals(action)) {
                clientDao.delete(normalizeNumtel(req.getParameter("numtel")));
                successMessage = "Suppression avec succes !";
            } else {
                throw new IllegalArgumentException("Action client invalide.");
            }
            redirectWithMessage(resp, req.getContextPath() + "/clients", successMessage);
        } catch (Exception e) {
            try {
                req.setAttribute("clients", clientDao.findAll());
            } catch (SQLException ignored) {
            }
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/clients/list.jsp").forward(req, resp);
        }
    }

    private void redirectWithMessage(HttpServletResponse resp, String basePath, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        resp.sendRedirect(basePath + "?ok=1&msg=" + encoded);
    }

    private String normalizeSexe(String sexe) {
        if (sexe == null) {
            throw new IllegalArgumentException("Le sexe est obligatoire (M ou F).");
        }
        String normalized = sexe.trim().toUpperCase();
        if (!"M".equals(normalized) && !"F".equals(normalized)) {
            throw new IllegalArgumentException("Le sexe doit etre M ou F.");
        }
        return normalized;
    }

    private String normalizeMail(String mail) {
        return mail == null ? "" : mail.trim();
    }

    private String normalizeNumtel(String numtel) {
        if (numtel == null || numtel.trim().isEmpty()) {
            throw new IllegalArgumentException("Le numéro de téléphone est obligatoire.");
        }
        String digitsOnly = numtel.replaceAll("\\D", "");
        if (digitsOnly.length() < 9 || digitsOnly.length() > 15) {
            throw new IllegalArgumentException("Le numéro de téléphone est invalide.");
        }
        return digitsOnly;
    }
}
