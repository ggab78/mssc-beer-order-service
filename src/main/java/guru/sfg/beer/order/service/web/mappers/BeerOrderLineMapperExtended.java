package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.services.beer.BeerService;
import guru.sfg.beer.order.service.web.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;


public class BeerOrderLineMapperExtended implements BeerOrderLineMapper {


    private BeerService beerService;
    private BeerOrderLineMapper beerOrderLineMapper;

    @Autowired
    public void setBeerMapper(BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }

    @Autowired
    public void setBeerInventoryService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto dto = beerOrderLineMapper.beerOrderLineToDto(line);

        Optional.ofNullable(line)
                .ifPresent(l -> {
                    beerService.getBeerDtoByUpc(l.getUpc())
                            .ifPresent(beerDto -> {
                                dto.setBeerName(beerDto.getBeerName());
                                dto.setBeerStyle(beerDto.getBeerStyle());
                                dto.setBeerId(beerDto.getId());
                            });
                });

        return dto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        return beerOrderLineMapper.dtoToBeerOrderLine(dto);
    }
}
