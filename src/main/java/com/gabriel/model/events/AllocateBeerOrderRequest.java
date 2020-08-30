package com.gabriel.model.events;

import com.gabriel.model.BeerOrderDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AllocateBeerOrderRequest extends BeerOrder {

    public AllocateBeerOrderRequest(BeerOrderDto beerOrderDto) {
        super(beerOrderDto);
    }
}
