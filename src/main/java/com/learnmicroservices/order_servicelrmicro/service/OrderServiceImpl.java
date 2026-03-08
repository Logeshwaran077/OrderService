package com.learnmicroservices.order_servicelrmicro.service;


import com.learnmicroservices.order_servicelrmicro.entity.Order;
import com.learnmicroservices.order_servicelrmicro.exception.CustomException;
import com.learnmicroservices.order_servicelrmicro.external.client.PaymentService;

import com.learnmicroservices.order_servicelrmicro.external.client.ProductService;
import com.learnmicroservices.order_servicelrmicro.external.request.PaymentRequest;
import com.learnmicroservices.order_servicelrmicro.external.response.PaymentResponse;
import com.learnmicroservices.order_servicelrmicro.model.OrderRequest;
import com.learnmicroservices.order_servicelrmicro.model.OrderResponse;
import com.learnmicroservices.order_servicelrmicro.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public long placeOrder(OrderRequest orderRequest) {

        log.info("placing order request: {}",orderRequest);
        productService.reduceQuantity(orderRequest.getProductId(),orderRequest.getQuantity());
        log.info("createing order iwht status Created");


        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        order = orderRepository.save(order);

        log.info("calling payment service from the payment");
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done successfully. changin order status to success");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.info("Error occured in payment");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("order places successfully with order id: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {

        log.info("get order details for order id : {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->new CustomException(
                "order not found for the order id " + orderId,
                "ORDER_NOT_FOUND",
                404
        ));

        log.info("incvoking product service to product for id:{}",order.getProductId());
        OrderResponse.ProductDetails productResponse =  restTemplate.getForObject(
                "http://PRODUCT-SERVICE/product/" + order.getProductId(),
                OrderResponse.ProductDetails.class
        );

        log.info("getting payment information from the payment service");
        PaymentResponse paymentResponse = restTemplate.getForObject("" +
                "http://PAYMENT-SERVICE/payment/order/" +order.getId(),PaymentResponse.class);

        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails.builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .build();

        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentStatus(paymentResponse.getStatus())
                .paymentDate(paymentResponse.getPaymentDate())
                .paymentMode(paymentResponse.getPaymentMode())
                .build();


        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();
        return orderResponse;
    }
}
