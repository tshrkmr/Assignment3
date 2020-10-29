package edu.depaul.assignment3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener{

    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private final List<Stock> stockList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String targetURL = "http://www.marketwatch.com/investing/stock/AAPL";
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for(int i = 0; i< 30; i++){
            stockList.add(new Stock("St " + i+1, "Company " + i+1, i*20.0, i*0.5, i*0.01 ));
        }

        recyclerView = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swiper);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.stock_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        Toast.makeText(this, "Menu was selected", Toast.LENGTH_LONG).show();
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onRefresh(){
        Toast.makeText(this, "Page Refreshed", Toast.LENGTH_LONG).show();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(targetURL));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View view) {
        position = recyclerView.getChildLayoutPosition(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Stock Symbol " + "Place Holder" + "?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            stockList.remove(position);
            stockAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }


}