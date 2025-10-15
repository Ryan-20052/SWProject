package anbd.he191271.service;

import anbd.he191271.entity.Order;
import anbd.he191271.entity.OrderDetail;
import anbd.he191271.repository.OrderDetailRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class OrderDetailService {
    private final OrderDetailRepository orderDetailRepository;
    public OrderDetailService(OrderDetailRepository orderDetailRepository) {
        this.orderDetailRepository = orderDetailRepository;
    }
    public List<OrderDetail> getOrderDetails(Order  order) {
        return orderDetailRepository.findByOrder(order);
    }
}
