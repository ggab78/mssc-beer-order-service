package com.gabriel.beerservice.web.mappers;

import com.gabriel.beerservice.domain.Customer;
import com.gabriel.beerservice.web.model.CustomerDto;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {

    Customer CustomerDtoToCustomer(CustomerDto dto);
    CustomerDto CustomerToCustomerDto(Customer customer);
}
