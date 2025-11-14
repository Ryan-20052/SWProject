package anbd.he191271.controller;

import anbd.he191271.entity.Manager;
import anbd.he191271.service.RevenueService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping("/dashboard")
    public String getRevenueDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "monthly") String timeRange,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            HttpSession session,
            Model model) {

        // Lấy manager từ session
        Manager manager = (Manager) session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }

        LocalDate today = LocalDate.now();

        // Set default date range
        if (startDate == null) {
            startDate = LocalDate.of(2020, 1, 1);
        }
        if (endDate == null) {
            endDate = today;
        }



        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        var dashboard = revenueService.getRevenueDashboard(startDateTime, endDateTime);

        List<Map<String, Object>> allProducts = dashboard.get("revenueByProduct") != null ?
                (List<Map<String, Object>>) dashboard.get("revenueByProduct") :
                List.of();

        // Search
        if (search != null && !search.trim().isEmpty()) {
            allProducts = allProducts.stream()
                    .filter(p -> p.get("productName").toString().toLowerCase().contains(search.toLowerCase().trim()))
                    .collect(Collectors.toList());
        }

        // Sort
        if (sort != null && !sort.trim().isEmpty()) {
            switch (sort) {
                case "name_asc":
                    allProducts.sort((a, b) -> a.get("productName").toString().compareToIgnoreCase(b.get("productName").toString()));
                    break;
                case "name_desc":
                    allProducts.sort((a, b) -> b.get("productName").toString().compareToIgnoreCase(a.get("productName").toString()));
                    break;
                case "revenue_asc":
                    allProducts.sort((a, b) -> {
                        Long revenueA = (Long) a.get("revenue");
                        Long revenueB = (Long) b.get("revenue");
                        return revenueA.compareTo(revenueB);
                    });
                    break;
                case "revenue_desc":
                    allProducts.sort((a, b) -> {
                        Long revenueA = (Long) a.get("revenue");
                        Long revenueB = (Long) b.get("revenue");
                        return revenueB.compareTo(revenueA);
                    });
                    break;
            }
        }

        // Phân trang
        int pageSize = 10;
        int totalItems = allProducts.size();
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / pageSize);

        // Đảm bảo page hợp lệ
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        List<Map<String, Object>> products = allProducts.subList(startIndex, endIndex);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("timeRange", timeRange);
        model.addAttribute("manager", manager);
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("today", today);

        return "revenue";
    }

    @GetMapping("/daily-details")
    public String getDailyRevenueDetails(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session,
            Model model) {

        // Lấy manager từ session
        Manager manager = (Manager) session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }

        LocalDate today = LocalDate.now();

        // Set default date range
        if (startDate == null) {
            startDate = today.minusDays(30);
        }
        if (endDate == null) {
            endDate = today;
        }

        // Đơn giản hóa validation
        if (startDate.isAfter(today)) {
            startDate = today.minusDays(30);
        }
        if (endDate.isAfter(today)) {
            endDate = today;
        }
        if (endDate.isBefore(startDate)) {
            endDate = startDate;
        }

        // Lấy chi tiết doanh thu theo ngày
        var dailyRevenue = revenueService.getDailyRevenueDetails(startDate, endDate);

        model.addAttribute("dailyRevenue", dailyRevenue);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("manager", manager);
        model.addAttribute("today", today);

        return "revenue-daily-details";
    }
}