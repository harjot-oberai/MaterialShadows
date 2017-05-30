package com.sdsmdg.harjot.materialshadowsdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.sdsmdg.harjot.materialshadows.MaterialShadowViewWrapper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CheckBox calculateAsync;
    private CheckBox showWhenAllReady;

    private MaterialShadowViewWrapper shadowViewWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shadowViewWrapper = (MaterialShadowViewWrapper) findViewById(R.id.shadow_wrapper);
        calculateAsync = (CheckBox) findViewById(R.id.cb_calculate_async);
        showWhenAllReady = (CheckBox) findViewById(R.id.cb_show_when_all_ready);

        findViewById(R.id.btn_re_render).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        shadowViewWrapper.setShouldCalculateAsync(calculateAsync.isChecked());
        shadowViewWrapper.setShowShadowsWhenAllReady(showWhenAllReady.isChecked());
        shadowViewWrapper.requestLayout();
    }

}
