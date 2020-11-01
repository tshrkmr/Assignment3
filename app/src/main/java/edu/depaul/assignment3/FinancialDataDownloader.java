package edu.depaul.assignment3;

import android.net.Uri;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FinancialDataDownloader implements Runnable{

    private static final String TAG = "FinancialDataDownloader";
    private static final String FINANCIAL_DATA_URL_1 = "https://cloud.iexapis.com/stable/stock/";
    private static final String FINANCIAL_DATA_URL_2 = "/quote?token=pk_97328407750b4a79a12cd7b561bb0771";
    private final MainActivity mainActivity;
    private final String searchTarget;

    public FinancialDataDownloader(MainActivity mainActivity, String searchTarget) {
        this.mainActivity = mainActivity;
        this.searchTarget = searchTarget;
    }

    @Override
    public void run() {
        Uri.Builder uriBuilder = Uri.parse(FINANCIAL_DATA_URL_1 + searchTarget + FINANCIAL_DATA_URL_2).buildUpon();
        String urlToUse = uriBuilder.toString();

        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "run: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
            return;
        }

        process(sb.toString());
        Log.d(TAG, "run: ");
    }

    private void process(String s) {
        try {
            JSONObject jObjMain = new JSONObject(s);

            String symbol = jObjMain.getString("symbol");
            Log.d(TAG, "process symbol: " + symbol);
            String name = jObjMain.getString("companyName");

            String lp = jObjMain.getString("latestPrice");
            double latestPrice = 0.0;
            if (!lp.trim().isEmpty() && !lp.trim().equals(null))
                latestPrice= Double.parseDouble(lp);

            String ch = jObjMain.getString("change");
            double priceChange = 0.0;
            if (!ch.trim().isEmpty() && !ch.trim().equals(null))
                priceChange = Double.parseDouble(ch);

            String cp = jObjMain.getString("changePercent");
            double changePercent = 0.0;
            if (!cp.trim().isEmpty() && !cp.trim().equals(null))
                changePercent = Double.parseDouble(cp);

            final Stock stock = new Stock(symbol, name);
            stock.setLatestPrice(latestPrice);
            stock.setPriceChange(priceChange);
            stock.setChangePercentage(changePercent);

            Log.d(TAG, "process: " + stock.toString());

            mainActivity.runOnUiThread(() -> mainActivity.addStock(stock));

        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
