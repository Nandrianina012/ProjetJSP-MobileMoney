package com.mobilemoney.servlet;

import com.mobilemoney.dao.OperationDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@WebServlet("/retraits")
public class WithdrawalServlet extends HttpServlet {
    private final OperationDao operationDao = new OperationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("retraits", operationDao.listWithdrawals());
        } catch (Exception e) {
            req.setAttribute("retraits", Collections.emptyList());
            req.setAttribute("error", "Connexion a la base indisponible. Verifiez MySQL (localhost:3306) et db.properties.");
        }
        req.getRequestDispatcher("/retraits/form.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if (action != null && !action.isBlank()) {
                throw new IllegalArgumentException("La modification/suppression de retrait est desactivee pour proteger la coherence des soldes.");
            } else {
                operationDao.doWithdrawal(
                        normalizeNumtel(req.getParameter("numtel")),
                        Integer.parseInt(req.getParameter("montant"))
                );
            }
            redirectWithMessage(resp, req.getContextPath() + "/retraits", "Retrait enregistre avec succes !");
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            try {
                req.setAttribute("retraits", operationDao.listWithdrawals());
            } catch (Exception ignored) {
            }
            req.getRequestDispatcher("/retraits/form.jsp").forward(req, resp);
        }
    }

    private void redirectWithMessage(HttpServletResponse resp, String basePath, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        resp.sendRedirect(basePath + "?ok=1&msg=" + encoded);
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
