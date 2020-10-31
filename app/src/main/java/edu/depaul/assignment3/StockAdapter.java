package edu.depaul.assignment3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private List<Stock> stockList;
    private MainActivity mainAct;

    public StockAdapter(List<Stock> stockList, MainActivity mainAct) {
        this.stockList = stockList;
        this.mainAct = mainAct;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_entry, parent, false);
        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);
        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock s = stockList.get(position);
        holder.stockSymbol.setText(s.getStockSymbol());
        holder.companyName.setText(s.getCompanyName());
        holder.stockPrice.setText(String.format(Locale.getDefault(),"%.2f",s.getLatestPrice()));
        holder.changeSymbol.setText(s.getStockSymbol());
        holder.changeValue.setText(String.format(Locale.getDefault(),"%.2f" +"(" + "%.2f"+")",s.getPriceChange(), s.getChangePercentage()));
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
