package com.mobilemoney.servlet;

import com.mobilemoney.dao.OperationDao;
import com.mobilemoney.dao.ReportDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private final ReportDao reportDao = new ReportDao();
    private final OperationDao operationDao = new OperationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String dateParam = req.getParameter("date");
            LocalDate date = (dateParam == null || dateParam.isBlank()) ? LocalDate.now() : LocalDate.parse(dateParam);
            req.setAttribute("date", date.toString());
            req.setAttribute("operations", operationDao.operationsByDate(date));
            req.setAttribute("recette", reportDao.recetteTotaleOperateur());
            req.getRequestDispatcher("/reports/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
