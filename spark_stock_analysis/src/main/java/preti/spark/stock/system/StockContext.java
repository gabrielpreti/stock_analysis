package preti.spark.stock.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import preti.spark.stock.model.Stock;
import preti.spark.stock.model.Trade;
import preti.spark.stock.reporting.AbstractReport;

@SuppressWarnings("serial")
public class StockContext implements Serializable {
	protected static final Log log = LogFactory.getLog(StockContext.class);

	private Stock stock;
	private List<Trade> trades;

	public StockContext(Stock stock) {
		super();
		this.stock = stock;
		trades = new ArrayList<>();
	}

	public Stock getStock() {
		return stock;
	}

	public List<Trade> getTrades() {
		return trades;
	}

	public Trade getLastTrade() {
		if (trades.size() == 0)
			return null;

		return trades.get(trades.size() - 1);
	}

	public boolean isInOpenPosition() {
		Trade t = getLastTrade();
		return t != null && t.isOpen();
	}

	public Trade openNewTrade(double size, Date buyDate, double stopPos) {
		if (isInOpenPosition()) {
			throw new IllegalArgumentException("Can't open a new trade with one already opened.");
		}

		Trade t = new Trade(this.stock, size, stopPos, buyDate);
		trades.add(t);
		return t;
	}

	public Trade closeLastTrade(Date closeDate) {
		if (!isInOpenPosition()) {
			throw new IllegalArgumentException("No open trade to close.");
		}
		Trade t = getLastTrade();
		t.close(closeDate);
		return t;
	}

	public boolean hasReachedStopPosition(Date d) {
		Trade t = getLastTrade();
		return t != null && t.isOpen() && t.hasReachedStopPosition(d);
	}

	public boolean isProfittable(Date d) {
		Trade t = getLastTrade();
		return t != null && t.isProfitable(d);
	}

	public boolean hasAnyTrade() {
		return trades.size() > 0;
	}

	public Trade getTradeOpenAt(Date d) {
		Trade tradeOpen = null;
		for (Trade t : trades) {
			if (t.getBuyDate().equals(d)) {
				tradeOpen = t;
				break;
			}
		}
		return tradeOpen;
	}

	public Trade getTradeClosedAt(Date d) {
		Trade tradeClosed = null;
		for (Trade t : trades) {
			if (t.getSellDate().equals(d)) {
				tradeClosed = t;
				break;
			}
		}
		return tradeClosed;
	}

	public double getOpenPositionsValueAtDate(Date d) {
		if (!hasAnyTrade()) {
			return 0;
		}

		double value = 0;
		for (Trade t : trades) {
			if (t.isOpen(d)) {
				value += t.getTotalValue(d);
			}
		}

		return value;
	}

}