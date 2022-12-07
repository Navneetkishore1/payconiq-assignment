package net.payconiq.stocks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.payconiq.stocks.repository.PriceHistoryRepository;
import net.payconiq.stocks.repository.StockRepository;

@Service
public class StockDeleteService {
	public static final String SUCCESS = "Successfully Deleted the stock id :: ";
	
	 @Autowired
	    private StockRepository stockRepository;
	 
	    @Transactional
	    public String deleteStock(long id) {
	    	stockRepository.deleteById(id);
	    	StringBuffer buffer = new StringBuffer();
	    	buffer.append(SUCCESS).append(id);
	    	return SUCCESS + id;
	    }

}
