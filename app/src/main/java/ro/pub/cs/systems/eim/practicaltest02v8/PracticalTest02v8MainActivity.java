package ro.pub.cs.systems.eim.practicaltest02v8;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PracticalTest02v8MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText currencyEditText;
    private TextView resultTextView;
    private Button requestButton;

    private CacheManager cacheManager = new CacheManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openCalculatorButton = findViewById(R.id.navigateButton);
        openCalculatorButton.setOnClickListener(v -> {
            Intent intent = new Intent(PracticalTest02v8MainActivity.this, CalculatorActivity.class);
            startActivity(intent);
        });

        // Inițializează componentele UI
        currencyEditText = findViewById(R.id.currencyEditText);
        resultTextView = findViewById(R.id.resultTextView);
        requestButton = findViewById(R.id.requestButton);

        // Setează OnClickListener pentru buton
        requestButton.setOnClickListener(v -> {
            String currency = currencyEditText.getText().toString().trim().toUpperCase();
            if (!currency.isEmpty()) {
                fetchExchangeRate(currency);
            } else {
                resultTextView.setText("Introduceți o valută validă (ex: USD, EUR).");
            }
        });
    }

    private void fetchExchangeRate(String currency) {
        String cachedData = cacheManager.getCachedData();
        if (cachedData != null) {
            runOnUiThread(() -> resultTextView.setText("Din cache: " + cachedData));
            return;
        }

        new Thread(() -> {
            try {
                String apiUrl = "https://api.coindesk.com/v1/bpi/currentprice/" + currency + ".json";
                URL url = new URL(apiUrl);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Log.d(TAG, "Răspuns primit: " + response.toString());

                // Parsează JSON-ul
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONObject bpiObject = jsonObject.getJSONObject("bpi");
                JSONObject currencyObject = bpiObject.getJSONObject(currency);

                String rate = currencyObject.getString("rate");

                cacheManager.updateCache(rate);

                // Actualizează UI-ul pe thread-ul principal
                runOnUiThread(() -> resultTextView.setText("Cursul valutar pentru " + currency + ": " + rate));

            } catch (Exception e) {
                Log.e(TAG, "Eroare la obținerea datelor: ", e);
                runOnUiThread(() -> resultTextView.setText("Eroare la obținerea cursului valutar."));
            }
        }).start();
    }
}