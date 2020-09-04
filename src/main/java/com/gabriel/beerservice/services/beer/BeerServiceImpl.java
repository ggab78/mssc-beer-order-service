package com.gabriel.beerservice.services.beer;

import com.gabriel.model.BeerDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@ConfigurationProperties(prefix = "mssc.brewery", ignoreUnknownFields = false)
public class BeerServiceImpl implements BeerService {

    public final static String PATH = "/api/v1/beer/upc/";

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
        return Optional.of(restTemplate.getForObject(beerHost + PATH+upc,BeerDto.class));
    }

}
