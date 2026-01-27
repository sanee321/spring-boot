package com.example.orderservice.service;

import com.example.orderservice.dto.OrderItemResponse;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public OrderResponse createOrder(OrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(OrderStatus.PENDING);

        // Create order items
        List<OrderItem> items = request.getItems().stream()
                .map(itemRequest -> {
                    OrderItem item = new OrderItem();
                    item.setProductId(itemRequest.getProductId());
                    item.setQuantity(itemRequest.getQuantity());
                    item.setUnitPrice(itemRequest.getUnitPrice());
                    item.calculateSubtotal();
                    return item;
                })
                .collect(Collectors.toList());

        order.setItems(items);

        // Calculate total amount
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    public Optional<OrderResponse> getOrderById(Long id) {
        return orderRepository.findById(id).map(this::mapToResponse);
    }

    public Optional<OrderResponse> getOrderById(Long id, Long userId) {
        return orderRepository.findByIdAndUserId(id, userId).map(this::mapToResponse);
    }

    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse updateOrderStatus(Long id, String status) {
        Optional<Order> optional = orderRepository.findById(id);
        if (optional.isPresent()) {
            Order order = optional.get();
            order.setStatus(OrderStatus.valueOf(status));
            Order updatedOrder = orderRepository.save(order);
            return mapToResponse(updatedOrder);
        }
        return null;
    }

    public boolean cancelOrder(Long id) {
        Optional<Order> optional = orderRepository.findById(id);
        if (optional.isPresent()) {
            Order order = optional.get();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return true;
        }
        return false;
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().toString());
        response.setShippingAddress(order.getShippingAddress());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getId(), item.getProductId(),
                        item.getQuantity(), item.getUnitPrice(), item.getSubtotal()))
                .collect(Collectors.toList());
        response.setItems(items);

        return response;
    }
}