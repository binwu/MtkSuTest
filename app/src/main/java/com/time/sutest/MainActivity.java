package com.time.sutest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MtkSuTool.CommandResultCallback {
    private Button bt_release_64, bt_release_32, bt_run;
    private EditText et_cmd;
    private TextView tv_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MtkSuTool.getInstance(this).releaseSu(R.raw.mtksu);//default release 32bit file
        initView();
        MtkSuTool.getInstance(this).regCallBack(this);
    }

    private void initView() {
        bt_release_32 = findViewById(R.id.bt_release_32);
        bt_release_64 = findViewById(R.id.bt_release_64);
        bt_run = findViewById(R.id.bt_run);
        et_cmd = findViewById(R.id.et_cmd);
        tv_result = findViewById(R.id.tv_result);

        bt_release_32.setOnClickListener(this);
        bt_release_64.setOnClickListener(this);
        bt_run.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_release_32:
                MtkSuTool.getInstance(this).releaseSu(R.raw.mtksu);
                break;
            case R.id.bt_release_64:
                MtkSuTool.getInstance(this).releaseSu(R.raw.mtksu64);
                break;
            case R.id.bt_run:
                MtkSuTool.getInstance(this).runExec(et_cmd.getText().toString());
                break;
        }
    }

    @Override
    public void onResult(final String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_result.setText(tv_result.getText() + "\n" + result);
            }
        });
    }
}