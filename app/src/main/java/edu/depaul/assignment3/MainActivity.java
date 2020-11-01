package edu.depaul.assignment3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener{

    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private final List<Stock> stockList = new ArrayList<>();
    private final List<Stock> tmpStockList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String targetURL = "http://www.marketwatch.com/investing/stock/";
    private int position;
    private String choice;
    private static final String TAG = "MainActivity";
    private boolean first = true;
    private final String noData = "noData";
    private final String noStock = "noStock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        for(int i = 0; i< 30; i++){
//            stockList.add(new Stock("St " + i+1, "Company " + i+1, i*20.0, i*0.5, i*0.01 ));
//        }
        recyclerView = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swiper);
        swipeRefreshLayout.setOnRefreshListener(this);
        readJSONData();
        connectionDecision();
    }

    private void connectionDecision(){
        if(!checkNetworkConnection()){
            noConnectionDialog("Updated");
            for(Stock s: tmpStockList){
                s.setLatestPrice(0.0);
                s.setPriceChange(0.0);
                s.setChangePercentage(0.0);
                stockList.add(s);
            }
            Collections.sort(tmpStockList);
            stockAdapter.notifyDataSetChanged();
            return;
        }
        financialDataDownloader(tmpStockList);
    }

    @Override
    public void onRefresh(){
        if(!checkNetworkConnection()){
            noConnectionDialog("Updated");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        tmpStockList.clear();
        readJSONData();
        stockList.clear();
        financialDataDownloader(tmpStockList);
        tmpStockList.clear();
        swipeRefreshLayout.setRefreshing(false);
    }

    private boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting() && first){
            symbolNameDownload();
            first = false;
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void financialDataDownloader(List<Stock> list){
        for(Stock s: list) {
            FinancialDataDownloader financialDataDownloader = new FinancialDataDownloader(this, s.getStockSymbol());
            new Thread(financialDataDownloader).start();
        }
    }

    private void symbolNameDownload(){
        SymbolNameDownloader symbolNameDownloader = new SymbolNameDownloader();
        new Thread(symbolNameDownloader).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.stock_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.addStockMenu){
            makeStockDialog();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void makeStockDialog(){
        if(!checkNetworkConnection()){
            noConnectionDialog("Added");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        builder.setView(et);

        builder.setPositiveButton("OK", (dialog, id) -> {
            choice = et.getText().toString().trim();

            final ArrayList<String> results = SymbolNameDownloader.findMatches(choice);

            if (results.size() == 0) {
                doNoAnswer(choice, noStock);
            } else if (results.size() == 1) {
                doSelection(results.get(0));
            } else {
                String[] array = results.toArray(new String[0]);

                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setTitle("Make a selection");
                builder1.setItems(array, (dialog1, which) -> {
                    String symbol = results.get(which);
                    doSelection(symbol);
                });
                builder1.setNegativeButton("Nevermind", (dialog1, id1) -> {
                    // User cancelled the dialog
                });
                AlertDialog dialog2 = builder1.create();
                dialog2.show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> {
            // User cancelled the dialog
        });

        builder.setMessage("Please enter a Symbol or Name:");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doNoAnswer(String symbol, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Symbol Not Found: " + symbol);
        if(message.equals(noStock)){
            builder.setMessage("Data for stock symbol");
        }else if(message.equals(noData)){
            builder.setMessage("No data for selection");
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doSelection(String sym) {
        String[] data = sym.split("-");
        FinancialDataDownloader financialDataDownloader = new FinancialDataDownloader(this, data[0].trim());
        new Thread(financialDataDownloader).start();
    }

    public void addStock(Stock stock){
        if (stock == null) {
            doNoAnswer(choice, noData);
            return;
        }
        if (stockList.contains(stock)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Stock Symbol " + stock.getCompanyName() + " is already displayed");
            builder.setTitle("Duplicate Stock");
            builder.setIcon(R.drawable.error);

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        stockList.add(stock);
        Collections.sort(stockList);
        writeJSONData();
        stockAdapter.notifyDataSetChanged();
    }

    private void noConnectionDialog(String function){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage("Stocks Cannot Be "+ function +" Without A Network Connection");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        position = recyclerView.getChildLayoutPosition(view);
        String sym = stockList.get(position).getStockSymbol();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(targetURL+sym));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View view) {
        position = recyclerView.getChildLayoutPosition(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Stock Symbol " + stockList.get(position).getStockSymbol()+ "?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            stockList.remove(position);
            writeJSONData();
            stockAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return false;
    }

    private void readJSONData() {
        try {
            FileInputStream fis = getApplicationContext().
                    openFileInput(getString(R.string.data_file));

            // Read string content from file
            byte[] data = new byte[fis.available()]; // this technique is good for small files
            int loaded = fis.read(data);
            Log.d(TAG, "readJSONData: Loaded " + loaded + " bytes");
            fis.close();
            String json = new String(data);

            // Create JSON Array from string file content
            JSONArray stockArr = new JSONArray(json);
            for (int i = 0; i < stockArr.length(); i++) {
                JSONObject cObj = stockArr.getJSONObject(i);

                // Access note data fields
                String name = cObj.getString("name");
                String symbol = cObj.getString("symbol");
                //Log.d(TAG, "loadFile: " + name);
                // Create Stock and add to temporary ArrayList
                Stock s = new Stock(symbol, name);
                tmpStockList.add(s);
            }
            Log.d(TAG, "readJSONData: " + tmpStockList.toString());

            stockAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeJSONData() {
        try {
            FileOutputStream fos = getApplicationContext().
                    openFileOutput(getString(R.string.data_file), Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            writer.setIndent("  ");
            writer.beginArray();
            for (Stock s : stockList) {
                writer.beginObject();

                writer.name("name").value(s.getCompanyName());
                writer.name("symbol").value(s.getStockSymbol());
                writer.endObject();
            }
            writer.endArray();
            writer.close();
            Log.d(TAG, "saveFile: " + stockList.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "writeJSONData: " + e.getMessage());
        }
    }
}