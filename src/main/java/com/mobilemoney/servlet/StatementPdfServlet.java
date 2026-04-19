package com.mobilemoney.servlet;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mobilemoney.dao.ReportDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@WebServlet("/reports/statement-pdf")
public class StatementPdfServlet extends HttpServlet {
    private final ReportDao reportDao = new ReportDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String numtel = req.getParameter("numtel");
        int year = Integer.parseInt(req.getParameter("year"));
        int month = Integer.parseInt(req.getParameter("month"));
        if (numtel == null || numtel.isBlank()) {
            resp.sendError(400, "numtel requis");
            return;
        }

        try {
            List<Map<String, Object>> rows = reportDao.monthlyStatement(numtel, year, month);
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"releve-" + numtel + "-" + year + "-" + month + ".pdf\"");
            Document doc = new Document();
            PdfWriter.getInstance(doc, resp.getOutputStream());
            doc.open();
            doc.add(new Paragraph("Releve mensuel - Client " + numtel + " - " + month + "/" + year));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.addCell("Date");
            table.addCell("Raison");
            table.addCell("Debit");
            table.addCell("Credit");

            int totalDebit = 0;
            int totalCredit = 0;
            for (Map<String, Object> row : rows) {
                Timestamp ts = (Timestamp) row.get("date");
                int debit = (Integer) row.get("debit");
                int credit = (Integer) row.get("credit");
                totalDebit += debit;
                totalCredit += credit;

                table.addCell(ts.toLocalDateTime().toLocalDate().toString());
                table.addCell(String.valueOf(row.get("raison")));
                table.addCell(String.valueOf(debit));
                table.addCell(String.valueOf(credit));
            }
            doc.add(table);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Total debit : " + totalDebit + " Ar"));
            doc.add(new Paragraph("Total credit : " + totalCredit + " Ar"));
            doc.close();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
