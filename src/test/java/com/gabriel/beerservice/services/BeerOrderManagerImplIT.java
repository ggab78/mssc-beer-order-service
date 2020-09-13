package com.gabriel.beerservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabriel.beerservice.config.JmsConfig;
import com.gabriel.beerservice.domain.BeerOrder;
import com.gabriel.beerservice.domain.BeerOrderLine;
import com.gabriel.beerservice.domain.BeerOrderStatusEnum;
import com.gabriel.beerservice.domain.Customer;
import com.gabriel.beerservice.repositories.BeerOrderRepository;
import com.gabriel.beerservice.repositories.CustomerRepository;
import com.gabriel.beerservice.services.beer.BeerServiceImpl;
import com.gabriel.model.BeerDto;
import com.gabriel.model.events.DeallocateBeerOrderRequest;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class BeerOrderManagerImplIT {

    @Autowired
    BeerOrderManager beerOrderManager;
    @Autowired
    BeerOrderRepository beerOrderRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    WireMockServer wireMockServer;
    @Autowired
    JmsTemplate jmsTemplate;

    Customer testCustomer;

    UUID beerId = UUID.randomUUID();
    String upc = "123";

    @TestConfiguration
    static class RestTemplateBuilderProvider {

        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer server = with(wireMockConfig().port(8083));
            server.start();
            return server;
        }
    }


    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder()
                .customerName("test customer")
                .build());
    }

    @Test
    void testNewToAllocated() throws JsonProcessingException, InterruptedException {

        BeerDto beerDto=BeerDto.builder().id(beerId).upc(upc).build();
        //BeerPagedList list = new BeerPagedList(Arrays.asList(beerDto));

        wireMockServer.stubFor(get(BeerServiceImpl.PATH+beerDto.getUpc()).willReturn(okJson(objectMapper.writeValueAsString(Optional.of(beerDto)))));

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(createBeerOrder());

        await().untilAsserted(()->{
            BeerOrder order = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            order.getBeerOrderLines().forEach(l->assertEquals(l.getOrderQuantity(), l.getQuantityAllocated()));

            assertEquals(BeerOrderStatusEnum.ALLOCATED, order.getOrderStatus());
        });

        BeerOrder beerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(beerOrder);

        assertEquals(BeerOrderStatusEnum.ALLOCATED, beerOrder.getOrderStatus());
        beerOrder.getBeerOrderLines().forEach(l->assertEquals(l.getOrderQuantity(), l.getQuantityAllocated()));

    }


    @Test
    void testFailedValidation() throws JsonProcessingException {

        BeerDto beerDto=BeerDto.builder().id(beerId).upc(upc).build();

        wireMockServer.stubFor(get(BeerServiceImpl.PATH+beerDto.getUpc()).willReturn(okJson(objectMapper.writeValueAsString(Optional.of(beerDto)))));

        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("fail-validation");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(()->{
            BeerOrder order = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.VALIDATION_EXCEPTION, order.getOrderStatus());
        });


    }


    @Test
    void testFailedAllocation() throws JsonProcessingException {

        BeerDto beerDto=BeerDto.builder().id(beerId).upc(upc).build();

        wireMockServer.stubFor(get(BeerServiceImpl.PATH+beerDto.getUpc()).willReturn(okJson(objectMapper.writeValueAsString(Optional.of(beerDto)))));

        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("fail-allocation");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(()->{
            BeerOrder order = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATION_EXCEPTION, order.getOrderStatus());
        });

    }


    @Test
    void testCancel() throws JsonProcessingException {

        BeerDto beerDto=BeerDto.builder().id(beerId).upc(upc).build();

        wireMockServer.stubFor(get(BeerServiceImpl.PATH+beerDto.getUpc()).willReturn(okJson(objectMapper.writeValueAsString(Optional.of(beerDto)))));

        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        beerOrderManager.cancelBeerOrder(beerOrder.getId());

        await().untilAsserted(()->{
            BeerOrder order = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.CANCELLED, order.getOrderStatus());
        });

    }


    @Test
    void testCancelWhenAllocationPending() throws JsonProcessingException {

        BeerDto beerDto=BeerDto.builder().id(beerId).upc(upc).build();

        wireMockServer.stubFor(get(BeerServiceImpl.PATH+beerDto.getUpc()).willReturn(okJson(objectMapper.writeValueAsString(Optional.of(beerDto)))));

        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);
        beerOrderManager.cancelBeerOrder(beerOrder.getId());

        await().untilAsserted(()->{
            BeerOrder order = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATION_PENDING, order.getOrderStatus());
        });

        beerOrderManager.cancelBeerOrder(beerOrder.getId());

        await().untilAsserted(()->{
            BeerOrder order = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.CANCELLED, order.getOrderStatus());
        });

    }



    @Test
    void testCancelWhenAllocated() throws JsonProcessingException {

        BeerDto beerDto=BeerDto.builder().id(beerId).upc(upc).build();

        wireMockServer.stubFor(get(BeerServiceImpl.PATH+beerDto.getUpc()).willReturn(okJson(objectMapper.writeValueAsString(Optional.of(beerDto)))));

        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);


        await().untilAsserted(()->{
            BeerOrder order = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED, order.getOrderStatus());
        });

        beerOrderManager.cancelBeerOrder(beerOrder.getId());

        await().untilAsserted(()->{
            BeerOrder order = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.CANCELLED, order.getOrderStatus());
        });

       DeallocateBeerOrderRequest deallocateBeerOrderRequest = (DeallocateBeerOrderRequest) jmsTemplate.receiveAndConvert(JmsConfig.DEALLOCATE_BEER_ORDER);

       assertNotNull(deallocateBeerOrderRequest);
       assertEquals(deallocateBeerOrderRequest.getBeerOrderDto().getId(),savedBeerOrder.getId());

    }

    @Test
    void testAllocationPending() throws JsonProcessingException {

        BeerDto beerDto=BeerDto.builder().id(beerId).upc(upc).build();

        wireMockServer.stubFor(get(BeerServiceImpl.PATH+beerDto.getUpc()).willReturn(okJson(objectMapper.writeValueAsString(Optional.of(beerDto)))));

        BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("pending");

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(()->{
            BeerOrder order = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.PENDING_INVENTORY, order.getOrderStatus());
        });
    }


    @Test
    void testNewToPickedUp() throws JsonProcessingException {

        BeerDto beerDto=BeerDto.builder().id(beerId).upc(upc).build();

        wireMockServer.stubFor(get(BeerServiceImpl.PATH+beerDto.getUpc()).willReturn(okJson(objectMapper.writeValueAsString(Optional.of(beerDto)))));

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(createBeerOrder());

        await().untilAsserted(()->{
            BeerOrder order = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            order.getBeerOrderLines().forEach(l->assertEquals(l.getOrderQuantity(), l.getQuantityAllocated()));
            assertEquals(BeerOrderStatusEnum.ALLOCATED, order.getOrderStatus());
        });

        beerOrderManager.pickUpBeerOrder(savedBeerOrder.getId());

        BeerOrder beerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertEquals(BeerOrderStatusEnum.PICKED_UP, beerOrder.getOrderStatus());


    }

    private BeerOrder createBeerOrder() {
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
                .beerId(beerId)
                .orderQuantity(1)
                .upc(upc)
                .beerOrder(beerOrder)
                .build());

        beerOrder.setBeerOrderLines(lines);

        return beerOrder;
    }
}
