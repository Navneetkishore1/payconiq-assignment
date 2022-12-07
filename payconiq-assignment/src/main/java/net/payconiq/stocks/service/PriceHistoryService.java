package net.payconiq.stocks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.payconiq.stocks.model.StockPriceHistory;
import net.payconiq.stocks.model.Stock;
import net.payconiq.stocks.repository.PriceHistoryRepository;

import java.time.Instant;

/**
 * Service to perform business logic for {@link StockPriceHistory} entities.
 */
@Service
public class PriceHistoryService {

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    /**
     * Updates price history for specified {@link Stock}.
     * Closes current last price history record with provided timestamp (if present).
     * Opens new price history record with empty {@link StockPriceHistory#getEndDate()}.
     *
     * @param stock      - stock to update price for.
     * @param lastUpdate - timestamp which was taken for {@link Stock#getLastUpdate()}.
     */
    @Transactional
    public void updateStockPrice(@NonNull Stock stock, @NonNull Instant lastUpdate) {
        if (!stock.getHistory().isEmpty()) {
            StockPriceHistory lastPriceHistory = stock.getHistory().get(0);
            lastPriceHistory.setEndDate(lastUpdate);
            priceHistoryRepository.saveAndFlush(lastPriceHistory);
        }

        StockPriceHistory stockPriceHistory = new StockPriceHistory();
        stockPriceHistory.setPrice(stock.getCurrentPrice());
        stockPriceHistory.setStock(stock);
        stockPriceHistory.setStartDate(lastUpdate);
        priceHistoryRepository.saveAndFlush(stockPriceHistory);
    }
}
