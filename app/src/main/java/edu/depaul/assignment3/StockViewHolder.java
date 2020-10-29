package edu.depaul.assignment3;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {

    TextView stockSymbol;
    TextView companyName;
    TextView stockPrice;
    TextView changeSymbol;
    TextView changeValue;

    public StockViewHolder(@NonNull View itemView) {
        super(itemView);
        stockSymbol = itemView.findViewById(R.id.symbolTextView);
        companyName = itemView.findViewById(R.id.nameTextView);
        stockPrice = itemView.findViewById(R.id.priceTextView);
        changeSymbol = itemView.findViewById(R.id.changeSymboltextView);
        changeValue = itemView.findViewById(R.id.changeValueTextView);
    }


}
