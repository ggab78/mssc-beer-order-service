package guru.sfg.beer.order.service.services.beer;

import org.springframework.stereotype.Service;


public interface BeerService {

    String getBeerNameByUpc(String upc);
    String getBeerStyleByUpc(String upc);
}
