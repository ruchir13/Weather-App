package com.weatherapp.presenter;

import com.weatherapp.model.ListClass;
import com.weatherapp.model.Main;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Ruchir on 11-11-2016.
 */

public interface WeatherInterface {
    @GET("/data/2.5/forecast")
//Weather web API
    void getCurrentWeather(@Query("q") String city, @Query("appid") String appid, Callback<Main> jsonObjectCallback);
}
