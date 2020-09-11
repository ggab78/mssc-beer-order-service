package com.gabriel.model.events;

import com.gabriel.model.BeerOrderDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DeallocateBeerOrderRequest extends BeerOrder {

    public DeallocateBeerOrderRequest(BeerOrderDto beerOrderDto) {
        super(beerOrderDto);
    }
}
