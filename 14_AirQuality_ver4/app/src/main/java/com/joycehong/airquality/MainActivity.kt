package com.joycehong.airquality

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.joycehong.airquality.databinding.ActivityMainBinding
import com.joycehong.airquality.retrofit.AirQualityResponse
import com.joycehong.airquality.retrofit.AirQualityService
import com.joycehong.airquality.retrofit.RetrofitConnection
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    // 런타임 권한 요청시 필요한 요청 코드입니다.
    private val PERMISSIONS_REQUEST_CODE = 100

    // 요청할 권한 리스트 입니다.
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // 위치 서비스 요청시 필요한 런처입니다.
    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>

    // 위도와 경도를 가져올 때 필요합니다.
    lateinit var locationProvider: LocationProvider

    // 위도와 경도를 저장합니다.
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    val startMapActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode ?: Activity.RESULT_CANCELED == Activity.RESULT_OK) {
                    latitude = result?.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                    longitude = result?.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                    updateUI()
                }
            }
        })

    //전면 광고
    var mInterstitialAd : InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()
        updateUI()
        setRefreshButton()
        setFab()

        setBannerAds() // 추가
    }

    override fun onResume() {
        super.onResume()
        setInterstitialAds()
    }

    private fun setInterstitialAds(){
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-6454769716136341/8207564092", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("ads log", "전면 광고가 로드 실패했습니다. ${adError.responseInfo}")
                setTestInterstitialAds()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("ads log", "전면 광고가 로드되었습니다.")
                mInterstitialAd = interstitialAd
            }
        })
    }

    /**
     * 만약 실제 광고 단위가 동작하지 않을 시에는 테스트 전면 광고를 보여줌.
     * */
    private fun setTestInterstitialAds(){
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("ads log", "테스트 전면 광고가 로드 실패했습니다. ${adError.responseInfo}")
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("ads log", "테스트 전면 광고가 로드되었습니다.")
                mInterstitialAd = interstitialAd
            }
        })
    }




    private fun setBannerAds(){
        MobileAds.initialize(this);
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("ads log","배너 광고가 로드되었습니다.")
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                Log.d("ads log","배너 광고가 로드 실패했습니다. ${adError.responseInfo} ${adError.code}")
                setTestBannerAds()
            }

            override fun onAdOpened() {
                Log.d("ads log","배너 광고를 열었습니다.") //전면에 광고가 오버레이 되었을 때
            }

            override fun onAdClicked() {
                Log.d("ads log","배너 광고를 클릭했습니다.")
            }

            override fun onAdClosed() {
                Log.d("ads log", "배너 광고를 닫았습니다.")
            }
        }
    }

    /**
     * 만약 실제 광고 단위가 동작하지 않을 시에는 테스트 배너를 보여줌.
     * */
    private fun setTestBannerAds(){
        //기존 배너는 숨김 처리. 테스트 배너는 보임 처리
        binding.testAdView.visibility = View.VISIBLE
        binding.adView.visibility = View.GONE

        MobileAds.initialize(this);
        val adRequest = AdRequest.Builder().build()
        binding.testAdView.loadAd(adRequest)
        binding.testAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("ads log","테스트 배너 광고가 로드되었습니다.")
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                Log.d("ads log","테스트 배너 광고가 로드 실패했습니다. ${adError.responseInfo} ${adError.code}")
            }

            override fun onAdOpened() {
                Log.d("ads log","테스트 배너 광고를 열었습니다.") //전면에 광고가 오버레이 되었을 때
            }

            override fun onAdClicked() {
                Log.d("ads log","테스트 배너 광고를 클릭했습니다.")
            }

            override fun onAdClosed() {
                Log.d("ads log", "테스트 배너 광고를 닫았습니다.")
            }
        }
    }



    private fun setFab() {
        binding.fab.setOnClickListener {
            if(mInterstitialAd != null) {
                mInterstitialAd!!.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("ads log", "전면 광고가 닫혔습니다.")

                        // start of [기존 코드]
                        val intent = Intent(this@MainActivity, MapActivity::class.java) //this -> this@MainActivity 로 수정
                        intent.putExtra("currentLat", latitude)
                        intent.putExtra("currentLng", longitude)
                        startMapActivityResult.launch(intent)
                        // end of [기존 코드]
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                        Log.d("ads log", "전면 광고가 열리는 데 실패했습니다.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d("ads log", "전면 광고가 성공적으로 열렸습니다.")
                        // 전면 광고는 재사용이 어렵기 때문에 한 번 사용하고 나서 다시 null로 만들어주어야합니다.
                        mInterstitialAd = null
                    }
                }

                mInterstitialAd!!.show(this)
            }else{
                Log.d("InterstitialAd", "전면 광고가 로딩되지 않았습니다.")
                Toast.makeText(
                    this@MainActivity,
                    "잠시 후 다시 시도해주세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setRefreshButton() {
        binding.btnRefresh.setOnClickListener {
            updateUI()
        }
    }

    private fun updateUI() {
        locationProvider = LocationProvider(this@MainActivity)

        //위도와 경도 정보를 가져옵니다.
        if (latitude == 0.0 || longitude == 0.0) {
            latitude = locationProvider.getLocationLatitude()
            longitude = locationProvider.getLocationLongitude()
        }

        if (latitude != 0.0 || longitude != 0.0) {

            //1. 현재 위치를 가져오고 UI 업데이트
            //현재 위치를 가져오기
            val address = getCurrentAddress(latitude, longitude) //주소가 null 이 아닐 경우 UI 업데이트
            address?.let {
                binding.tvLocationTitle.text = "${it.thoroughfare}" // 예시: 역삼 1동
                binding.tvLocationSubtitle.text = "${it.countryName} ${it.adminArea}" // 예시 : 대한민국 서울특별시
            }

            //2. 현재 미세먼지 농도 가져오고 UI 업데이트
            getAirQualityData(latitude, longitude)

        } else {
            Toast.makeText(
                this@MainActivity, "위도, 경도 정보를 가져올 수 없었습니다. 새로고침을 눌러주세요.", Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * @desc 레트로핏 클래스를 이용하여 미세먼지 오염 정보를 가져옵니다.
     * */
    private fun getAirQualityData(latitude: Double, longitude: Double) { // 레트로핏 객체를 이용하면 AirQualityService 인터페이스 구현체를 가져올 수 있습니다.
        val retrofitAPI = RetrofitConnection.getInstance().create(AirQualityService::class.java)

        retrofitAPI.getAirQualityData(
            latitude.toString(), longitude.toString(), "f8f5a711-7da9-4118-a875-304ffded8cb8"
        ).enqueue(object : Callback<AirQualityResponse> {
            override fun onResponse(
                call: Call<AirQualityResponse>, response: Response<AirQualityResponse>
            ) { //정상적인 Response가 왔다면 UI 업데이트
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "최신 정보 업데이트 완료!", Toast.LENGTH_SHORT).show() //만약 response.body()가 null 이 아니라면 updateAirUI()
                    response.body()?.let { updateAirUI(it) }
                } else {
                    Toast.makeText(this@MainActivity, "업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    /**
     * @desc 가져온 데이터 정보를 바탕으로 화면을 업데이트한다.
     * */
    private fun updateAirUI(airQualityData: AirQualityResponse) {
        val pollutionData = airQualityData.data.current.pollution

        //수치 지정 (가운데 숫자)
        binding.tvCount.text = pollutionData.aqius.toString()

        //측정된 날짜 지정
        //"2021-09-04T14:00:00.000Z" 형식을  "2021-09-04 23:00"로 수정
        val dateTime = ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        binding.tvCheckTime.text = dateTime.format(dateFormatter).toString()

        when (pollutionData.aqius) {
            in 0..50 -> {
                binding.tvTitle.text = "좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }

            in 51..150 -> {
                binding.tvTitle.text = "보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }

            in 151..200 -> {
                binding.tvTitle.text = "나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }

            else -> {
                binding.tvTitle.text = "매우 나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }

    /**
     * @desc 위도와 경도를 기준으로 실제 주소를 가져온다.
     * */
    fun getCurrentAddress(latitude: Double, longitude: Double): Address? {
        val geocoder = Geocoder(this, Locale.getDefault()) // Address 객체는 주소와 관련된 여러 정보를 가지고 있습니다. android.location.Address 패키지 참고.
        val addresses: List<Address>?

        addresses = try { //Geocoder 객체를 이용하여 위도와 경도로부터 리스트를 가져옵니다.
            geocoder.getFromLocation(latitude, longitude, 7)
        } catch (ioException: IOException) {
            Toast.makeText(this, "지오코더 서비스 사용불가합니다.", Toast.LENGTH_LONG).show()
            return null
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 위도, 경도 입니다.", Toast.LENGTH_LONG).show()
            return null
        }

        //에러는 아니지만 주소가 발견되지 않은 경우
        if (addresses == null || addresses.size == 0) {
            Toast.makeText(this, "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
            return null
        }

        val address: Address = addresses[0]

        return address
    }

    private fun checkAllPermissions() {
        if (!isLocationServicesAvailable()) { //1. 위치 서비스(GPS)가 켜져있는지 확인합니다.
            showDialogForLocationServiceSetting();
        } else {  //2. 런타임 앱 권한이 모두 허용되어있는지 확인합니다.
            isRunTimePermissionsGranted();
        }
    }

    fun isLocationServicesAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun isRunTimePermissionsGranted() { // 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) { // 권한이 한 개라도 없다면 퍼미션 요청을 합니다.
            ActivityCompat.requestPermissions(
                this@MainActivity, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE
            )
        }
    }

    /**
     * @desc 런타임 권한을 요청하고 권한 요청에 따른 결과를 리턴한다.
     * */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            var checkResult = true

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }
            if (checkResult) { //위치 값을 가져올 수 있음
            } else { //퍼미션이 거부되었다면 앱을 종료합니다.
                Toast.makeText(
                    this@MainActivity, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    /**
     * @desc LocationManager를 사용하기 위해서 권한을 요청한다.
     * */
    private fun showDialogForLocationServiceSetting() {

        //먼저 ActivityResultLauncher를 설정해줍니다. 이 런처를 이용하여 결과 값을 리턴해야하는 인텐트를 실행할 수 있습니다.
        getGPSPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> //결과 값을 받았을 때 로직을 작성해줍니다.
            if (result.resultCode == Activity.RESULT_OK) { //사용자가 GPS 를 활성화 시켰는지 확인합니다.
                if (isLocationServicesAvailable()) {
                    isRunTimePermissionsGranted()
                } else { //위치 서비스가 허용되지 않았다면 앱을 종료합니다.
                    Toast.makeText(
                        this@MainActivity, "위치 서비스를 사용할 수 없습니다.", Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스가 꺼져있습니다. 설정해야 앱을 사용할 수 있습니다.")
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id ->
            dialog.cancel()
            Toast.makeText(
                this@MainActivity, "기기에서 위치서비스(GPS) 설정 후 사용해주세요.", Toast.LENGTH_SHORT
            ).show()
            finish()
        })
        builder.create().show()
    }
}