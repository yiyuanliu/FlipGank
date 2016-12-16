package com.yiyuanliu.flipgank.data;


import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by YiyuanLiu on 2016/12/15.
 */

public interface Api {
    String BASE_URL = "http://gank.io/";

    @GET("api/day/{year}/{month}/{day}")
    Observable<GankResponse> loadData(@Path("year") int year, @Path("month") int month, @Path("day") int day);
}
