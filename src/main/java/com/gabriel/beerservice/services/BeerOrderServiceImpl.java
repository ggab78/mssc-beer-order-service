/*
 *  Copyright 2019 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.gabriel.beerservice.services;

import com.gabriel.beerservice.domain.BeerOrder;
import com.gabriel.beerservice.domain.BeerOrderStatusEnum;
import com.gabriel.beerservice.repositories.BeerOrderRepository;
import com.gabriel.beerservice.repositories.CustomerRepository;
import com.gabriel.beerservice.web.mappers.BeerOrderMapper;
import com.gabriel.model.BeerOrderDto;
import com.gabriel.model.BeerOrderPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderServiceImpl implements BeerOrderService {

    private final BeerOrderRepository beerOrderRepository;
    private final CustomerRepository customerRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final BeerOrderManager beerOrderManager;


    @Override
    public BeerOrderPagedList listOrders(UUID customerId, Pageable pageable) {

        return customerRepository.findById(customerId)
                .map(customer -> {
                    Page<BeerOrder> beerOrderPage = beerOrderRepository.findAllByCustomer(customer, pageable);
                    List<BeerOrderDto> beerOrders =
                            beerOrderRepository.findAllByCustomer(customer, pageable)
                                    .stream()
                                    .map(beerOrderMapper::beerOrderToDto)
                                    .collect(Collectors.toList());

                    return new BeerOrderPagedList(beerOrders,
                            PageRequest.of(
                                    beerOrderPage.getPageable().getPageNumber(),
                                    beerOrderPage.getPageable().getPageSize()),
                            beerOrderPage.getTotalElements());
                }).orElseGet(() -> {
                    return null;
                });
    }

    @Transactional
    @Override
    public BeerOrderDto placeOrder(UUID customerId, BeerOrderDto beerOrderDto) {


        return customerRepository.findById(customerId)
                .map(customer -> {
                    BeerOrder beerOrder = beerOrderMapper.dtoToBeerOrder(beerOrderDto);
                    beerOrder.setId(null); //should not be set by outside client
                    beerOrder.setCustomer(customer);
                    beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);
                    beerOrder.getBeerOrderLines().forEach(line -> {
                        System.out.println("line upc : "+line.getUpc());
                        line.setBeerOrder(beerOrder);});
                    BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

                    log.debug("Saved Beer Order: " + savedBeerOrder.getId());
//
                    return beerOrderMapper.beerOrderToDto(savedBeerOrder);
                })
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public BeerOrderDto getOrderById(UUID customerId, UUID orderId) {
        return beerOrderMapper.beerOrderToDto(getOrder(customerId, orderId));
    }

    @Override
    public void pickupOrder(UUID orderId) {
       beerOrderManager.pickUpBeerOrder(orderId);
    }

    private BeerOrder getOrder(UUID customerId, UUID orderId) {

        return customerRepository.findById(customerId)
                .map(customer -> beerOrderRepository.findById(orderId)
                        .filter(beerOrder -> beerOrder.getCustomer().getId().equals(customerId))
                        .orElseThrow(() -> new RuntimeException("Beer Order Not Found"))
                ).orElseThrow(() -> new RuntimeException("Customer Not Found"));
    }
}
