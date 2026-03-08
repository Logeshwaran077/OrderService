package com.learnmicroservices.order_servicelrmicro.external.client;


import com.learnmicroservices.order_servicelrmicro.exception.CustomException;
import com.learnmicroservices.order_servicelrmicro.external.request.PaymentRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CircuitBreaker(name = "external", fallbackMethod = "fallback" )
@FeignClient(name = "PAYMENT-SERVICE/payment")
public interface PaymentService {
    @PostMapping
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    default  void fallback(Exception e){
        throw new CustomException("payment service is not available","UNAVAILABLE",500);
    }
}
