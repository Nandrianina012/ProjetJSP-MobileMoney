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
import java.util.List;
import java.util.Map;
import java.util.Collections;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private final ReportDao reportDao = new ReportDao();
    private final OperationDao operationDao = new OperationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String dateParam = req.getParameter("date");
        LocalDate date = (dateParam == null || dateParam.isBlank()) ? LocalDate.now() : LocalDate.parse(dateParam);
        req.setAttribute("date", date.toString());
        try {
            List<Map<String, Object>> operations = operationDao.operationsByDate(date);
            int envoisCount = 0;
            int retraitsCount = 0;
            int envoisMontant = 0;
            int retraitsMontant = 0;
            for (Map<String, Object> o : operations) {
                String type = String.valueOf(o.get("type"));
                int montant = (Integer) o.get("montant");
                if ("ENVOI".equals(type)) {
                    envoisCount++;
                    envoisMontant += montant;
                } else {
                    retraitsCount++;
                    retraitsMontant += montant;
                }
            }

            req.setAttribute("operations", operations);
            req.setAttribute("recette", reportDao.recetteTotaleOperateur());
            req.setAttribute("opsCount", operations.size());
            req.setAttribute("envoisCount", envoisCount);
            req.setAttribute("retraitsCount", retraitsCount);
            req.setAttribute("envoisMontant", envoisMontant);
            req.setAttribute("retraitsMontant", retraitsMontant);
        } catch (Exception e) {
            req.setAttribute("operations", Collections.emptyList());
            req.setAttribute("recette", 0);
            req.setAttribute("opsCount", 0);
            req.setAttribute("envoisCount", 0);
            req.setAttribute("retraitsCount", 0);
            req.setAttribute("envoisMontant", 0);
            req.setAttribute("retraitsMontant", 0);
            req.setAttribute("error", "Connexion a la base indisponible. Verifiez MySQL (localhost:3306) et la configuration db.properties.");
        }
        req.getRequestDispatcher("/reports/dashboard.jsp").forward(req, resp);
    }
}
