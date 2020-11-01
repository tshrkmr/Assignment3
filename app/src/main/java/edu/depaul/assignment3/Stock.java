package edu.depaul.assignment3;

import java.io.Serializable;
import java.util.Objects;

public class Stock implements Serializable, Comparable<Stock> {

    String stockSymbol;
    String companyName;
    double latestPrice;
    double priceChange;
    double changePercentage;

    public Stock(String stockSymbol, String companyName) {
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Double getLatestPrice() {
        return latestPrice;
    }

    public Double getPriceChange() {
        return priceChange;
    }

    public Double getChangePercentage() {
        return changePercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return stockSymbol.equals(stock.stockSymbol) &&
                companyName.equals(stock.companyName);
    }

    public void setLatestPrice(double latestPrice) {
        this.latestPrice = latestPrice;
    }

    public void setPriceChange(double priceChange) {
        this.priceChange = priceChange;
    }

    public void setChangePercentage(double changePercentage) {
        this.changePercentage = changePercentage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockSymbol, companyName);
    }

    @Override
    public int compareTo(Stock stock) {
        return companyName.compareTo(stock.getCompanyName());
    }

    @Override
    public String toString() {
        return "\n" + stockSymbol + " | " + companyName + " | " + latestPrice + " | " + priceChange + " | " + changePercentage;
    }
}
