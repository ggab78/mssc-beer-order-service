package com.gabriel.beerservice.sm;

import com.gabriel.beerservice.domain.BeerOrder;
import com.gabriel.beerservice.domain.BeerOrderEventEnum;
import com.gabriel.beerservice.domain.BeerOrderStatusEnum;
import com.gabriel.beerservice.repositories.BeerOrderRepository;
import com.gabriel.beerservice.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;

    @Override
    public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state, Message<BeerOrderEventEnum> message, Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition, StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {

        Optional.ofNullable(message)
                .map(msg -> (String) msg.getHeaders().getOrDefault(BeerOrderManagerImpl.BEER_ORDER_HEADER_ID,""))
                .ifPresent(id -> {
                    BeerOrder beerOrder = beerOrderRepository.getOne(UUID.fromString(id));
                    beerOrder.setOrderStatus(state.getId());
                    beerOrderRepository.saveAndFlush(beerOrder);
                });
    }
}
