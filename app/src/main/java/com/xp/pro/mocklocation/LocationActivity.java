package com.xp.pro.mocklocation;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNTTSManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.cyfonly.flogger.FLogger;
import com.cyfonly.flogger.constants.Constant;
import com.xp.pro.mocklocation.baidu.NormalUtils;
import com.xp.pro.mocklocation.baidu.RoutePlanDemo;
import com.xp.pro.mocklocationlib.LocationBean;
import com.xp.pro.mocklocationlib.LocationWidget;

import java.io.File;

public class LocationActivity extends Activity implements OnGetGeoCoderResultListener {
    LocationBean mLocationBean;

    private static final String APP_FOLDER_NAME = "MockLocationDemo";
    private static final int REQUEST_PERMISSION_LOCATION = 255;
    private static final int authBaseRequestCode = 1;

    Button mBaiduBtn;
    Button mBaiduLogBtn;
    Button mAmapBtn;

    EditText mDivEt;
    Button mDivBtn;

    private boolean hasInitSuccess = false;
    private String mSDCardPath = null;
    GeoCoder mGeoCoder = null; // 搜索模块，也可去掉地图模块独立使用

    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_content_view);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_LOCATION);
            }
        }

        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(this);

        initMockLocationData();
        initView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We now have permission to use the location
            }
        }
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
        LogicLocation.getInstance().setLocationWidget((LocationWidget) findViewById(R.id.id_location_wigdet));

        mBaiduBtn = (Button) findViewById(R.id.btn_baidu);
        mBaiduBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (initDirs()) {
                    initNavi(false);
                }

                Intent intent;
                intent = new Intent(LocationActivity.this, RoutePlanDemo.class);
                startActivity(intent);
            }
        });

        mBaiduLogBtn = (Button) findViewById(R.id.btn_baidu_log);
        mBaiduLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (initDirs()) {
                    initNavi(true);
                }

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

        mDivEt = (EditText) findViewById(R.id.et_div);
        mDivBtn = (Button) findViewById(R.id.btn_div);
        mDivBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int div = Integer.parseInt(mDivEt.getText().toString());
                LogicLocation.getInstance().setDiv(div);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogicLocation.getInstance().setLocation(mLocationBean.getLatitude(), mLocationBean.getLongitude());
        LogicLocation.getInstance().startMock();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        LogicLocation.getInstance().refreshData();
    }

    @Override
    protected void onPause() {
        LogicLocation.getInstance().removeUpdates();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LogicLocation.getInstance().stopMock();
        super.onDestroy();
    }


    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean hasBasePhoneAuth() {
        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initNavi(final boolean log) {
        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;
            }
        }

        BaiduNaviManagerFactory.getBaiduNaviManager().init(this,
                mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {

                    @Override
                    public void onAuthResult(int status, String msg) {
                        String result;
                        if (0 == status) {
                            result = "key校验成功!";
                        } else {
                            result = "key校验失败, " + msg;
                        }
                        Toast.makeText(LocationActivity.this, result, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void initStart() {
                        Toast.makeText(LocationActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void initSuccess() {
                        Toast.makeText(LocationActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                        hasInitSuccess = true;
                        // 初始化tts
                        initTTS(log);
                    }

                    @Override
                    public void initFailed() {
                        Toast.makeText(LocationActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    // 外置tts时需要实现的接口回调
    private IBNTTSManager.IBNOuterTTSPlayerCallback mTTSCallback = new IBNTTSManager.IBNOuterTTSPlayerCallback() {

        @Override
        public int getTTSState() {
//            /** 播放器空闲 */
//            int PLAYER_STATE_IDLE = 1;
//            /** 播放器正在播报 */
//            int PLAYER_STATE_PLAYING = 2;
            return PLAYER_STATE_IDLE;
        }

        @Override
        public int playTTSText(String text, String s1, int i, String s2) {
            Log.e("BNSDKDemo", "playTTSText:" + text);
            FLogger.getInstance().writeLog("TTSText_" + NormalUtils.sDate, Constant.INFO, String.format("%s %s %d %s", text, s1, i, s2));

            if (LogicLocation.getInstance().getCurrentLocation() != null) {
                LatLng ptCenter = new LatLng(LogicLocation.getInstance().getCurrentLocation().getLatitude(),
                        LogicLocation.getInstance().getCurrentLocation().getLongitude());

                mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(ptCenter).newVersion(1));
            }

            return 0;
        }

        @Override
        public void stopTTS() {
            Log.e("BNSDKDemo", "stopTTS");
        }
    };

    private void initTTS(boolean log) {
        if (log) {
            // 不使用内置TTS
            BaiduNaviManagerFactory.getTTSManager().initTTS(mTTSCallback);
        } else {

            // 使用内置TTS
            BaiduNaviManagerFactory.getTTSManager().initTTS(getApplicationContext(),
                    getSdcardDir(), APP_FOLDER_NAME, NormalUtils.getTTSAppID());
        }

        // 注册同步内置tts状态回调
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedListener(
                new IBNTTSManager.IOnTTSPlayStateChangedListener() {
                    @Override
                    public void onPlayStart() {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayStart");
                    }

                    @Override
                    public void onPlayEnd(String speechId) {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayEnd");
                    }

                    @Override
                    public void onPlayError(int code, String message) {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayError");
                    }
                }
        );

        // 注册内置tts 异步状态消息
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedHandler(
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Log.e("BNSDKDemo", "ttsHandler.msg.what=" + msg.what);
                    }
                }
        );
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            FLogger.getInstance().writeLog("TTSText_" + NormalUtils.sDate, Constant.INFO, String.format("Address: 未能找到结果"));
            return;
        }

        FLogger.getInstance().writeLog("TTSText_" + NormalUtils.sDate, Constant.INFO, String.format("lat: %f, long: %f, Address: %s ", result.getLocation().latitude, result.getLocation().longitude, result.getAddress()));
    }
}