package com.example.summerpj

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient


class MainroomActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var toolbar:Toolbar
    lateinit var main_drawer_layout:DrawerLayout
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    lateinit var main_nav:NavigationView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var locationPermissionGranted = false
    var mycurrentlocation:LatLng? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainroom)

        //메인 drawer
        main_drawer_layout=findViewById(R.id.main_drawer_layout)

        //메인 nav
        main_nav = findViewById(R.id.main_navigationView)

        //툴바 설정
        toolbar = findViewById(R.id.main_layout_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_menu_24) // 홈버튼 이미지 변경

        //place api 추가
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyB57FGSwBEESpdkLu9lM9H_kOt7DMyAfAY")
        }

        //네비게이션 아이템 SelectedListener
        main_nav.setNavigationItemSelectedListener {menuItem->
            when (menuItem.itemId){
                R.id.my_account->{
                    val myinfo_intent = Intent(this, myinfo::class.java)
                    startActivity(myinfo_intent)
                    true
                }
                R.id.log->{
                    val driving_recode_intent = Intent(this, driving_recode::class.java)
                    startActivity(driving_recode_intent)
                    true
                }
                //로그아웃 기능
                R.id.logout->{
                    Logout()
                    true
                }
                else -> {false}
            }
        }

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            locationPermissionGranted = true
            initializeMap()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 사용자가 위치 권한을 허용한 경우
                locationPermissionGranted = true
                initializeMap()
            } else {
                // 사용자가 위치 권한을 거부한 경우
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeMap() {
        mapView.onCreate(Bundle())
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        if (locationPermissionGranted) {
            // 위치 권한이 허용된 경우에만 내 위치를 표시하도록 호출
            enableMyLocation()
            //searchNearbyParkingLots()
        }
    }

    private fun Logout(){
        val preferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean("Autologin", false)
        editor.apply()

        // 로그아웃 후 로그인 화면으로 이동하도록 코드를 추가할 수 있습니다.
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        // 현재 액티비티를 종료하여 사용자가 로그인 화면에서 뒤로 가기를 눌러도 다시 로그인 화면으로 돌아오지 않도록 할 수 있습니다.
        finish()
    }

    // 주차장 찾기 함수
    /*@SuppressLint("MissingPermission")
    private fun searchNearbyParkingLots() {
        if (mycurrentlocation == null) {
            // 현재 위치를 알 수 없는 경우 처리
            Toast.makeText(this, "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val placesClient = Places.createClient(this)

        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FindCurrentPlaceRequest.builder(placeFields).build()

        placesClient.findCurrentPlace(request).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val response = task.result
                response?.let {
                    val nearByParkingLots = it.placeLikelihoods.filter { placeLikelihood ->
                        placeLikelihood.place.types?.contains(Place.Type.PARKING) == true
                    }

                    for (placeLikelihood in nearByParkingLots) {
                        val place = placeLikelihood.place
                        val latLng = place.latLng
                        if (latLng != null) {
                            val markerOptions = MarkerOptions()
                                .position(latLng)
                                .title(place.name)

                            googleMap?.addMarker(markerOptions)
                        }
                    }
                }
            } else {
                // 오류 처리
                Toast.makeText(this, "주변 주차장을 검색하는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    private fun enableMyLocation() {
        if (googleMap != null) {
            try {
                googleMap?.isMyLocationEnabled = true
                getCurrentLocation()
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    mycurrentlocation = LatLng(location.latitude, location.longitude)
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(mycurrentlocation!!, 15f))
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    //네비게이션 드로어 홈버튼으로 열기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{ // 메뉴 버튼

                main_drawer_layout.openDrawer(GravityCompat.START)    // 네비게이션 드로어 열기
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

}