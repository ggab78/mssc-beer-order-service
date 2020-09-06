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

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidatorListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_BEER_ORDER)
    public void listen(Message msg){

        log.debug("----------------Mock validation response-----------------------");

        ValidateBeerOrderRequest request = (ValidateBeerOrderRequest) msg.getPayload();

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_BEER_ORDER_RESPONSE, ValidateBeerOrderResult.builder()
                .isValid(true)
                .orderId(request.getBeerOrderDto().getId())
                .build());
    }

}
