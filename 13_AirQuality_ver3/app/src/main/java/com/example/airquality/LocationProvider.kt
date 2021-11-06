package com.example.airquality

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.requestLocationUpdates

/**
 * @author Joyce Hong
 * @email joycehong0524@gmail.com
 * @created 2021/08/29
 * @desc
 */

class LocationProvider(val context: Context) {

    //Location 클래스는 위도, 경도, 고도와 같이 위치에 관련된 정보를 가지고 있는 데이터 클래스입니다.
    private var location: Location? = null
    //Location Manager는 시스템 위치 서비스에 접근을 제공하는 클래스입니다.
    private var locationManager: LocationManager? = null

    init {
        //초기화 시에 위치를 가져옵니다.
        getLocation();
    }

    private fun getLocation(): Location? {
        try {
            //먼저 위치 시스템 서비스를 가져옵니다.
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


            var gpsLocation: Location? = null
            var networkLocation: Location? = null

            //GPS Provider 와 Network Provider 활성화 되어있는지 확인
            val isGPSEnabled: Boolean =
                locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled: Boolean =
                locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) {
                //GPS, Network Provider 둘 다 사용 불가능한 상황이면 null 을 반환합니다.
                return null
            } else {
                val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION // ACCESS_COARSE_LOCATION 보다 더 정밀한 위치 정보를 얻을 수 있습니다.
                )
                val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION // 도시 Block 단위의 정밀도의 위치 정보를 얻을 수 있습니다.
                )
                //만약 위 두 개 권한 없다면 null을 반환합니다.
                if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED
                ) return null

                //네트워크를 통한 위치 파악이 가능한 경우에 위치를 가져옵니다.
                if (isNetworkEnabled) {
                    networkLocation =
                        locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }

                //GPS를 통한 위치 파악이 가능한 경우에 위치를 가져옵니다.
                if (isGPSEnabled) {
                    gpsLocation =
                        locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }

                if (gpsLocation != null && networkLocation != null) {
                    //만약 두 개 위치가 있다면 정확도 높은 것으로 선택합니다.
                    if (gpsLocation.accuracy > networkLocation.accuracy) {
                        location = gpsLocation
                        return gpsLocation
                    } else {
                        location = networkLocation
                        return networkLocation
                    }
                } else {
                    //만약 가능한 위치 정보가 한 개만 있는 경우
                    if (gpsLocation != null) {
                        location = gpsLocation
                    }

                    if (networkLocation != null) {
                        location = networkLocation
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return location
    }

    //위도 정보를 가져오는 함수입니다.
    fun getLocationLatitude(): Double {
        return location?.latitude ?: 0.0
    }

    //경도 정보르 가져오는 함수입니다.
    fun getLocationLongitude(): Double {
        return location?.longitude ?: 0.0
    }

}