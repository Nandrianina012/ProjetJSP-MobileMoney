package com.mobilemoney.servlet;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import java.util.Map;

@WebServlet("/reports/statement-pdf")
public class StatementPdfServlet extends HttpServlet {
    private final ReportDao reportDao = new ReportDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String numtel = normalizeNumtel(req.getParameter("numtel"));
            int year = parseRequiredInt(req.getParameter("year"), "Année invalide.");
            int month = parseRequiredInt(req.getParameter("month"), "Mois invalide (1-12).");
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("Mois invalide (1-12).");
            }

            Map<String, Object> client = reportDao.findClientSummary(numtel);
            if (client == null) {
                throw new IllegalArgumentException("Client introuvable.");
            }
            List<Map<String, Object>> rows = reportDao.monthlyStatement(numtel, year, month);
            resp.setContentType("application/pdf");
            String clientPhone = String.valueOf(client.get("numtel"));
            String clientName = String.valueOf(client.get("nom"));
            resp.setHeader("Content-Disposition", "attachment; filename=\"releve-" + clientPhone + "-" + year + "-" + month + ".pdf\"");
            Document doc = new Document();
            PdfWriter.getInstance(doc, resp.getOutputStream());
            doc.open();

            Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
            Paragraph dateTitle = new Paragraph("Date : " + monthYearLabel(month, year), titleFont);
            dateTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(dateTitle);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Contact : " + clientPhone));
            doc.add(new Paragraph(clientName));
            doc.add(new Paragraph(client.get("age") + " ans"));
            doc.add(new Paragraph("M".equals(String.valueOf(client.get("sexe"))) ? "Masculin" : "Feminin"));
            doc.add(new Paragraph("Solde actuel : " + amountFr((Integer) client.get("solde")) + " Ariary"));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.addCell("Date");
            table.addCell("Raison");
            table.addCell("Debit");
            table.addCell("Credit");

            int totalDebit = 0;
            int totalCredit = 0;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Map<String, Object> row : rows) {
                Timestamp ts = (Timestamp) row.get("date");
                int debit = (Integer) row.get("debit");
                int credit = (Integer) row.get("credit");
                totalDebit += debit;
                totalCredit += credit;

                LocalDateTime ldt = ts.toLocalDateTime();
                table.addCell(ldt.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                table.addCell(String.valueOf(row.get("raison")));
                table.addCell(debit == 0 ? "" : amountFr(debit));
                table.addCell(credit == 0 ? "" : amountFr(credit));
            }
            doc.add(table);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Total Débit : " + amountFr(totalDebit) + " Ar"));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Total Crédit : " + amountFr(totalCredit) + " Ar"));
            doc.close();
        } catch (IllegalArgumentException e) {
            resp.sendError(400, e.getMessage());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private String normalizeNumtel(String numtel) {
        if (numtel == null || numtel.trim().isEmpty()) {
            throw new IllegalArgumentException("numtel requis");
        }
        return numtel.replaceAll("\\D", "");
    }

    private int parseRequiredInt(String raw, String message) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    private String amountFr(int amount) {
        return String.format(Locale.FRANCE, "%,d", amount).replace(',', '.');
    }

    private String monthYearLabel(int month, int year) {
        Month m = Month.of(month);
        String name = m.getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH);
        if (name.isEmpty()) return month + "/" + year;
        String cap = name.substring(0, 1).toUpperCase(Locale.FRENCH) + name.substring(1);
        return cap + " " + year;
    }
}
