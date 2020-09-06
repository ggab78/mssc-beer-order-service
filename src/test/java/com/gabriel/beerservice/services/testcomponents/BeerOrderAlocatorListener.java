package com.gabriel.beerservice.services.testcomponents;

import com.gabriel.beerservice.config.JmsConfig;
import com.gabriel.model.BeerOrderDto;
import com.gabriel.model.events.AllocateBeerOrderRequest;
import com.gabriel.model.events.AllocateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAlocatorListener {

    private final JmsTemplate jmsTemplate;


    @JmsListener(destination = JmsConfig.ALLOCATE_BEER_ORDER)
    public void listen(Message msg){

        log.debug("----------------Mock allocation response-----------------------");

        AllocateBeerOrderRequest request = (AllocateBeerOrderRequest) msg.getPayload();

        BeerOrderDto beerOrderDto=request.getBeerOrderDto();

        beerOrderDto.getBeerOrderLines().forEach(l->{
            l.setQuantityAllocated(l.getOrderQuantity());
        });

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_BEER_ORDER_RESPONSE, AllocateBeerOrderResult.builder()
                .allocationError(false)
                .pendingInventory(false)
                .beerOrderDto(beerOrderDto)
                .build());
    }

}
