package preti.stock.api.repository;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import preti.stock.api.model.db.StockDBEntity;
import preti.stock.api.model.db.StockHistoryDBEntity;
import preti.stock.api.repository.mappers.StockHistoryMapper;
import preti.stock.api.repository.mappers.StockRowMapper;

@Repository
public class StocksRepository {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Date getLastStocksDate() {
        return jdbcTemplate.queryForObject("select max(date) from stock_history", Date.class);
    }

    public boolean existsStock(String stockCode) {
        return jdbcTemplate.queryForObject("select count(*) from stock where stock_code = ?",
                new Object[] { stockCode }, Integer.class) > 0;
    }

    public StockDBEntity getStock(long stockId) {
        return jdbcTemplate.queryForObject("select stock_id, stock_code, stock_name from stock where stock_id = ?",
                new Object[] { stockId }, new StockRowMapper());
    }

    public StockDBEntity getStock(String stockCode) {
        return jdbcTemplate.queryForObject("select stock_id, stock_code, stock_name from stock where stock_code = ?",
                new Object[] { stockCode }, new StockRowMapper());
    }

    public void createStock(String stockCode, String stockName) {
        jdbcTemplate.update("insert into stock (stock_code, stock_name) values (?, ?)", stockCode, stockName);
    }

    public Integer getStockId(String stockCode) {
        return jdbcTemplate.queryForObject("select stock_id from stock where stock_code=?", new Object[] { stockCode },
                Integer.class);
    }

    public void createHistory(Integer stockId, Date date, Double high, Double low, Double close, Double open,
            Double volume) {
        jdbcTemplate.update(
                "insert into stock_history (stock_id, date, high, low, close, open, volume) values (?, ?, ?, ?, ?, ?, ?)",
                new Object[] { stockId, date, high, low, close, open, volume });
    }

    public List<StockHistoryDBEntity> getStockHistory(String stockCode) {
        StringBuilder sql = new StringBuilder();
        sql.append("select sh.stock_history_id, sh.stock_id, sh.date, sh.high, sh.low, sh.close, sh.open, sh.volume ");
        sql.append("from stock_history sh ");
        sql.append("inner join stock s on s.stock_id=sh.stock_id ");
        sql.append("where s.stock_code = ?");
        return jdbcTemplate.query(sql.toString(), new Object[] { stockCode }, new StockHistoryMapper());
    }

    public List<StockHistoryDBEntity> getStockHistory(String stockCode, Date initialDate, Date finalDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("select sh.stock_history_id, sh.stock_id, sh.date, sh.high, sh.low, sh.close, sh.open, sh.volume ");
        sql.append("from stock_history sh ");
        sql.append("inner join stock s on s.stock_id=sh.stock_id ");
        sql.append("where s.stock_code = ? and sh.date >= ? and sh.date <= ?");
        return jdbcTemplate.query(sql.toString(), new Object[] { stockCode, initialDate, finalDate },
                new StockHistoryMapper());
    }

    public List<StockHistoryDBEntity> getStockHistory(long stockId, Date initialDate, Date finalDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("select sh.stock_history_id, sh.stock_id, sh.date, sh.high, sh.low, sh.close, sh.open, sh.volume ");
        sql.append("from stock_history sh ");
        sql.append("inner join stock s on s.stock_id=sh.stock_id ");
        sql.append("where s.stock_id = ? and sh.date >= ? and sh.date <= ?");
        return jdbcTemplate.query(sql.toString(), new Object[] { stockId, initialDate, finalDate },
                new StockHistoryMapper());
    }

    public boolean existsHistoryAtDate(long stockId, Date date) {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) ");
        sql.append("from stock_history sh ");
        sql.append("inner join stock s on s.stock_id=sh.stock_id ");
        sql.append("where s.stock_id = ? and sh.date = ?");
        return jdbcTemplate.queryForObject(sql.toString(), new Object[] { stockId, date }, Integer.class) > 0;
    }
}
