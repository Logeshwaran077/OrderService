package com.learnmicroservices.order_servicelrmicro.repository;

import com.learnmicroservices.order_servicelrmicro.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
}
