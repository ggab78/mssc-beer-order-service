package com.gabriel.beerservice.web.mappers;

import com.gabriel.beerservice.domain.BeerOrderLine;
import com.gabriel.model.BeerOrderLineDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
@DecoratedWith(BeerOrderLineMapperExtended.class)
public interface BeerOrderLineMapper {

    BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line);
    BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto);
}
