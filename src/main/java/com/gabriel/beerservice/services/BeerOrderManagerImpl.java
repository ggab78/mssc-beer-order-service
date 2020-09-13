package com.gabriel.beerservice.services;

import com.gabriel.beerservice.domain.BeerOrder;
import com.gabriel.beerservice.domain.BeerOrderEventEnum;
import com.gabriel.beerservice.domain.BeerOrderStatusEnum;
import com.gabriel.beerservice.repositories.BeerOrderRepository;
import com.gabriel.beerservice.sm.BeerOrderStateChangeInterceptor;
import com.gabriel.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String BEER_ORDER_HEADER_ID = "header_id";

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;


    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {

        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);

        return savedBeerOrder;
    }

    @Transactional
    @Override
    public void pickUpBeerOrder(UUID id) {

        Optional<BeerOrder> orderOptional = beerOrderRepository.findById(id);

        orderOptional.ifPresentOrElse(o-> sendBeerOrderEvent(o, BeerOrderEventEnum.BEERORDER_PICKED_UP)
                ,()->log.error("beer order not found"+id));
    }

    @Transactional
    @Override
    public void cancelBeerOrder(UUID id) {

        Optional<BeerOrder> orderOptional = beerOrderRepository.findById(id);

        orderOptional.ifPresentOrElse(o-> sendBeerOrderEvent(o, BeerOrderEventEnum.CANCEL_ORDER)
                ,()->log.error("beer order not found"+id));
    }


    @Transactional
    @Override
    public BeerOrder processValidationResult(UUID beerOrderId, Boolean isValid) {


        log.debug("Process Validation Result");

        BeerOrder beerOrder = beerOrderRepository.findById(beerOrderId).get();
        if(isValid){
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);
            allocateBeerOrder(beerOrderId);
        } else{
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
        }
        return beerOrder;
    }

    @Transactional
    @Override
    public BeerOrder processAllocationResult(BeerOrderDto beerOrderDto, Boolean allocationError, Boolean pendingInventory) {
        BeerOrder beerOrder = beerOrderRepository.findById(beerOrderDto.getId()).get();

        if(allocationError){
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
        }else{
            if(pendingInventory){
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
                updateAllocatedQty(beerOrderDto);
            }else{
                sendBeerOrderEvent(beerOrder,BeerOrderEventEnum.ALLOCATION_SUCCESS);
                log.debug("BeerOrder state"+ beerOrder.getOrderStatus());
                //todo await for db to update beerOrderstatus to allocated
                updateAllocatedQty(beerOrderDto);
            }
        }
        return beerOrder;
    }


    private void updateAllocatedQty(BeerOrderDto beerOrderDto){

        BeerOrder allocatedBeerOrder = beerOrderRepository.findById(beerOrderDto.getId()).get();

        allocatedBeerOrder.getBeerOrderLines().forEach(line->{
            beerOrderDto.getBeerOrderLines().forEach(lineDto->{
                if(line.getId().equals(lineDto.getId())){
                    line.setQuantityAllocated(lineDto.getQuantityAllocated());
                }
            });

        });
        beerOrderRepository.saveAndFlush(allocatedBeerOrder);
    }


    private BeerOrder allocateBeerOrder(UUID beerOrderId) {
        BeerOrder beerOrder = beerOrderRepository.findById(beerOrderId).get();
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
        return beerOrder;
    }



    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {

        log.debug("Sending beer order event : "+eventEnum.name());
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);

        Message msg = MessageBuilder
                .withPayload(eventEnum)
                .setHeader(BEER_ORDER_HEADER_ID, beerOrder.getId().toString())
                .build();
        sm.sendEvent(msg);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder){

        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm =
                Optional.ofNullable(stateMachineFactory.getStateMachine(beerOrder.getId()))
                .orElseGet(()->{
                    log.debug("Creating State Machine with no Id");
                    return stateMachineFactory.getStateMachine();
                });


        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma-> {
                    sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
                });

        //log.debug("Rehydrating SM for beerOrderId "+beerOrder.getId() +"with state "+sm.getState().getId().name());
        sm.start();

        return sm;
    }

}
