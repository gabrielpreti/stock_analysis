package preti.stock.client.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class Wallet implements Serializable {

    private long walletId;
    private long stockId;
    private long accountId;
    private double size;
    private Date creationDate;
    private Date updateDate;

    public Wallet() {

    }

    public Wallet(long walletId, long stockId, long accountId, double size, Date creationDate, Date updateDate) {
        super();
        this.walletId = walletId;
        this.stockId = stockId;
        this.accountId = accountId;
        this.size = size;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public long getStockId() {
        return stockId;
    }

    public void setStockId(long stockId) {
        this.stockId = stockId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

}
