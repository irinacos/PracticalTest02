package ro.pub.cs.systems.eim.practicaltest02v8;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CalculatorActivity extends AppCompatActivity {

    private static final String TAG = "CalculatorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        EditText operand1EditText = findViewById(R.id.operand1EditText);
        EditText operand2EditText = findViewById(R.id.operand2EditText);
        EditText operationEditText = findViewById(R.id.operationEditText);
        Button calculateButton = findViewById(R.id.calculateButton);
        TextView resultTextView = findViewById(R.id.resultTextView);

        calculateButton.setOnClickListener(v -> {
            String operand1 = operand1EditText.getText().toString().trim();
            String operand2 = operand2EditText.getText().toString().trim();
            String operation = operationEditText.getText().toString().trim();

            if (!operand1.isEmpty() && !operand2.isEmpty() && !operation.isEmpty()) {
                fetchCalculationResult(operation, operand1, operand2, resultTextView);
            } else {
                resultTextView.setText("Introduceți valori valide.");
            }
        });
    }

    private void fetchCalculationResult(String operation, String operand1, String operand2, TextView resultTextView) {
        new Thread(() -> {
            try {
                // Construiște URL-ul cererii
                String apiUrl = "http://10.0.2.2:8080/expr/expr_get.php?operation=" + operation +
                        "&t1=" + operand1 + "&t2=" + operand2;
                Log.d(TAG, "URL-ul cererii: " + apiUrl);

                // Trimite cererea HTTP
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Codul răspunsului: " + responseCode);

                if (responseCode == 200) {
                    // Citește răspunsul
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Afișează răspunsul complet în Logcat
                    Log.d(TAG, "Răspuns primit de la serviciul web: " + response.toString());

                    // Parsează răspunsul JSON (dacă este cazul)
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String result = jsonObject.getString("result");

                    // Actualizează UI-ul
                    runOnUiThread(() -> resultTextView.setText("Rezultatul: " + result));
                } else {
                    // În caz de eroare
                    Log.e(TAG, "Eroare la server: " + connection.getResponseMessage());
                    runOnUiThread(() -> {
                        try {
                            resultTextView.setText("Eroare la server: " + connection.getResponseMessage());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

            } catch (Exception e) {
                // În caz de excepție
                Log.e(TAG, "Eroare la calcul: ", e);
                runOnUiThread(() -> resultTextView.setText("Eroare la calcul."));
            }
        }).start();
    }
}
