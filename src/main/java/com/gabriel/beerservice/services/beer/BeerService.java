package com.gabriel.beerservice.services.beer;

import com.gabriel.model.BeerDto;

import java.util.Optional;

public interface BeerService {

    Optional<BeerDto> getBeerDtoByUpc(String upc);

}
