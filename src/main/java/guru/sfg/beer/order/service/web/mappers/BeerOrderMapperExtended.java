package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.services.beer.BeerService;
import guru.sfg.beer.order.service.web.model.BeerOrderDto;
import org.springframework.beans.factory.annotation.Autowired;

public class BeerOrderMapperExtended implements BeerOrderMapper{


    private BeerService beerService;
    private BeerOrderMapper beerOrderMapper;

    @Autowired
    public void setBeerMapper(BeerOrderMapper beerOrderMapper) {
        this.beerOrderMapper = beerOrderMapper;
    }

    @Autowired
    public void setBeerInventoryService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Override
    public BeerOrderDto beerOrderToDtoExtended(BeerOrder beerOrder) {
        BeerOrderDto dto = beerOrderMapper.beerOrderToDto(beerOrder);
        String upc = beerOrder.getBeerOrderLines().stream()
                .map(o->o.getUpc())
                .findFirst()
                .orElseThrow(RuntimeException::new);

        beerService.getBeerDtoByUpc(upc)
                .ifPresent(o->{
                    dto.setBeerName(o.getBeerName());
                    dto.setBeerStyle(o.getBeerStyle());
                });

        return dto;
    }

    @Override
    public BeerOrderDto beerOrderToDto(BeerOrder beerOrder) {
        return beerOrderMapper.beerOrderToDto(beerOrder);

    }

    @Override
    public BeerOrder dtoToBeerOrder(BeerOrderDto dto) {
        return beerOrderMapper.dtoToBeerOrder(dto);
    }

}
