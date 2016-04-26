package preti.stock.fe.location.recomendations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import preti.stock.coremodel.Stock;
import preti.stock.coremodel.StockHistory;
import preti.stock.coremodel.Trade;
import preti.stock.fe.facade.RecomendationFacade;
import preti.stock.fe.facade.RemoteApiException;
import preti.stock.fe.facade.StockFacade;
import preti.stock.fe.facade.TradeFacade;
import preti.stock.fe.location.TradeVO;

@Service
public class RecomendationsService {
    private Logger logger = LoggerFactory.getLogger(RecomendationsService.class);

    @Autowired
    private RecomendationFacade recomendationFacade;

    @Autowired
    private StockFacade stockFacade;

    @Autowired
    private TradeFacade tradeFacade;

    public RecomendationsVO generateRecomendations(long accountId, Date recomendationsDate) throws RemoteApiException {
        List<Trade> recTrades = recomendationFacade.generateRecomendations(accountId, recomendationsDate);
        if (recTrades == null || recTrades.isEmpty()) {
            return new RecomendationsVO(accountId, new ArrayList<>());
        }

        // FIXME: 20 dias fixo
        Date initialHistoryDate = new DateTime(recomendationsDate.getTime()).minusDays(20).toDate();
        List<TradeVO> trades = new ArrayList<>();
        for (Trade t : recTrades) {
            Stock stock = stockFacade.getStock(t.getStockId());
            StockHistory[] history = stockFacade.getStockHistory(t.getStockId(), initialHistoryDate,
                    recomendationsDate);
            stock.setHistory(Arrays.asList(history));

            trades.add(new TradeVO(t.getId(), stock.getId(), t.getModelId(), stock.getCode(), stock.getName(),
                    t.getSize(), t.getStopPos(), t.getBuyDate(), t.getSellDate(), t.getBuyValue(),
                    stock.getCloseValueAtDate(recomendationsDate)));
        }

        return new RecomendationsVO(accountId, trades);
    }

    public void executeTrades(long accountId, List<TradeVO> trades) {
        logger.info(String.format("Execution %s trades for account %s", trades.size(), accountId));
        tradeFacade.realizetrades(accountId, trades);
    }
}