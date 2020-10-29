package edu.depaul.assignment3;

import java.io.Serializable;
import java.util.Objects;

public class Stock implements Serializable, Comparable<Stock> {

    String stockSymbol;
    String companyName;
    Double price;
    Double priceChange;
    Double changePercentage;

    public Stock(String stockSymbol, String companyName, Double price, Double priceChange, Double changePercentage) {
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.price = price;
        this.priceChange = priceChange;
        this.changePercentage = changePercentage;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Double getPrice() {
        return price;
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

    @Override
    public int hashCode() {
        return Objects.hash(stockSymbol, companyName);
    }

    @Override
    public int compareTo(Stock stock) {
        return companyName.compareTo(stock.getCompanyName());
    }
}
