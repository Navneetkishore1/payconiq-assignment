package net.payconiq.stocks.service;

import com.sun.istack.Nullable;

import net.payconiq.stocks.exception.IncorrectRequestException;
import net.payconiq.stocks.exception.StockAlreadyExistsException;
import net.payconiq.stocks.exception.StockNotFoundException;
import net.payconiq.stocks.model.Stock;
import net.payconiq.stocks.repository.StockRepository;
import net.payconiq.stocks.request.NewStockRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.Repositories;
import org.springframework.format.datetime.joda.JodaTimeContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * Service to perform business logic on {@link Stock} entities.
 */
@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private PriceHistoryService priceHistoryService;
    

    /**
     * Returns list of all {@link Stock}s.
     *
     * @return list of all {@link Stock}s.
     */
    @Transactional
    @NonNull
    public Collection<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    /**
     * Returns {@link Stock} by its id.
     *
     * @param id - id of stock to lookup.
     * @return {@link Stock} by its id.
     * @throws StockNotFoundException when there is no stock with such id.
     */
    @Transactional
    @NonNull
    public Stock lookupStock(long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException("Stock with id " + id + " not found"));
    }

    /**
     * Updates a price of a given stock.
     *
     * @param id    - id of stock to update.
     * @param price - price to update stock with.
     * @return update result response.
     * @throws StockNotFoundException    when there is no stock with such id.
     * @throws IncorrectRequestException when price is 0 or below.
     */
    @Transactional
    @NonNull
    public Stock updateStockPrice(long id, @Nullable Double price) {
        validatePrice(price);
        Stock stock = lookupStock(id);
        stock.setCurrentPrice(price);
        return saveStock(stock);
    }
    
   

    /**
     * Adding new stock by request.
     *
     * @param newStockRequest - {@link NewStockRequest} of new stock to add.
     * @return adding result response.
     * @throws IncorrectRequestException                             when price is 0 or below or stock name is empty.
     * @throws net.payconiq.stocks.exception.StockAlreadyExistsException when stock with such name already exists.
     */
    @Transactional
    @NonNull
    public Stock addNewStock(@NonNull NewStockRequest newStockRequest) {
    	 Instant lastUpdate = Instant.now();
        Stock newStock = new Stock();
        newStock.setCurrentPrice(validatePrice(newStockRequest.getPrice()));
        newStock.setName(validateName(newStockRequest.getName()));
        newStock.setLastUpdate(lastUpdate);
        return saveStock(newStock);
    }

    /**
     * Saves a {@link Stock} and updates its {@link net.payconiq.stocks.model.StockPriceHistory} links.
     *
     * @param stock - {@link Stock} to save.
     * @return saved Stock.
     */
    @Transactional
    @NonNull
    private Stock saveStock(@NonNull Stock stock) {
        Instant lastUpdate = Instant.now();
        stock.setLastUpdate(lastUpdate);

        Stock savedStock = stockRepository.save(stock);

        priceHistoryService.updateStockPrice(savedStock, lastUpdate);

        return savedStock;
    }

    /**
     * Validates that price is greater than 0.
     *
     * @param price - price to validate.
     * @return input price.
     * @throws IncorrectRequestException when price is 0 or less.
     */
    private static double validatePrice(@Nullable Double price) {
        if (price == null || price <= 0) {
            throw new IncorrectRequestException("Stock price should be greater than zero");
        }
        return price;
    }

    /**
     * Validates that name is:
     * - not null
     * - not empty
     * - unique among all Stocks.
     *
     * @param name - name to validate.
     * @return input name without leading and trailing whitespaces.
     */
    @NonNull
    private String validateName(@Nullable String name) {
        if (name == null || name.isEmpty() || name.isBlank()) {
            throw new IncorrectRequestException("Stock name can't be empty");
        }
        String cleanName = name.trim();
        Optional<Stock> actualStock = stockRepository.findByName(cleanName);
        if (actualStock.isPresent()) {
            throw new StockAlreadyExistsException("Stock already exists with name: " + cleanName);
        }
        return cleanName;
    }
}
