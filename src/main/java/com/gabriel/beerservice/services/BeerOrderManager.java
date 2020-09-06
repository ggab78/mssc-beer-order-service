package com.gabriel.beerservice.services;

import com.gabriel.beerservice.domain.BeerOrder;
import com.gabriel.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);
    public BeerOrder processValidationResult(UUID beerOrderId, Boolean isValid);
    public BeerOrder processAllocationResult(BeerOrderDto beerOrderDto, Boolean allocationError, Boolean pendingInventory);
    public void pickUpBeerOrder(UUID id);
}
