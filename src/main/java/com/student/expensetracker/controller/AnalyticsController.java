package com.student.expensetracker.controller;

import com.student.expensetracker.dto.AnalyticsOverview;
import com.student.expensetracker.dto.CategorySummary;
import com.student.expensetracker.security.CurrentUser;
import com.student.expensetracker.service.AnalyticsService;
import com.student.expensetracker.service.CsvExportService;
import com.student.expensetracker.service.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Pie-chart aggregation, KPI cards and CSV export (PRD 7.3 / 4.5). */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ExpenseService expenseService;
    private final CsvExportService csvExportService;
    private final CurrentUser currentUser;

    public AnalyticsController(
            AnalyticsService analyticsService,
            ExpenseService expenseService,
            CsvExportService csvExportService,
            CurrentUser currentUser) {
        this.analyticsService = analyticsService;
        this.expenseService = expenseService;
        this.csvExportService = csvExportService;
        this.currentUser = currentUser;
    }

    @GetMapping("/category-summary")
    public List<CategorySummary> categorySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return analyticsService.categorySummary(currentUser.requireId(), startDate, endDate);
    }

    /** Single call powering the KPI cards and the pie chart together. */
    @GetMapping("/overview")
    public AnalyticsOverview overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId) {
        return analyticsService.overview(currentUser.requireId(), startDate, endDate, categoryId);
    }

    @GetMapping("/export/csv")
    public void exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            HttpServletResponse response) throws IOException {

        var expenses = expenseService.listForExport(currentUser.requireId(), startDate, endDate, categoryId);

        response.setContentType(MediaType.parseMediaType("text/csv").toString());
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + csvExportService.filename() + "\"");
        csvExportService.write(response.getOutputStream(), expenses);
    }
}