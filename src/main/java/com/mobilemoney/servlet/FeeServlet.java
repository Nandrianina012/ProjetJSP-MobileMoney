package com.mobilemoney.servlet;

import com.mobilemoney.dao.FeeDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.Collections;

@WebServlet("/frais")
public class FeeServlet extends HttpServlet {
    private final FeeDao feeDao = new FeeDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("sendFees", feeDao.listSendFees());
            req.setAttribute("receiveFees", feeDao.listReceiveFees());
        } catch (Exception e) {
            req.setAttribute("sendFees", Collections.emptyList());
            req.setAttribute("receiveFees", Collections.emptyList());
            req.setAttribute("error", "Connexion a la base indisponible. Verifiez MySQL (localhost:3306) et db.properties.");
        }
        req.getRequestDispatcher("/reports/frais.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            String successMessage;
            if ("createSend".equals(action)) {
                feeDao.createSendFee(generateFeeId("E"), Integer.parseInt(req.getParameter("montant1")),
                        Integer.parseInt(req.getParameter("montant2")), Integer.parseInt(req.getParameter("frais")));
                successMessage = "Ajout du frais d'envoi avec succes !";
            } else if ("createReceive".equals(action)) {
                feeDao.createReceiveFee(generateFeeId("R"), Integer.parseInt(req.getParameter("montant1")),
                        Integer.parseInt(req.getParameter("montant2")), Integer.parseInt(req.getParameter("frais")));
                successMessage = "Ajout du frais de retrait avec succes !";
            } else if ("updateSend".equals(action)) {
                feeDao.updateSendFee(req.getParameter("id"), Integer.parseInt(req.getParameter("montant1")),
                        Integer.parseInt(req.getParameter("montant2")), Integer.parseInt(req.getParameter("frais")));
                successMessage = "Modification du frais d'envoi avec succes !";
            } else if ("updateReceive".equals(action)) {
                feeDao.updateReceiveFee(req.getParameter("id"), Integer.parseInt(req.getParameter("montant1")),
                        Integer.parseInt(req.getParameter("montant2")), Integer.parseInt(req.getParameter("frais")));
                successMessage = "Modification du frais de retrait avec succes !";
            } else if ("deleteSend".equals(action)) {
                feeDao.deleteSendFee(req.getParameter("id"));
                successMessage = "Suppression du frais d'envoi avec succes !";
            } else if ("deleteReceive".equals(action)) {
                feeDao.deleteReceiveFee(req.getParameter("id"));
                successMessage = "Suppression du frais de retrait avec succes !";
            } else {
                throw new IllegalArgumentException("Action de frais invalide.");
            }
            redirectWithMessage(resp, req.getContextPath() + "/frais", successMessage);
        } catch (Exception e) {
            try {
                req.setAttribute("sendFees", feeDao.listSendFees());
                req.setAttribute("receiveFees", feeDao.listReceiveFees());
            } catch (Exception ignored) {
            }
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/reports/frais.jsp").forward(req, resp);
        }
    }

    private void redirectWithMessage(HttpServletResponse resp, String basePath, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        resp.sendRedirect(basePath + "?ok=1&msg=" + encoded);
    }

    private String generateFeeId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
