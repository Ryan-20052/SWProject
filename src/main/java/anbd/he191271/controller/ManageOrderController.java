package anbd.he191271.controller;

import anbd.he191271.entity.Manager;
import anbd.he191271.entity.Order;
import anbd.he191271.entity.OrderDetail;
import anbd.he191271.service.OrderDetailService;
import anbd.he191271.service.OrderService;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/manageOrder")
public class ManageOrderController {
    @Autowired
    private OrderService  orderService;
    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping("/viewOrder")
    public String viewOrder(Model model, HttpSession session,
                            @RequestParam(required = false) String orderCode,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate) {
        Manager manager = (Manager)session.getAttribute("manager");
        List<String> statusList = List.of("PENDING", "PAID", "FAILED");
        if (manager == null) {
            return "redirect:/login.html";
        }
        Stream<Order> stream = orderService.findAll().stream();
        if(orderCode != null && !orderCode.trim().isEmpty()) {
            stream = stream.filter(o -> o.getCode().contains(orderCode));
        }
        if(status != null && !status.trim().isEmpty()) {
            stream = stream.filter(o -> o.getStatus().equals(status));
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            stream = stream.filter(o -> !o.getOrderDate().toLocalDate().isBefore(start));
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            LocalDate end = LocalDate.parse(endDate);
            stream = stream.filter(o -> !o.getOrderDate().toLocalDate().isAfter(end));
        }
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("statusList", statusList);
        model.addAttribute("manager", manager);
        model.addAttribute("orderList",stream.toList());
        return "manageOrder";
    }

    @PostMapping("/deleteOrder")
    public String deleteOrder(@RequestParam int orderId, RedirectAttributes redirectAttributes) {
        orderService.deleteById(orderId);
        redirectAttributes.addFlashAttribute("msg", "Đã xóa đơn hàng");
        return "redirect:/manageOrder/viewOrder";
    }

    @GetMapping("/viewOrderDetail/{id}")
    public String viewOrderDetail(@PathVariable("id") int id, HttpSession session, Model model) {
        Order order = orderService.findById(id);
        Manager manager = (Manager)session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        model.addAttribute("order", order);
        model.addAttribute("manager", manager);
        return "OrderDetail";
    }

}
