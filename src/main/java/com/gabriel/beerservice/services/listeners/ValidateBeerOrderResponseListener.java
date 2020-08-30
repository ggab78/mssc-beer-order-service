package com.gabriel.beerservice.services.listeners;

import com.gabriel.beerservice.config.JmsConfig;
import com.gabriel.beerservice.services.BeerOrderManager;
import com.gabriel.model.events.ValidateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateBeerOrderResponseListener {


    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_BEER_ORDER_RESPONSE)
    public void listen(ValidateBeerOrderResult result) {
        beerOrderManager.processValidationResult(result.getOrderId(), result.getIsValid());
    }

}
