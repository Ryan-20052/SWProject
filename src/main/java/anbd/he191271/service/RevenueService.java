package anbd.he191271.service;

import anbd.he191271.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
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
                                                   LocalDateTime endDate
                                                   ) {
        Map<String, Object> result = new HashMap<>();

        System.out.println("=== REVENUE DASHBOARD DATA ===");
        System.out.println("Start Date: " + startDate);
        System.out.println("End Date: " + endDate);

        Long revenue = orderRepository.getTotalRevenueByPeriod(startDate, endDate);

        // Doanh thu theo sản phẩm
        List<Map<String, Object>> revenueByProduct = getRevenueByProduct(startDate, endDate);
        System.out.println("Product Revenue size: " + (revenueByProduct != null ? revenueByProduct.size() : 0));

        result.put("revenue", revenue);
        result.put("revenueByProduct", revenueByProduct);


        return result;
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

        return productRevenue;
    }

    public List<Map<String, Object>> getDailyRevenueDetails(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> dailyRevenue = new ArrayList<>();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> rawData = orderRepository.getDailyRevenue(startDateTime, endDateTime);

        for (Object[] data : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", data[0]);
            item.put("revenue", data[1]);
            item.put("orderCount", data[2]);
            dailyRevenue.add(item);
        }

        return dailyRevenue;
    }
}