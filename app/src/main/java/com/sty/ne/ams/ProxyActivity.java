package com.sty.ne.ams;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Author: ShiTianyi
 * Time: 2021/11/17 0017 20:03
 * Description:
 */
public class ProxyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "我是代理Activity....", Toast.LENGTH_SHORT).show();
    }
}
