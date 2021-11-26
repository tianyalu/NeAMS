package com.sty.ne.ams;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button btnJump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListeners();
    }

    private void initView() {
        btnJump = findViewById(R.id.btn_jump);
    }

    private void initListeners() {
        btnJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnJumpClicked();
            }
        });
    }

    private void onBtnJumpClicked() {
        //简述：很多源码级的跳转，省略... --> AMS四大组件（管理、进程控制，调度等等）--> 检测LoginActivity是否在清单文件中注册
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }
}