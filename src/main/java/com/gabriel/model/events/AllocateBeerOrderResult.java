package com.gabriel.model.events;

import com.gabriel.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocateBeerOrderResult {

    private BeerOrderDto beerOrderDto;
    private Boolean allocationError=false;
    private Boolean pendingInventory=false;

}
