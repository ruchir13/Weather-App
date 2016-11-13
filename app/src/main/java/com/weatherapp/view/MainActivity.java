package com.weatherapp.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Observable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.weatherapp.ItemTypeAdapterFactory;
import com.weatherapp.R;
import com.weatherapp.model.ListClass;
import com.weatherapp.model.Main;
import com.weatherapp.model.MyPojo;
import com.weatherapp.presenter.WeatherInterface;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity implements LocationListener {

    private RestAdapter restAdapter = null;
    private String cityName = "";
    private TextView txtCityName, txtCurrentTemp, txtMax, txtMin;

    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        getCurrentLocation();

    }

    private void initializeViews() {
        txtCityName = (TextView) findViewById(R.id.txtCityName);
        txtCurrentTemp = (TextView) findViewById(R.id.txtCurrentTemp);
        txtMax = (TextView) findViewById(R.id.txtMax);
        txtMin = (TextView) findViewById(R.id.txtMin);
    }

    private void getCurrentLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                onLocationChanged(location);
                if (cityName != null && cityName.trim().length() > 0) {
                    getWeatherData();
                } else {
                    Toast.makeText(this, "Location Not Found", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Location Not Found", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException ex) {
            Log.e("Location Exception", ex.toString());
        }
    }

    private void getWeatherData() {

        try {
            RestAdapter restAdapter = sendDataToServer(this);
            WeatherInterface weatherInterface = restAdapter.create(WeatherInterface.class);
            weatherInterface.getCurrentWeather(cityName, getResources().getString(R.string.appid), new Callback<Main>() {

                @Override
                public void success(Main main, Response response) {
                    txtCityName.setText(cityName);
                    /*txtCurrentTemp.setText(main.getTemp().toString());
                    txtMax.setText(main.getTemp_max().toString());
                    txtMin.setText(main.getTemp_min().toString());*/
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("Error", error.toString());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RestAdapter sendDataToServer(Activity activity) throws IOException {

        RequestInterceptor inerceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Content-Language", "en-US");
                request.addHeader("Content-Type", "application/x-www-form-urlencoded");
                request.addHeader("Connection", "Keep-Alive");
            }
        };


        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ItemTypeAdapterFactory()).create();
        restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(activity.getResources().getString(R.string.url))
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(inerceptor)
                .build();
        return restAdapter;
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> addresses = geoCoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                for (int i = 0; i < 1; i++) {
                    Address address = addresses.get(i);
                    if (address.getLocality() != null
                            && address.getLocality() != "") {
                        cityName = address.getLocality();
                    }
                }
            }
            Log.d("Location Found", cityName);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }
}
