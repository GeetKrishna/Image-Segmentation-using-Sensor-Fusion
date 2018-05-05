package com.example.nisch.group20.graph;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.nisch.group20.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nisch on 4/24/2018.
 */

public class GraphForIdeal extends AppCompatActivity {

    WebView webview;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_ideal);

        final String URL = "https://api.nal.usda.gov/ndb/nutrients/?format=json&api_key=h6E3WwtYwCzjTDQnfzyFghq4ipvbdr6udQrGfjVW&nutrients=304&nutrients=203&nutrients=268&nutrients=401&nutrients=307&nutrients=269&nutrients=303&nutrients=291&nutrients=305&nutrients=301&nutrients=204&nutrients=205&fg=0100&fg=2200&fg=0100&fg=1100&max=15";

        queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = null;
        final Map<String, String> data = new HashMap<>();
        jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject iterator = null;
                        try {
                            iterator = response.getJSONObject("report");
                            JSONArray iterator1 = iterator.getJSONArray("foods");
                            for (int j = 0; j < 10;j++) {

                                JSONObject it1 = iterator1.getJSONObject(j);
                                JSONArray it2 = it1.getJSONArray("nutrients");

                                for (int i = 0; i < it2.length(); i++) {
                                    JSONObject obj = it2.getJSONObject(i);
                                    String name = obj.getString("nutrient");
                                    String value = obj.getString("value");
                                    value = value.equals("--") ? "0" : value;
                                    double price = data.containsKey(name) ? Double.valueOf(data.get(name)) : 0;
                                    price += Double.valueOf(value);
                                    data.put(name, String.valueOf(price));
                                }
                            }

                            final JSONObject json = new JSONObject(data);

                            webview = findViewById(R.id.webview);
                            final WebSettings webSettings = webview.getSettings();
                            webSettings.setJavaScriptEnabled(true);
                            webSettings.setBuiltInZoomControls(true);
                            webSettings.setSupportZoom(true);
                            webSettings.setUseWideViewPort(true);
                            webview.setWebChromeClient(new WebChromeClient());
                            webview.setInitialScale(1);
                            webview.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    webview.loadUrl("javascript:showGraph(" + json + ")" );
                                }
                            });
                            webview.loadUrl("file:///android_asset/html/graph.html");

                            TextView idealText = findViewById(R.id.ideals);
                            idealText.setText("Ideal Nutrients for Food groups - " +
                                    "Dairy and Egg Products, Poultry Products, " +
                                    "Vegetables and Vegetable Products " +
                                    "Meals, Entrees, and Side Dishes");

                            Button diary = findViewById(R.id.feedback);
                            diary.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(GraphForIdeal.this, FoodDiary.class);
                                    startActivity(i);
                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        queue.add(jsonObjectRequest);
    }
}
