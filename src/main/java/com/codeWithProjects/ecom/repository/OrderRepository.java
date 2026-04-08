package com.codeWithProjects.ecom.repository;

import com.codeWithProjects.ecom.entity.Order;
import com.codeWithProjects.ecom.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

   Order findByUserIdAndOrderStatus(Long UserId, OrderStatus orderStatus);

   List<Order> findAllByOrderStatusIn(List<OrderStatus> orderStatusList);

   List<Order> findByUserIdAndOrderStatusIn(Long UserId, List<OrderStatus> orderStatus);

   Optional<Order> findByTrackingId(UUID trackingId);


   Optional<Order> searchOrderByTrackingId(UUID trackingId);

   List<Order> findByDateBetweenAndOrderStatus(Date startOfMonth, Date endOfMonth, OrderStatus status);

   Long countByOrderStatus(OrderStatus status);
}
