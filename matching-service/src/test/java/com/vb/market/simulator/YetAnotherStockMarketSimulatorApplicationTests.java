package com.vb.market.simulator;

import com.vb.market.domain.PlaceOrderRequest;
import com.vb.market.domain.PlaceOrderResponse;
import com.vb.market.domain.Side;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class YetAnotherStockMarketSimulatorApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void test() throws Exception {
		String placeOrderUrl = "http://localhost:" + port + "/market/placeorder";

		PlaceOrderRequest placeOrderRequest = PlaceOrderRequest.Builder.anOrderRequest()
				.withClientId("Vlad")
				.withPrice(20)
				.withQuantity(100)
				.withSide(Side.BUY)
				.withSymbol("AAA")
				.build();

		ResponseEntity<PlaceOrderResponse> responseEntity =
				this.restTemplate.postForEntity(placeOrderUrl, placeOrderRequest, PlaceOrderResponse.class);

		String test = "";

	}

}
