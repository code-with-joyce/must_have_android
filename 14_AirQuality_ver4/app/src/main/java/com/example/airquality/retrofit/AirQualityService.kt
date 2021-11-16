package com.example.airquality.retrofit

import com.example.airquality.retrofit.AirQualityResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author Joyce Hong
 * @email joycehong0524@gmail.com
 * @created 2021/09/03
 * @desc
 */
interface AirQualityService {

    @GET("nearest_city")
    fun getAirQualityData(@Query("lat") lat : String, @Query("lon") lon : String, @Query("key") key : String ) : Call<AirQualityResponse>

}
