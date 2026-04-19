package com.mobilemoney.servlet;

import com.mobilemoney.dao.FeeDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/frais")
public class FeeServlet extends HttpServlet {
    private final FeeDao feeDao = new FeeDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("sendFees", feeDao.listSendFees());
            req.setAttribute("receiveFees", feeDao.listReceiveFees());
            req.getRequestDispatcher("/reports/frais.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if ("createSend".equals(action)) {
                feeDao.createSendFee(req.getParameter("id"), Integer.parseInt(req.getParameter("montant1")),
                        Integer.parseInt(req.getParameter("montant2")), Integer.parseInt(req.getParameter("frais")));
            } else if ("createReceive".equals(action)) {
                feeDao.createReceiveFee(req.getParameter("id"), Integer.parseInt(req.getParameter("montant1")),
                        Integer.parseInt(req.getParameter("montant2")), Integer.parseInt(req.getParameter("frais")));
            } else if ("updateSend".equals(action)) {
                feeDao.updateSendFee(req.getParameter("id"), Integer.parseInt(req.getParameter("montant1")),
                        Integer.parseInt(req.getParameter("montant2")), Integer.parseInt(req.getParameter("frais")));
            } else if ("updateReceive".equals(action)) {
                feeDao.updateReceiveFee(req.getParameter("id"), Integer.parseInt(req.getParameter("montant1")),
                        Integer.parseInt(req.getParameter("montant2")), Integer.parseInt(req.getParameter("frais")));
            } else if ("deleteSend".equals(action)) {
                feeDao.deleteSendFee(req.getParameter("id"));
            } else if ("deleteReceive".equals(action)) {
                feeDao.deleteReceiveFee(req.getParameter("id"));
            }
            resp.sendRedirect(req.getContextPath() + "/frais");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
