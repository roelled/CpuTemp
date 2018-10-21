package com.example.danelroelle.cputemp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.loopj.android.http.*;

import org.w3c.dom.Text;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button getTemp;

    private Button httpRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.getTemp = findViewById(R.id.buttonGetTemp);
        this.getTemp.setOnClickListener(this);

        this.httpRequestButton = findViewById(R.id.buttonHttpRequest);
        this.httpRequestButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case (R.id.buttonGetTemp):
                TextView setTempCpu = findViewById(R.id.tVCpuTemp);
                String tempCpu = Float.toString(getCpuTemp());
                setTempCpu.setText(tempCpu);
                break;
            case (R.id.buttonHttpRequest):
                httpClient();
                break;
        }

    }

    public float getCpuTemp() {
        Process p;
        try {
            p = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp");
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = reader.readLine();
            float temp = Float.parseFloat(line) / 1000.0f;

            return temp;

        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    public void httpClient() {

        TextView cpuTemp = findViewById(R.id.tVCpuTemp);

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String strDate = dateFormat.format(date);
        String strTemp = cpuTemp.getText().toString();

        String logString = strDate + " " + strTemp;

        AsyncHttpClient client = new AsyncHttpClient();
        StringEntity entity = null;
        try {
            entity = new StringEntity(logString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        client.post(this,"http://172.16.0.26:8085/upload", entity, "text/plain", new AsyncHttpResponseHandler() {
            TextView fehlermeldung = findViewById(R.id.tVFehlerHttp);
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                fehlermeldung.setText("Daten wurden Erfolgreich gesendet.");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                fehlermeldung.setText("Keine Verbindung zum Backend.");
            }
        });

    }

}
