package net.payconiq.stocks.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import net.payconiq.stocks.exception.IncorrectRequestException;
import net.payconiq.stocks.model.StockPriceHistory;
import net.payconiq.stocks.model.Stock;
import net.payconiq.stocks.request.NewStockRequest;
import net.payconiq.stocks.request.PriceUpdateRequest;
import net.payconiq.stocks.service.StockDeleteService;
import net.payconiq.stocks.service.StockService;

import java.net.URI;
import java.util.Collection;

/**
 * Main Stocks REST controller
 */
@RestController
@RequestMapping("/api/stocks")
public class StockController {
	 Logger logger = LoggerFactory.getLogger(StockController.class);
    @Autowired
    private StockService stockService;
    
    
    @Autowired
    private StockDeleteService deleteService;
    
    

    /**
     * Returns list of all {@link Stock}s.
     *
     * @return list of all {@link Stock}s.
     */
    @GetMapping
    @NonNull
    public Collection<Stock> getStocks() {
    	logger.info("Get All stocks called :: ");
        return stockService.getAllStocks();
    }

    /**
     * Returns {@link Stock} by its id.
     *
     * @param id - id of stock to lookup.
     * @return {@link Stock} by its id.
     * @throws net.payconiq.stocks.exception.StockNotFoundException when there is no stock with such id.
     */
    @GetMapping("/{id}")
    @NonNull
    public Stock getStock(@PathVariable long id) {
    	logger.info("getStock called with id :: "+id);
        return stockService.lookupStock(id);
    }

    /**
     * Returns a price history for requested {@link Stock}.
     *
     * @param id - id of stock to lookup.
     * @return a price history for requested {@link Stock}.
     * @throws net.payconiq.stocks.exception.StockNotFoundException when there is no stock with such id.
     */
    @GetMapping("/{id}/history")
    @NonNull
    public Collection<StockPriceHistory> getHistory(@PathVariable long id) {
    	logger.info("getHistory by id :: "+id);
        return getStock(id).getHistory();
    }

    /**
     * Updates a price of a given stock.
     *
     * @param priceUpdateRequest - {@link PriceUpdateRequest} of price update.
     * @param id                 - id of stock to update.
     * @return update result response.
     * @throws net.payconiq.stocks.exception.StockNotFoundException when there is no stock with such id.
     * @throws IncorrectRequestException                        when price is 0 or below.
     */
    @PatchMapping("/{id}")
    @ResponseBody
    @NonNull
    public ResponseEntity<?> updatePrice(@RequestBody @NonNull PriceUpdateRequest priceUpdateRequest, @PathVariable long id) {
        stockService.updateStockPrice(id, priceUpdateRequest.getPrice());
        return ResponseEntity.ok("Stock price updated");
    }
    /**
     * 
     * @param id
     * @return success Message
     */
    
    @DeleteMapping("/{id}")
    @NonNull
    public ResponseEntity<?> deleteStock(@PathVariable long id) {
    	 deleteService.deleteStock(id);
    	 return ResponseEntity.ok("Stock deleted for Id :: "+id);
    }

    /**
     * Adding new stock by request.
     * @param newStockRequest - {@link NewStockRequest} of new stock to add.
     * @return adding result response.
     * @throws IncorrectRequestException                             when price is 0 or below or stock name is empty.
     * @throws net.payconiq.stocks.exception.StockAlreadyExistsException when stock with such name already exists.
     */
    @PostMapping
    @ResponseBody
    @NonNull
    public ResponseEntity<?> addStock(@RequestBody @NonNull NewStockRequest newStockRequest) {
        Stock newStock = stockService.addNewStock(newStockRequest);
        URI newStockLocation = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newStock.getId()).toUri();
        return ResponseEntity.created(newStockLocation).body("New Stock created");
    }
}
