package com.xp.pro.mocklocation;


import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.RouteStep;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.cyfonly.flogger.FLogger;
import com.cyfonly.flogger.constants.Constant;
import com.xp.pro.mocklocation.baidu.NormalUtils;
import com.xp.pro.mocklocationlib.LocationBean;
import com.xp.pro.mocklocationlib.LocationWidget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhoujing on 2017/10/19.
 */

public class LogicLocation {

    static LogicLocation instance = null;

    private LocationWidget mLocationWidget;
    private RouteLine mRoute = null;
    private List<LocationBean> mBeanList = new ArrayList<>();
    private LocationBean mCurrentBean;

    private int mDiv = 5;

    public static LogicLocation getInstance(){
        if (null == instance)
            instance = new LogicLocation();
        return instance;
    }


    private LogicLocation(){

    }

    public void setDiv(int div) {
        mDiv = div;
    }

    public LocationWidget getLocationWidget() {
        return mLocationWidget;
    }

    public void setLocationWidget(LocationWidget locationWidget) {
        mLocationWidget = locationWidget;
    }

    public void setLocation(double latitude, double longitude) {
        mLocationWidget.setMangerLocationData(latitude, longitude);
    }

    public LocationBean getCurrentLocation() {
        return  mCurrentBean;
    }

    public void startMock() {
        mLocationWidget.startMockLocation();
    }

    public void stopMock() {
        mLocationWidget.stopMockLocation();
    }

    public void refreshData() {
        mLocationWidget.refreshData();
    }

    public void removeUpdates() {
        mLocationWidget.removeUpdates();
    }

    public void setRoute(RouteLine route) {
        mRoute = route;
    }

    public void startNavi() {

        if (mBeanList == null) {
            mBeanList = new ArrayList<>();
        }
        mBeanList.clear();

        {
            LocationBean locationBean = new LocationBean();
            locationBean.setLongitude(mRoute.getStarting().getLocation().longitude);
            locationBean.setLatitude(mRoute.getStarting().getLocation().latitude);
            mBeanList.add(locationBean);
        }

        for (int i=0; i<mRoute.getAllStep().size(); i++) {

            RouteStep step = (RouteStep) mRoute.getAllStep().get(i);
            for (int j=0; j<step.getWayPoints().size(); j++) {

                LocationBean locationBean = new LocationBean();
                locationBean.setLongitude(step.getWayPoints().get(j).longitude);
                locationBean.setLatitude(step.getWayPoints().get(j).latitude);

                LocationBean lastBean = mBeanList.get(mBeanList.size() - 1);
                double chaLongitude = (locationBean.getLongitude() - lastBean.getLongitude()) / (double)mDiv;
                double chaLatitude = (locationBean.getLatitude() - lastBean.getLatitude()) / (double)mDiv;

                for (int k=1; k<mDiv; k++) {
                    LocationBean chaBean = new LocationBean();
                    chaBean.setLongitude(lastBean.getLongitude() + chaLongitude * k);
                    chaBean.setLatitude(lastBean.getLatitude() + chaLatitude * k);
                    mBeanList.add(chaBean);
                }
                mBeanList.add(locationBean);
            }
        }

        {
            LocationBean locationBean = new LocationBean();
            locationBean.setLongitude(mRoute.getTerminal().getLocation().longitude);
            locationBean.setLatitude(mRoute.getTerminal().getLocation().latitude);

            LocationBean lastBean = mBeanList.get(mBeanList.size() - 1);
            double chaLongitude = (locationBean.getLongitude() - lastBean.getLongitude()) / (double)mDiv;
            double chaLatitude = (locationBean.getLatitude() - lastBean.getLatitude()) / (double)mDiv;
            for (int k=1; k<5; k++) {
                LocationBean chaBean = new LocationBean();
                chaBean.setLongitude(lastBean.getLongitude() + chaLongitude * k);
                chaBean.setLatitude(lastBean.getLatitude() + chaLatitude * k);
                mBeanList.add(chaBean);
            }

            mBeanList.add(locationBean);
        }

        for (int i=0; i<mBeanList.size(); i++) {
            FLogger.getInstance().writeLog("RouteList_" + NormalUtils.sDate, Constant.INFO, String.format("lat: %f, long: %f", mBeanList.get(i).getLatitude(), mBeanList.get(i).getLongitude()));
        }

        new Thread(new RunnableNavi()).start();
    }

    /**
     * 模拟位置线程
     */
    private class RunnableNavi implements Runnable {

        @Override
        public void run() {

            for (int i=0; i<mBeanList.size(); i++) {
                try {
                    Thread.sleep(1000);
                    LocationBean locationBean = mBeanList.get(i);
                    mCurrentBean = locationBean;

                    Gps gps = PositionUtil.bd09_To_Gps84(locationBean.getLatitude(), locationBean.getLongitude());

                    LogicLocation.getInstance().setLocation(gps.getWgLat(), gps.getWgLon());

                    Log.i("LogicLocation", "size:" + mBeanList.size() + ";pos:" + i + ";long:" + locationBean.getLongitude() + ";lat:" + locationBean.getLatitude());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
