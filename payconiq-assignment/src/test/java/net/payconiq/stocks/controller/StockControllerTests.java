
package net.payconiq.stocks.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.payconiq.stocks.exception.IncorrectRequestException;
import net.payconiq.stocks.exception.StockAlreadyExistsException;
import net.payconiq.stocks.exception.StockNotFoundException;
import net.payconiq.stocks.model.Stock;
import net.payconiq.stocks.model.StockPriceHistory;
import net.payconiq.stocks.repository.StockRepository;
import net.payconiq.stocks.request.NewStockRequest;
import net.payconiq.stocks.request.PriceUpdateRequest;

@ExtendWith(SpringExtension.class)

@SpringBootTest

@AutoConfigureMockMvc

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class StockControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StockRepository stockRepository;

	@Test
	void testGetAllStocks() throws Exception {
		mockMvc.perform(get("/api/stocks")).andExpect(status().isOk()).andExpect(content().json("["
				+ "{\"id\":1,\"name\":\"Bombay Stock\",\"currentPrice\":2.0,\"lastUpdate\":\"2022-12-11T22:58:34Z\"},"
				+ "{\"id\":4,\"name\":\"Singapore Stock\",\"currentPrice\":11.5,\"lastUpdate\":\"2022-12-11T23:59:56Z\"}"
				+ "]", true));
	}

	@Test
	void testGetStockById() throws Exception {
		mockMvc.perform(get("/api/stocks/4")).andExpect(status().isOk()).andExpect(content().json(
				"{\"id\":4,\"name\":\"Singapore Stock\",\"currentPrice\":11.5,\"lastUpdate\":\"2022-12-11T23:59:56Z\"}",
				true));
	}

	@Test
	void testGetIncorrectId() throws Exception {
		Exception exception = mockMvc.perform(get("/api/stocks/2")).andExpect(status().isNotFound()).andReturn()
				.getResolvedException();
		assertException(exception, StockNotFoundException.class, "Stock with id 2 not found");
	}

	@Test
	void testGetUnparsableId() throws Exception {
		Exception exception = mockMvc.perform(get("/api/stocks/1_is_not_a_number")).andExpect(status().isBadRequest())
				.andReturn().getResolvedException();
		assertException(exception, MethodArgumentTypeMismatchException.class,
				"Failed to convert value of type 'java.lang.String' to required type 'long'; nested exception is java.lang.NumberFormatException: For input string: \"1_is_not_a_number\"");
	}

	@Test
	void testGetHistory() throws Exception {
		mockMvc.perform(get("/api/stocks/4/history")).andExpect(status().isOk()).andExpect(content().json("["
				+ "{\"price\":23.0,\"startDate\":\"2022-12-11T23:59:56Z\",\"endDate\":null},"
				+ "{\"price\":24.0,\"startDate\":\"2022-12-10T07:03:00Z\",\"endDate\":\"2022-12-11T23:59:56Z\"},"
				+ "{\"price\":22.75,\"startDate\":\"2022-12-09T21:08:47Z\",\"endDate\":\"2022-12-10T07:03:00Z\"}" + "]",
				true));
	}

	@Test
	void testGetHistoryIncorrectId() throws Exception {
		Exception exception = mockMvc.perform(get("/api/stocks/5/history")).andExpect(status().isNotFound()).andReturn()
				.getResolvedException();
		assertException(exception, StockNotFoundException.class, "Stock with id 5 not found");
	}

	@Test
	void testPostNewStock() throws Exception {
		NewStockRequest newStockRequest = new NewStockRequest();
		newStockRequest.setName("   Tokyo Stock    ");
		newStockRequest.setPrice(2d);

		Instant timestampBeforeInsertion = Instant.now();

		mockMvc.perform(post("/api/stocks").contentType("application/json")
				.content(new ObjectMapper().writeValueAsString(newStockRequest))).andExpect(status().isCreated());

		Optional<Stock> expectedStock = stockRepository.findByName("Tokyo Stock");
		assertThat(expectedStock).isPresent()
				.hasValueSatisfying(s -> Assertions.assertAll(() -> assertThat(s.getId()).isNotNull(),
						() -> assertThat(s.getName()).isEqualTo("Tokyo Stock"),
						() -> assertThat(s.getCurrentPrice()).isEqualTo(2d),
						() -> assertThat(s.getLastUpdate()).isAfter(timestampBeforeInsertion), () -> {
							List<StockPriceHistory> history = s.getHistory();
							assertThat(history).isNotNull().hasSize(1);
							StockPriceHistory stockPriceHistory = history.get(0);
							Assertions.assertAll(() -> assertThat(stockPriceHistory.getId()).isNotNull(),
									() -> assertThat(stockPriceHistory.getPrice()).isEqualTo(2d),
									() -> assertThat(stockPriceHistory.getStock().getId()).isEqualTo(s.getId()),
									() -> assertThat(stockPriceHistory.getStartDate()).isEqualTo(s.getLastUpdate()),
									() -> assertThat(stockPriceHistory.getEndDate()).isNull());
						}));
	}

	@Test
	void testPostNewStockZeroPrice() throws Exception {
		NewStockRequest newStockRequest = new NewStockRequest();
		newStockRequest.setName("Hong-Kong Stock");
		newStockRequest.setPrice(0d);

		Exception exception = mockMvc
				.perform(post("/api/stocks").contentType("application/json")
						.content(new ObjectMapper().writeValueAsString(newStockRequest)))
				.andExpect(status().isBadRequest()).andReturn().getResolvedException();

		assertException(exception, IncorrectRequestException.class, "Stock price should be greater than zero");
	}

	@Test
	void testPostNewStockEmptyName() throws Exception {
		NewStockRequest newStockRequest = new NewStockRequest();
		newStockRequest.setName("     ");
		newStockRequest.setPrice(10d);

		Exception exception = mockMvc
				.perform(post("/api/stocks").contentType("application/json")
						.content(new ObjectMapper().writeValueAsString(newStockRequest)))
				.andExpect(status().isBadRequest()).andReturn().getResolvedException();

		assertException(exception, IncorrectRequestException.class, "Stock name can't be empty");
	}

	@Test
	void testPostNewStockAlreadyExists() throws Exception {
		NewStockRequest newStockRequest = new NewStockRequest();
		newStockRequest.setName(" Bombay Stock  ");
		newStockRequest.setPrice(10d);

		Exception exception = mockMvc
				.perform(post("/api/stocks").contentType("application/json")
						.content(new ObjectMapper().writeValueAsString(newStockRequest)))
				.andExpect(status().isBadRequest()).andReturn().getResolvedException();

		assertException(exception, StockAlreadyExistsException.class, "Stock already exists with name: Bombay Stock");
	}

    @Test
    void testPatchNewPrice() throws Exception {
        double newPrice = 23.0;

        PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();
        priceUpdateRequest.setPrice(newPrice);

        Optional<Stock> expectedStock = stockRepository.findById(4L);
        assertThat(expectedStock).isPresent();
        Stock expectedStockValue = expectedStock.get();
        String originalStockName = expectedStockValue.getName();
        Instant timestampBeforeUpdate = expectedStockValue.getLastUpdate();
        double priceBeforeUpdate = expectedStockValue.getCurrentPrice();
        int historySizeBeforeUpdate = expectedStockValue.getHistory().size();

        mockMvc.perform(
                patch("/api/stocks/4")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
                .andExpect(status().isOk());

        Optional<Stock> updatedStock = stockRepository.findById(4L);
        assertThat(updatedStock)
                .isPresent()
                .hasValueSatisfying(s -> Assertions.assertAll(
                        () -> assertThat(s.getName()).isEqualTo(originalStockName),
                        () -> assertThat(s.getCurrentPrice()).isEqualTo(newPrice),
                        () -> assertThat(s.getLastUpdate()).isNotNull(),
                        () -> {
                            List<StockPriceHistory> history = s.getHistory();
                            assertThat(history).isNotNull().hasSize(historySizeBeforeUpdate + 1);
                            StockPriceHistory lastPriceHistory = history.get(0);
                            Assertions.assertAll(
                                    () -> assertThat(lastPriceHistory.getId()).isNotNull(),
                                    () -> assertThat(lastPriceHistory.getPrice()).isNotNull(),
                                    () -> assertThat(lastPriceHistory.getStock().getId()).isEqualTo(s.getId()),
                                    () -> assertThat(lastPriceHistory.getStartDate()).isNotNull(),
                                    () -> assertThat(lastPriceHistory.getEndDate()).isNotNull()
                            );
                            StockPriceHistory previousPriceHistory = history.get(1);
                            Assertions.assertAll(
                                    () -> assertThat(previousPriceHistory.getPrice()).isNotNull(),
                                    () -> assertThat(previousPriceHistory.getStock().getId()).isEqualTo(s.getId()),
                                    () -> assertThat(previousPriceHistory.getEndDate()).isNotNull()
                            );
                        }
                ));
    }

	@Test
	void testPutNewPriceIncorrectId() throws Exception {
		PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();
		priceUpdateRequest.setPrice(3d);

		Exception exception = mockMvc
				.perform(patch("/api/stocks/6").contentType("application/json")
						.content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
				.andExpect(status().isNotFound()).andReturn().getResolvedException();
		assertException(exception, StockNotFoundException.class, "Stock with id 6 not found");
	}

	@Test
	void testPutNewPriceNegative() throws Exception {
		PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();
		priceUpdateRequest.setPrice(-3d);

		Exception exception = mockMvc
				.perform(patch("/api/stocks/4").contentType("application/json")
						.content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
				.andExpect(status().isBadRequest()).andReturn().getResolvedException();
		assertException(exception, IncorrectRequestException.class, "Stock price should be greater than zero");
	}

	@Test
	void testPutNewPriceNull() throws Exception {
		PriceUpdateRequest priceUpdateRequest = new PriceUpdateRequest();

		Exception exception = mockMvc
				.perform(patch("/api/stocks/4").contentType("application/json")
						.content(new ObjectMapper().writeValueAsString(priceUpdateRequest)))
				.andExpect(status().isBadRequest()).andReturn().getResolvedException();
		assertException(exception, IncorrectRequestException.class, "Stock price should be greater than zero");
	}

	private static void assertException(Exception exception, Class<? extends Throwable> exceptionClass,
			String message) {
		assertThat(exception).isNotNull();
		assertThat(exception).isExactlyInstanceOf(exceptionClass);
		assertThat(exception.getMessage()).isEqualTo(message);
	}

}
