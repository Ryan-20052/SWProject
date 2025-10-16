package anbd.he191271.service;

import anbd.he191271.entity.Order;
import anbd.he191271.repository.OrderDetailRepository;
import anbd.he191271.repository.OrderRepository;
import anbd.he191271.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    final OrderRepository orderRepository;
    public  OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order>  findAll() {
        return orderRepository.findAll();
    }
    public Order findById(int id) {
        return orderRepository.findById(id).get();
    }
    public void deleteById(int id) {
        orderRepository.deleteById(id);
    }
}
