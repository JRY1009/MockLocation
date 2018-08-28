package com.xp.pro.mocklocation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.xp.pro.mocklocation.baidu.RoutePlanDemo;
import com.xp.pro.mocklocationlib.LocationBean;
import com.xp.pro.mocklocationlib.LocationWidget;

public class LocationActivity extends Activity {
    LocationWidget idLocationWidget;
    LocationBean mLocationBean;

    Button mBaiduBtn;
    Button mAmapBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_content_view);


        initMockLocationData();
        initView();
    }

    private void initMockLocationData() {
        double latitude;
        double longitude;
        try {
            latitude = getIntent().getDoubleExtra("latitude", 40.033096);
            longitude = getIntent().getDoubleExtra("longitude", 116.504051);

        } catch (Exception e) {
            latitude = 0;
            longitude = 0;
        }
        mLocationBean = new LocationBean();
        mLocationBean.setLatitude(latitude);
        mLocationBean.setLongitude(longitude);
    }

    private void initView() {
        idLocationWidget = (LocationWidget) findViewById(R.id.id_location_wigdet);

        mBaiduBtn = (Button) findViewById(R.id.btn_baidu);
        mBaiduBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(LocationActivity.this, RoutePlanDemo.class);
                startActivity(intent);
            }
        });

        mAmapBtn = (Button) findViewById(R.id.btn_amap);
        mAmapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        idLocationWidget.setMangerLocationData(mLocationBean.getLatitude(), mLocationBean.getLongitude());
        idLocationWidget.startMockLocation();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        idLocationWidget.refreshData();
    }

    @Override
    protected void onPause() {
        idLocationWidget.removeUpdates();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        idLocationWidget.stopMockLocation();
        super.onDestroy();
    }
}