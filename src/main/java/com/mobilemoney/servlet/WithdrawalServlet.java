package com.mobilemoney.servlet;

import com.mobilemoney.dao.OperationDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/retraits")
public class WithdrawalServlet extends HttpServlet {
    private final OperationDao operationDao = new OperationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("retraits", operationDao.listWithdrawals());
            req.getRequestDispatcher("/retraits/form.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if ("update".equals(action)) {
                operationDao.updateWithdrawal(
                        req.getParameter("idrecep"),
                        req.getParameter("numtel"),
                        Integer.parseInt(req.getParameter("montant"))
                );
            } else if ("delete".equals(action)) {
                operationDao.deleteWithdrawal(req.getParameter("idrecep"));
            } else {
                operationDao.doWithdrawal(
                        req.getParameter("numtel"),
                        Integer.parseInt(req.getParameter("montant"))
                );
            }
            resp.sendRedirect(req.getContextPath() + "/retraits?ok=1");
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            try {
                req.setAttribute("retraits", operationDao.listWithdrawals());
            } catch (Exception ignored) {
            }
            req.getRequestDispatcher("/retraits/form.jsp").forward(req, resp);
        }
    }
}
