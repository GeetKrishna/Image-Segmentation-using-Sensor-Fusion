package com.example.nisch.group20.graph;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.nisch.group20.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMImplementation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nisch on 5/3/2018.
 */

public class FoodDiary extends AppCompatActivity {
    WebView webview;
    JSONObject json = new JSONObject();
    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_graph);


        List<Integer> nutritionForEachDay = new ArrayList<>();

        final Calendar oneDayBefore = Calendar.getInstance();
        oneDayBefore.add(Calendar.DATE, -1);

        final Calendar twoDayBefore = Calendar.getInstance();
        twoDayBefore.add(Calendar.DATE, -2);

        final Calendar threeDayBefore = Calendar.getInstance();
        threeDayBefore.add(Calendar.DATE, -3);

        try {
            json.put(String.valueOf(dateFormat.format(date)),nutritionForEachDay);
            json.put(String.valueOf(dateFormat.format(oneDayBefore.getTime())),nutritionForEachDay);
            json.put(String.valueOf(dateFormat.format(twoDayBefore.getTime())),nutritionForEachDay);
            json.put(String.valueOf(dateFormat.format(threeDayBefore.getTime())),nutritionForEachDay);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                webview.loadUrl("javascript:showGraph(" + json + ")");
            }
        });
        webview.loadUrl("file:///android_asset/html/graph_diary.html");
    }

}
