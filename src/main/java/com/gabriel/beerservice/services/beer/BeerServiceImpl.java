package com.gabriel.beerservice.services.beer;

import com.gabriel.beerservice.services.beer.model.BeerDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import java.util.Optional;

@Component
@ConfigurationProperties(prefix = "mssc.brewery", ignoreUnknownFields = false)
public class BeerServiceImpl implements BeerService {

    private final static String PATH = "/api/v1/beer/upc/{upc}";

    private final RestTemplate restTemplate;

    private String beerHost;

    public void setBeerHost(String beerHost) {
        this.beerHost = beerHost;
    }

    public BeerServiceImpl(RestTemplateBuilder templateBuilder) {
        this.restTemplate = templateBuilder.build();
    }
    
    @Override
    public Optional<BeerDto> getBeerDtoByUpc(String upc) {
        return restTemplate.exchange(beerHost + PATH, HttpMethod.GET, null, new ParameterizedTypeReference<Optional<BeerDto>>() {
        }, (Object) upc).getBody();
    }

}
