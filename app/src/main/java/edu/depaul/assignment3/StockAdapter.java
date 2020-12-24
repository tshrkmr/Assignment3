package edu.depaul.assignment3;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private final List<Stock> stockList;
    private final MainActivity mainAct;

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
        int color;
        String pointer;
        if(s.getPriceChange()==0){
            color = Color.WHITE;
            pointer = "";
        }
        else if(s.getPriceChange()>0) {
            color = Color.GREEN;
            pointer = "\u25B2";
        } else {
            color = Color.RED;
            pointer = "\u25BC";
        }
        holder.stockSymbol.setTextColor(color);
        holder.companyName.setTextColor(color);
        holder.stockPrice.setTextColor(color);
        holder.changeSymbol.setTextColor(color);
        holder.changeValue.setTextColor(color);

        holder.stockSymbol.setText(s.getStockSymbol());
        holder.companyName.setText(s.getCompanyName());
        holder.stockPrice.setText(String.format(Locale.getDefault(),"%.2f",s.getLatestPrice()));
        holder.changeSymbol.setText(pointer);
        holder.changeValue.setText(String.format(Locale.getDefault(),"%.2f" +"(" + "%.2f"+ "%%)",s.getPriceChange(), s.getChangePercentage()));
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
