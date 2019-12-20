package com.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView tx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tx = findViewById(R.id.tx);
        findViewById(R.id.btn).setOnClickListener((View v) -> request());
    }

    private void request() {
        String requestUrl = "https://192.168.0.156:8888/demo/test";
        Request request = new Request.Builder().url(requestUrl).get().build();

        OkHttpInstance.getmOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String bodyStr = responseBody.string();
                    OkHttpInstance.getMainThreadHandler().post(() -> tx.setText(bodyStr));
                    Log.e(TAG, "responseBody: " + bodyStr);
                }
            }
        });
    }
}
