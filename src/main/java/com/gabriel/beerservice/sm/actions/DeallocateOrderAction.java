package com.gabriel.beerservice.sm.actions;

import com.gabriel.beerservice.config.JmsConfig;
import com.gabriel.beerservice.domain.BeerOrder;
import com.gabriel.beerservice.domain.BeerOrderEventEnum;
import com.gabriel.beerservice.domain.BeerOrderStatusEnum;
import com.gabriel.beerservice.repositories.BeerOrderRepository;
import com.gabriel.beerservice.services.BeerOrderManagerImpl;
import com.gabriel.beerservice.web.mappers.BeerOrderMapper;
import com.gabriel.model.events.DeallocateBeerOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeallocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

        String headerId = (String) stateContext.getMessageHeaders().getOrDefault(BeerOrderManagerImpl.BEER_ORDER_HEADER_ID, "");
        BeerOrder beerOrder = beerOrderRepository.findById(UUID.fromString(headerId)).get();
        jmsTemplate.convertAndSend(JmsConfig.DEALLOCATE_BEER_ORDER,
                new DeallocateBeerOrderRequest(beerOrderMapper.beerOrderToDto(beerOrder)));
        log.debug("Send Deallocation request to queue for orderId : " + headerId);
    }

}
