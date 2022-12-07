package net.payconiq.stocks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.payconiq.stocks.model.StockPriceHistory;

/**
 * Repository for {@link StockPriceHistory} entities to perform base operations.
 */
@Repository
public interface PriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {
	
	@Modifying
    @Query("delete from StockPriceHistory u where u.id = ?1")
    void deleteStockPriceHistoryById(long Id);
}
