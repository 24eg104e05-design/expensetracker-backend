package com.student.expensetracker.service;

import com.student.expensetracker.entity.Expense;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

/**
 * Streams the caller's filtered expenses as {@code text/csv} with the columns
 * fixed by PRD 4.5: Date, Title, Category, Amount, Description.
 */
@Service
public class CsvExportService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("Date", "Title", "Category", "Amount", "Description")
            .get();

    public void write(OutputStream outputStream, Iterable<Expense> expenses) throws IOException {
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                CSVPrinter printer = new CSVPrinter(writer, FORMAT)) {
            for (Expense expense : expenses) {
                printer.printRecord(
                        expense.getExpenseDate().format(DATE),
                        expense.getTitle(),
                        expense.getCategory().getName(),
                        expense.getAmount().toPlainString(),
                        expense.getDescription() == null ? "" : expense.getDescription());
            }
            printer.flush();
        }
    }

    /** PRD 4.5 requires the filename to embed the export date. */
    public String filename() {
        return "expenses_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv";
    }
}