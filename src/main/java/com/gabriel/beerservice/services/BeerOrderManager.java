package com.gabriel.beerservice.services;

import com.gabriel.beerservice.domain.BeerOrder;

import java.util.UUID;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);
    public BeerOrder processValidationResult(UUID beerOrderId, Boolean isValid);


}
