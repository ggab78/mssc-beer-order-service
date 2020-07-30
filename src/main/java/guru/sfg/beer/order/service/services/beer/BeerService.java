package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.services.beer.model.BeerDto;
import org.springframework.stereotype.Service;

import java.util.Optional;


public interface BeerService {

    Optional<BeerDto> getBeerDtoByUpc(String upc);

}
