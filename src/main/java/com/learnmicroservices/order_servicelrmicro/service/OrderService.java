package com.learnmicroservices.order_servicelrmicro.service;

import com.learnmicroservices.order_servicelrmicro.model.OrderRequest;
import com.learnmicroservices.order_servicelrmicro.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
