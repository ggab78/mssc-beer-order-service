package com.gabriel.beerservice.services.beer.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class BeerDto {

    private UUID id;
    private String beerName;
    private String beerStyle;

}