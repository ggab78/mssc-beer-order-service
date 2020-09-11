package com.gabriel.beerservice.services.testcomponents;


import com.gabriel.beerservice.config.JmsConfig;
import com.gabriel.model.events.ValidateBeerOrderRequest;
import com.gabriel.model.events.ValidateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidatorListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_BEER_ORDER)
    public void listen(Message msg){

        log.debug("----------------Mock validation response-----------------------");

        ValidateBeerOrderRequest request = (ValidateBeerOrderRequest) msg.getPayload();

        boolean isValid=Optional.ofNullable(request.getBeerOrderDto().getCustomerRef()).map(s->{
            if(s.equals("fail-validation")){
                return false;
            }
            return true;
        }).orElseGet(()->true);


        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_BEER_ORDER_RESPONSE, ValidateBeerOrderResult.builder()
                .isValid(isValid)
                .orderId(request.getBeerOrderDto().getId())
                .build());
    }

}
