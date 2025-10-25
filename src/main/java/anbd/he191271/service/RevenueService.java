package anbd.he191271.service;

import anbd.he191271.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final OrderRepository orderRepository;

    public Map<String, Object> getRevenueDashboard(LocalDateTime startDate,
                                                   LocalDateTime endDate,
                                                   String timeRange) {
        Map<String, Object> result = new HashMap<>();

        System.out.println("=== REVENUE DASHBOARD DATA ===");
        System.out.println("Start Date: " + startDate);
        System.out.println("End Date: " + endDate);
        System.out.println("Time Range: " + timeRange);

        // Doanh thu hiện tại
        Long currentRevenue = orderRepository.getTotalRevenueByPeriod(startDate, endDate);
        System.out.println("Current Revenue: " + currentRevenue);

        // Doanh thu kỳ trước (dựa trên timeRange)
        Long previousRevenue = getPreviousPeriodRevenue(startDate, endDate, timeRange);
        System.out.println("Previous Revenue: " + previousRevenue);

        // Tính % tăng trưởng
        Double growthRate = calculateGrowthRate(currentRevenue, previousRevenue);
        System.out.println("Growth Rate: " + growthRate);

        // Biểu đồ xu hướng
        List<Map<String, Object>> trendData = getRevenueTrend(startDate, endDate, timeRange);
        System.out.println("Trend Data size: " + (trendData != null ? trendData.size() : 0));

        // Doanh thu theo sản phẩm
        List<Map<String, Object>> revenueByProduct = getRevenueByProduct(startDate, endDate);
        System.out.println("Product Revenue size: " + (revenueByProduct != null ? revenueByProduct.size() : 0));

        result.put("totalRevenue", currentRevenue);
        result.put("previousRevenue", previousRevenue);
        result.put("growthRate", Math.round(growthRate * 100.0) / 100.0);
        result.put("trendData", trendData);
        result.put("revenueByProduct", revenueByProduct);

        return result;
    }

    private Long getPreviousPeriodRevenue(LocalDateTime startDate, LocalDateTime endDate, String timeRange) {
        long daysBetween = java.time.Duration.between(startDate, endDate).toDays();

        LocalDateTime previousStartDate;
        LocalDateTime previousEndDate;

        switch (timeRange.toLowerCase()) {
            case "daily":
                previousStartDate = startDate.minusDays(1);
                previousEndDate = endDate.minusDays(1);
                break;
            case "weekly":
                previousStartDate = startDate.minusWeeks(1);
                previousEndDate = endDate.minusWeeks(1);
                break;
            case "monthly":
                previousStartDate = startDate.minusMonths(1);
                previousEndDate = endDate.minusMonths(1);
                break;
            default:
                previousStartDate = startDate.minusDays(daysBetween);
                previousEndDate = endDate.minusDays(daysBetween);
        }

        return orderRepository.getRevenuePreviousPeriod(previousStartDate, previousEndDate);
    }

    private Double calculateGrowthRate(Long currentRevenue, Long previousRevenue) {
        if (previousRevenue == null || previousRevenue == 0) {
            return currentRevenue > 0 ? 100.0 : 0.0;
        }
        return ((currentRevenue - previousRevenue) * 100.0) / previousRevenue;
    }

    private List<Map<String, Object>> getRevenueTrend(LocalDateTime startDate,
                                                      LocalDateTime endDate,
                                                      String timeRange) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        List<Object[]> rawData;

        switch (timeRange.toLowerCase()) {
            case "daily":
                rawData = orderRepository.getDailyRevenue(startDate, endDate);
                for (Object[] data : rawData) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("period", data[0].toString());
                    item.put("revenue", data[1]);
                    trendData.add(item);
                }
                break;

            case "weekly":
                rawData = orderRepository.getWeeklyRevenue(startDate, endDate);
                for (Object[] data : rawData) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("period", "Tuần " + data[1] + " - " + data[0]);
                    item.put("revenue", data[2]);
                    trendData.add(item);
                }
                break;

            case "monthly":
                rawData = orderRepository.getMonthlyRevenue(startDate, endDate);
                for (Object[] data : rawData) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("period", "Tháng " + data[1] + "/" + data[0]);
                    item.put("revenue", data[2]);
                    trendData.add(item);
                }
                break;
        }

        // Nếu không có dữ liệu, thêm dữ liệu mẫu để test
        if (trendData.isEmpty()) {
            System.out.println("No trend data found, adding sample data");
            trendData = getSampleTrendData(timeRange);
        }

        return trendData;
    }

    private List<Map<String, Object>> getSampleTrendData(String timeRange) {
        List<Map<String, Object>> sampleData = new ArrayList<>();

        switch (timeRange.toLowerCase()) {
            case "daily":
                for (int i = 6; i >= 0; i--) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("period", LocalDate.now().minusDays(i).toString());
                    item.put("revenue", 1000000L + (i * 200000L));
                    sampleData.add(item);
                }
                break;
            case "weekly":
                for (int i = 3; i >= 0; i--) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("period", "Tuần " + (LocalDate.now().getDayOfYear()/7 - i) + " - " + LocalDate.now().getYear());
                    item.put("revenue", 5000000L + (i * 1000000L));
                    sampleData.add(item);
                }
                break;
            case "monthly":
                for (int i = 5; i >= 0; i--) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("period", "Tháng " + (LocalDate.now().getMonthValue() - i) + "/" + LocalDate.now().getYear());
                    item.put("revenue", 20000000L + (i * 3000000L));
                    sampleData.add(item);
                }
                break;
        }

        return sampleData;
    }

    private List<Map<String, Object>> getRevenueByProduct(LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> productRevenue = new ArrayList<>();
        List<Object[]> rawData = orderRepository.getRevenueByProduct(startDate, endDate);

        for (Object[] data : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("productName", data[0]);
            item.put("revenue", data[1]);
            productRevenue.add(item);
        }

        // Nếu không có dữ liệu, thêm dữ liệu mẫu để test
        if (productRevenue.isEmpty()) {
            System.out.println("No product revenue data found, adding sample data");
            productRevenue = getSampleProductData();
        }

        return productRevenue;
    }

    private List<Map<String, Object>> getSampleProductData() {
        List<Map<String, Object>> sampleData = new ArrayList<>();

        String[] products = {"Windows 10 Pro", "Microsoft Office", "Adobe Photoshop", "Antivirus Pro", "Game License"};

        for (int i = 0; i < products.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("productName", products[i]);
            item.put("revenue", 5000000L - (i * 800000L));
            sampleData.add(item);
        }

        return sampleData;
    }

    public List<Map<String, Object>> getRevenueByCategory(LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> categoryRevenue = new ArrayList<>();
        List<Object[]> rawData = orderRepository.getRevenueByCategory(startDate, endDate);

        for (Object[] data : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryName", data[0]);
            item.put("revenue", data[1]);
            categoryRevenue.add(item);
        }

        return categoryRevenue;
    }
}