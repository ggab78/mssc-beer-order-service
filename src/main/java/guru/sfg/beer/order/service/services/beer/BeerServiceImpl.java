package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.services.beer.model.BeerDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
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
    public String getBeerNameByUpc(String upc) {
        return Objects.requireNonNull(getBeerDto(upc).getBody()).getBeerName();

    }

    @Override
    public String getBeerStyleByUpc(String upc) {
        return Objects.requireNonNull(getBeerDto(upc).getBody()).getBeerStyle();
    }


    private ResponseEntity<BeerDto> getBeerDto(String upc){
        return restTemplate.exchange(beerHost+PATH, HttpMethod.GET, null, new ParameterizedTypeReference<BeerDto>(){},(Object) upc);
    }
}
