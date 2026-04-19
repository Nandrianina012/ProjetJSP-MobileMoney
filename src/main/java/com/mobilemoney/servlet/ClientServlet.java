package com.mobilemoney.servlet;

import com.mobilemoney.dao.ClientDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/clients")
public class ClientServlet extends HttpServlet {
    private final ClientDao clientDao = new ClientDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String q = req.getParameter("q");
            if (q != null && !q.isBlank()) {
                req.setAttribute("clients", clientDao.search(q.trim()));
            } else {
                req.setAttribute("clients", clientDao.findAll());
            }
            req.getRequestDispatcher("/clients/list.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String action = req.getParameter("action");
        try {
            String sexe = normalizeSexe(req.getParameter("sexe"));
            String mail = normalizeMail(req.getParameter("mail"));
            if ("create".equals(action)) {
                clientDao.create(
                        req.getParameter("numtel"),
                        req.getParameter("nom"),
                        sexe,
                        Integer.parseInt(req.getParameter("age")),
                        Integer.parseInt(req.getParameter("solde")),
                        mail
                );
            } else if ("update".equals(action)) {
                clientDao.update(
                        req.getParameter("numtel"),
                        req.getParameter("nom"),
                        sexe,
                        Integer.parseInt(req.getParameter("age")),
                        Integer.parseInt(req.getParameter("solde")),
                        mail
                );
            } else if ("delete".equals(action)) {
                clientDao.delete(req.getParameter("numtel"));
            }
            resp.sendRedirect(req.getContextPath() + "/clients?ok=1");
        } catch (Exception e) {
            try {
                req.setAttribute("clients", clientDao.findAll());
            } catch (SQLException ignored) {
            }
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/clients/list.jsp").forward(req, resp);
        }
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
}
