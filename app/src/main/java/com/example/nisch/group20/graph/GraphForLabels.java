package com.example.nisch.group20.graph;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nisch on 4/23/2018.
 */

public class GraphForLabels extends AppCompatActivity {
    WebView webview;
    RequestQueue queue;
    File fileToDownload;
    AmazonS3 s3;
    TransferUtility transferUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        fileToDownload = new File(Environment.getExternalStorageDirectory().getAbsolutePath() ,"food_label.txt");

        credentialsProvider();
        setTransferUtility();
        setFileToDownload();

        queue = Volley.newRequestQueue(this);

    }

    private ArrayList<String> getLabels() {

        //Read text from file
        StringBuilder text = new StringBuilder();
        ArrayList<String> labels = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(Environment.getExternalStorageDirectory().getAbsolutePath() ,"" +
                    "food_label.txt")));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                labels.add(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            Log.e("C2c", "Error occured while reading text file!!");
            //You'll need to add proper error handling here
        }

        //Find the view by its id
       return labels;
    }

    private void USDARestCall(final RequestQueue requestQueue, final List<String> labels, final String[] value1) {
        JsonObjectRequest jsonObjectRequest = null;
        for (int i = 0; i< labels.size() ; i++) {
        String URL = "https://api.nal.usda.gov/ndb/search/?format=json&" +
                "q=" + labels.get(i) + "&sort=n&max=1&offset=0&api_key=h6E3WwtYwCzjTDQnfzyFghq4ipvbdr6udQrGfjVW";
            final int finalI = i;
            jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONObject iterator = null;
                        try {
                            iterator = response.getJSONObject("list");
                            JSONArray iterator1 = iterator.getJSONArray("item");
                            JSONObject jsonObject1 = iterator1.getJSONObject(0);
                            value1[finalI] = jsonObject1.optString("ndbno");

                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }
                        NutrientRestCall(requestQueue, value1, labels);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
            requestQueue.add(jsonObjectRequest);
    }

    }

    private void NutrientRestCall(final RequestQueue requestQueue, final String[] value1, final List<String> labels) {
        if (value1[labels.size()-1] == null)
            return;

        StringBuilder URL = new StringBuilder("https://api.nal.usda.gov/ndb/V2/reports?" +
                "type=f&format=json&api_key=h6E3WwtYwCzjTDQnfzyFghq4ipvbdr6udQrGfjVW");

        final Map<String, String> data = new HashMap<>();

        for (int i = 0 ;i < value1.length; i++) {
            URL.append("&ndbno=" + value1[i]);
        }
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                URL.toString(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray it = null;
                        try {

                            it = response.getJSONArray("foods");
                            for (int j = 0; j < it.length();j++) {

                                JSONObject it1 = it.getJSONObject(j);
                                JSONObject it3 = it1.getJSONObject("food");
                                JSONArray it2 = it3.getJSONArray("nutrients");

                                int size = it2.length() < 10 ? it2.length() : 10;
                                for (int i = 0; i < size; i++) {
                                    JSONObject obj = it2.getJSONObject(i);
                                    String name = obj.getString("name");
                                    Double value = obj.getDouble("value");
                                    int price = data.containsKey(name) ? Integer.valueOf(data.get(name)) : 0;
                                    price += value;
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

                            TextView selectedLabels = findViewById(R.id.selectLabels);
                            StringBuilder selectLabels = new StringBuilder("The Food Labels are ");

                            for (int i = 0 ; i < value1.length; i++) {
                                selectLabels.append(labels.get(i) + " (" + value1[i] + ") ");
                            }

                            selectedLabels.setText(selectLabels.toString());

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

        requestQueue.add(jsonObjectRequest);
        Button ideal = findViewById(R.id.ideal);
        ideal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(GraphForLabels.this,"Ideal Food Chart For Food Groups",Toast.LENGTH_SHORT).show();
                Intent i = new Intent(GraphForLabels.this, GraphForIdeal.class);
                startActivity(i);
            }
        });
    }

    public void credentialsProvider(){

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:89c5978f-964e-4b9e-9449-142ec135c883", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        setAmazonS3Client(credentialsProvider);
    }

    public void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider){

        // Create an S3 client
        s3 = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));

    }

    public void setTransferUtility(){
        transferUtility = new TransferUtility(s3, getApplicationContext());
    }

    public void setFileToDownload(){

        TransferObserver transferObserver = transferUtility.download(
                "test-1bucket",     /* The bucket to upload to */
                "food_labels.txt",    /* The key for the uploaded object */
                fileToDownload        /* The file to download the object to */
        );

        transferObserverListener(transferObserver);

    }

    public void transferObserverListener(TransferObserver transferObserver){

        transferObserver.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.e("statechange", state+" ");
                if (state == TransferState.COMPLETED) {
                    List<String> labels = getLabels();

                    final String[] value1 = new String[labels.size()];
                    USDARestCall(queue,labels, value1);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("error","error");
            }

        });
    }

}
