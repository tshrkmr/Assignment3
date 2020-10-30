package edu.depaul.assignment3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener{

    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private final List<Stock> stockList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String targetURL = "http://www.marketwatch.com/investing/stock/AAPL";
    private int position;
    private String choice;
    private static final String TAG = "MainActivity";

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

        SymbolNameDownloader symbolNameDownloader = new SymbolNameDownloader();
        new Thread(symbolNameDownloader).start();

        readJSONData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        writeJSONData();
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
        Toast.makeText(this, "Menu was selected", Toast.LENGTH_LONG).show();
        return super.onOptionsItemSelected(menuItem);
    }

    private void makeStockDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        builder.setView(et);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                choice = et.getText().toString().trim();

                final ArrayList<String> results = SymbolNameDownloader.findMatches(choice);

                if (results.size() == 0) {
                    doNoAnswer(choice);
                } else if (results.size() == 1) {
                    doSelection(results.get(0));
                } else {
                    String[] array = results.toArray(new String[0]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Make a selection");
                    builder.setItems(array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String symbol = results.get(which);
                            doSelection(symbol);
                        }
                    });
                    builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog2 = builder.create();
                    dialog2.show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.setMessage("Please enter a Symbol or Name:");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doNoAnswer(String symbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Data for stock symbol");
        builder.setTitle("Symbol Not Found: " + symbol);

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
            badDataAlert(choice);
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
        stockAdapter.notifyDataSetChanged();
    }

    private void badDataAlert(String sym) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No data for selection");
        builder.setTitle("Symbol Not Found: " + sym);

        AlertDialog dialog = builder.create();
        dialog.show();
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
                // Create Stock and add to ArrayList
                Stock s = new Stock(symbol, name, 10.0, 20.0, 100.0);
                stockList.add(s);
            }
            Log.d(TAG, "readJSONData: " + stockList.toString());

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