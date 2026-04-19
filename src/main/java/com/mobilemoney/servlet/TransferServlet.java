package com.mobilemoney.servlet;

import com.mobilemoney.dao.OperationDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/envois")
public class TransferServlet extends HttpServlet {
    private final OperationDao operationDao = new OperationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("envois", operationDao.listTransfers());
            req.getRequestDispatcher("/envois/form.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if ("update".equals(action)) {
                operationDao.updateTransfer(
                        req.getParameter("idEnv"),
                        req.getParameter("numEnvoyeur"),
                        req.getParameter("numRecepteur"),
                        Integer.parseInt(req.getParameter("montant")),
                        "on".equals(req.getParameter("payerFraisRetrait")),
                        req.getParameter("raison")
                );
            } else if ("delete".equals(action)) {
                operationDao.deleteTransfer(req.getParameter("idEnv"));
            } else {
                operationDao.doTransfer(
                        req.getParameter("numEnvoyeur"),
                        req.getParameter("numRecepteur"),
                        Integer.parseInt(req.getParameter("montant")),
                        "on".equals(req.getParameter("payerFraisRetrait")),
                        req.getParameter("raison")
                );
            }
            resp.sendRedirect(req.getContextPath() + "/envois?ok=1");
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            try {
                req.setAttribute("envois", operationDao.listTransfers());
            } catch (Exception ignored) {
            }
            req.getRequestDispatcher("/envois/form.jsp").forward(req, resp);
        }
    }
}
