package com.example.hatenaaccountsample;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

abstract class HatenaActivity extends Activity {
    private static final String TAG = "HatenaActivity";

    private static DefaultHttpClient sHttpClient;
    static {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
            new Scheme(
                HttpHost.DEFAULT_SCHEME_NAME,
                PlainSocketFactory.getSocketFactory(),
                80
            )
        );

        HttpParams httpParams = new BasicHttpParams();
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);

        sHttpClient = new DefaultHttpClient(
            new ThreadSafeClientConnManager(httpParams, schemeRegistry),
            httpParams
        );

        HttpConnectionParams.setSoTimeout(sHttpClient.getParams(), 1000 * 30);
        HttpConnectionParams.setConnectionTimeout(sHttpClient.getParams(), 1000 * 5); 
        HttpClientParams.setRedirecting(sHttpClient.getParams(), false);
    }

    protected SharedPreferences mPref;
    protected ExecutorService mExecutor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutor.shutdownNow();
    }

    public HttpResponse request(HttpUriRequest req) throws IOException {
        String rk = mPref.getString(Setting.ACCOUNT_RK, null);

        if (rk != null) {
            BasicClientCookie cookie = new BasicClientCookie("rk", rk);
            cookie.setDomain(".hatena.ne.jp");
            sHttpClient.getCookieStore().addCookie(cookie);
        }

        return sHttpClient.execute(req);
    }
}
