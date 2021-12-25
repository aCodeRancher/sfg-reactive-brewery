package guru.springframework.sfgrestbrewery.web.controller;


import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebFluxTest(BeerController.class)
class BeerControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    BeerService beerService;

    BeerDto validBeer;


    @BeforeEach
    void setUp() {
        validBeer = BeerDto.builder()
                .beerName("Test beer")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_1_UPC)
                .price(BigDecimal.TEN)
                 .id(UUID.randomUUID())
                .quantityOnHand(1000)
                .build();
    }

    @Test
    void listBeers() {
        List<BeerDto> beerList = Arrays.asList(validBeer);

        BeerPagedList beerPagedList = new BeerPagedList(beerList, PageRequest.of(1,1), beerList.size());

        given(beerService.listBeers(any(), any(), any(), any())).willReturn(beerPagedList);

        webTestClient.get()
                .uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerPagedList.class);
    }

    @Test
    void getBeerByUPC() {
        given(beerService.getByUpc(any())).willReturn(validBeer);

        webTestClient.get()
                .uri("/api/v1/beerUpc/" + validBeer.getUpc())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(), equalTo(validBeer.getBeerName()));
    }

    @Test
    void getBeerById() {
        UUID beerId = UUID.randomUUID();
        given(beerService.getById(any(), any())).willReturn(validBeer);

        webTestClient.get()
                .uri("/api/v1/beer/" + beerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(), equalTo(validBeer.getBeerName()));
    }

    @Test
    void saveBeer() {

        when(beerService.saveNewBeer(any(BeerDto.class))).thenReturn(validBeer);
        String beerDtoJson =  "{\"beerName\":\"Test beer\",\"beerStyle\":\"PALE_ALE\",\"upc\":\"0631234200036\",\"price\":10,\"quantityOnHand\":1000,\"createdDate\":null,\"lastUpdatedDate\":null}";
        webTestClient.post()
                .uri("/api/v1/beer")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(beerDtoJson))
                 .exchange()
                .expectStatus().isCreated()
                .expectBody(Void.class);

    }

    @Test
    void deleteBeer(){
        doNothing().when(beerService).deleteBeerById(any(UUID.class));
        webTestClient.delete()
                .uri("/api/v1/beer/"+ UUID.randomUUID().toString())
                .exchange()
                .expectStatus().isOk().expectBody(Void.class);
    }

    @Test
    void updateBeer(){
        UUID id = UUID.randomUUID();

       BeerDto updatedBeer = BeerDto.builder()
                .beerName("Christmas beer")
                .beerStyle("PALE_ALE")
                .upc(BeerLoader.BEER_1_UPC)
                .price(BigDecimal.TEN)
                 .quantityOnHand(2000)
                 .createdDate(OffsetDateTime.now())
                 .lastUpdatedDate(OffsetDateTime.now())
                .build();

       when(beerService.updateBeer(any(UUID.class),any(BeerDto.class))).thenReturn(updatedBeer);
       webTestClient.put()
                .uri("/api/v1/beer/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatedBeer))
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(), equalTo(updatedBeer.getBeerName()));
    }

}