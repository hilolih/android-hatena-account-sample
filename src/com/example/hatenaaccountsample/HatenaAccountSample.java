package com.example.hatenaaccountsample;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HatenaAccountSample extends HatenaActivity {
    protected TextView mInfoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mInfoView = (TextView)findViewById(R.id.info);

        ((Button)findViewById(R.id.open_setting)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HatenaAccountSample.this, Setting.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInfo();
    }

    protected void updateInfo() {
        String username = mPref.getString(Setting.ACCOUNT_NAME, null);
        mInfoView.setText(String.format("Username: %s", username));

        mExecutor.execute(new Runnable() { public void run () {
            try {
                HttpResponse res = request(new HttpGet(String.format("http://b.hatena.ne.jp/my.name?%d", System.currentTimeMillis())));

                final StringBuilder body = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        body.append(line).append("\n");
                    }
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() { public void run () {
                    mInfoView.setText(String.format("b.hatena.ne.jp/my.name: %s", body.toString()));
                } });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } });
    }

}
