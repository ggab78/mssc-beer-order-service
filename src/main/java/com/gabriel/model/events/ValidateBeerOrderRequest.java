package com.gabriel.model.events;

import com.gabriel.model.BeerOrderDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ValidateBeerOrderRequest extends BeerOrder {

    public ValidateBeerOrderRequest(BeerOrderDto beerOrderDto) {
        super(beerOrderDto);
    }
}
