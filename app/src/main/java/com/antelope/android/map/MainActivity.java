package com.antelope.android.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import static com.amap.api.maps2d.LocationSource.*;

public class MainActivity extends AppCompatActivity{

    @BindView(R.id.position_tv)
    TextView mPositionTv;
    @BindView(R.id.mapView)
    MapView mMapView;
    private Unbinder unbinder;

    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient;
    private OnLocationChangedListener mListener;
    private AMapLocationClientOption mLocationOption;
    private AMap aMap;
    private MyLocationStyle mMyLocationStyle;
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示
        mMyLocationStyle = new MyLocationStyle();
        mMyLocationStyle.interval(1000);
        mMyLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER) ;//定位一次，且将视角移动到地图中心点。
        mMyLocationStyle.showMyLocation(true);
        aMap.setMyLocationStyle(mMyLocationStyle);//设置定位蓝点的style
        aMap.setMyLocationEnabled(true);//设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.startLocation();
    }

    private void initLocation() {
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        mLocationOption.setInterval(1000);
        mLocationOption.isNeedAddress();
        mLocationClient.setLocationOption(mLocationOption);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.stopLocation();
        aMap.setMyLocationEnabled(false);
        unbinder.unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本权限", Toast.LENGTH_LONG).show();
                            finish();//关闭当前程序
                            return;
                        }
                    }
                    requestLocation();//当所有权限被用户同意，才会调用requestLocation()开始地理位置定位
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                StringBuilder currentPosition = new StringBuilder();
                currentPosition.append("纬度：").append(aMapLocation.getLatitude()).append("\n");
                currentPosition.append("经度：").append(aMapLocation.getLongitude()).append("\n");
                currentPosition.append("国家：").append(aMapLocation.getCountry()).append("\n");
                currentPosition.append("省：").append(aMapLocation.getProvince()).append("\n");
                currentPosition.append("城市：").append(aMapLocation.getCity()).append("\n");
                currentPosition.append("城区：").append(aMapLocation.getDistrict()).append("\n");
                currentPosition.append("街道：").append(aMapLocation.getStreet()).append("\n");
                currentPosition.append("建筑物Id：").append(aMapLocation.getBuildingId()).append("\n");
                currentPosition.append("当前室内定位的楼层：").append(aMapLocation.getFloor()).append("\n");
                currentPosition.append("AOI信息：").append(aMapLocation.getAoiName()).append("\n");
                currentPosition.append("定位方式：");
                if (aMapLocation.getLocationType() == AMapLocation.LOCATION_TYPE_GPS) {
                    currentPosition.append("GPS");
                } else if (aMapLocation.getLocationType() == AMapLocation.LOCATION_TYPE_WIFI) {
                    currentPosition.append("WIFI");
                } else if (aMapLocation.getLocationType() == AMapLocation.LOCATION_TYPE_CELL) {
                    currentPosition.append("基站定位");
                }
                mPositionTv.setText(currentPosition);
            } else {
                Log.e("MapError", "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }

        }
    };

}
