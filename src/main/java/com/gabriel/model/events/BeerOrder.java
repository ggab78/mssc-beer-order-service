package com.gabriel.model.events;

import com.gabriel.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class BeerOrder implements Serializable {

    static final long serialVersionUID= -4737365164527072162L;

    private BeerOrderDto beerOrderDto;
}
