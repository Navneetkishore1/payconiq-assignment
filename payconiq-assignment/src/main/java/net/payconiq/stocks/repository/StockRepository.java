package net.payconiq.stocks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.payconiq.stocks.model.Stock;

import java.util.Optional;

/**
 * Repository for {@link Stock} entities to perform base operations.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByName(String name);
   

    @Modifying
    @Query("delete from Stock u where u.id = ?1")
    void deleteStockById(long Id);
}
