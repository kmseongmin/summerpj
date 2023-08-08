package com.example.summerpj

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import noman.googleplaces.NRPlaces
import noman.googleplaces.Place
import noman.googleplaces.PlaceType
import noman.googleplaces.PlacesException
import noman.googleplaces.PlacesListener
import java.io.IOException
import java.util.Locale
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date

class MainroomActivity : AppCompatActivity(), OnMapReadyCallback,PlacesListener,
    OnRequestPermissionsResultCallback,
    OnMarkerClickListener, GoogleMap.OnMapClickListener {
    lateinit var toolbar:Toolbar
    lateinit var main_drawer_layout:DrawerLayout
    private lateinit var mapView: MapView
    lateinit var main_nav:NavigationView
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var locationPermissionGranted = false
    var mycurrentlocation:LatLng? = null
    private var googleMap: GoogleMap? = null
    var previous_marker: MutableList<Marker>? = null
    lateinit var b2:Button
    lateinit var item: Marker
    private lateinit var headername:TextView
    private lateinit var headeremail:TextView

    // 선택된 마커 정보를 저장할 변수들
    var selectedMarkerTitle: String = ""
    var selectedMarkerAddress: String = ""
    var selectedMarkerDate: String = ""
    //선택된 마커 변수
    var selectedMarker: Marker? = null


    @SuppressLint("ResourceType", "SimpleDateFormat")
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
        previous_marker = ArrayList()

        val b1 = findViewById<Button>(R.id.b1)
        b1.setBackgroundColor(R.drawable.register_button)
        b1.setOnClickListener {
            mycurrentlocation?.let { it1 -> showPlaceInformation(it1) }
        }

        b2 = findViewById(R.id.b2)
        b2.setBackgroundColor(R.drawable.white_blue)
        b2.setOnClickListener {
            selectedMarker?.let {
                //선택된 마커 정보저장
                selectedMarkerTitle = it.title.toString()//장소이름 저장
                selectedMarkerAddress = it.snippet.toString()//주소 저장
                selectedMarkerDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()) // 현재 날짜 저장
                selectedMarker = null

                addUserDataToFirestore(selectedMarkerTitle, selectedMarkerAddress, selectedMarkerDate)
            }
            b2.visibility = View.INVISIBLE
        }

        //헤더 이름, 이메일
        val headerView = main_nav.getHeaderView(0)
        headername = headerView.findViewById(R.id.hearder_name)
        headeremail = headerView.findViewById(R.id.hearder_email)

    }

    private fun addUserDataToFirestore(selectedMarkerTitle: String, selectedMarkerAddress: String, selectedMarkerDate: String) {
        val db = Firebase.firestore
        val recode = hashMapOf(
            "title" to selectedMarkerTitle,
            "address" to selectedMarkerAddress,
            "data" to selectedMarkerDate
        )

        db.collection("recode")
            .add(recode)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                // 데이터 추가 성공
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
                // 데이터 추가 실패
            }
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
        googleMap = map
        //마커클릭리스너
        googleMap!!.setOnMarkerClickListener(this)
        //구글맵 클릭 리스너
        googleMap!!.setOnMapClickListener(this)
        if (locationPermissionGranted) {
            // 위치 권한이 허용된 경우에만 내 위치를 표시하도록 호출
            enableMyLocation()
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

    override fun onPlacesFailure(e: PlacesException?) {
    }

    override fun onPlacesStart() {
    }

    override fun onPlacesSuccess(places: List<Place>) {
        runOnUiThread {
            for (place in places) {
                val latLng = LatLng(
                    place.latitude, place.longitude
                )
                val markerDrawable = resources.getDrawable(R.drawable.baseline_local_parking_24)
                val scaleFactor = 1.4f // 크기 조절을 위한 값
                val newWidth = (markerDrawable.intrinsicWidth * scaleFactor).toInt()
                val newHeight = (markerDrawable.intrinsicHeight * scaleFactor).toInt()

                markerDrawable.setBounds(0, 0, newWidth, newHeight)
                val markerBitmap = Bitmap.createBitmap(
                    newWidth,
                    newHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(markerBitmap)
                markerDrawable.draw(canvas)

                val markerSnippet: String = getCurrentAddress(latLng)
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title(place.name)
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                markerOptions.snippet(markerSnippet)

                item = googleMap!!.addMarker(markerOptions)!!
                previous_marker!!.add(item)

            }

            //중복 마커 제거
            val hashSet = HashSet<Marker>()
            hashSet.addAll(previous_marker!!)
            previous_marker!!.clear()
            previous_marker!!.addAll(hashSet)
        }
    }

    override fun onPlacesFinished() {
    }

    fun showPlaceInformation(location: LatLng) {
        getCurrentLocation()
        googleMap!!.clear() //지도 클리어
        if (previous_marker != null) previous_marker!!.clear()
        NRPlaces.Builder()
            .listener(this@MainroomActivity)
            .key("AIzaSyADvY2EaKlHIOfU7clZGQuDDSiCl8G6bWg")
            .latlng(location.latitude, location.longitude) //현재 위치
            .radius(1000) //1키로 내 검색
            .type(PlaceType.PARKING) //주차장검색
            .build()
            .execute()
    }

    fun getCurrentAddress(latlng: LatLng): String {

        //지오코더... GPS를 주소로 변환
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1
            )
        } catch (ioException: IOException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            return "지오코더 서비스 사용불가"
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
            return "잘못된 GPS 좌표"
        }
        if (addresses == null || addresses.size == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show()
            return "주소 미발견"
        } else {
            val address = addresses[0]
            return address.getAddressLine(0).toString()
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        b2.visibility = View.VISIBLE
        selectedMarker = marker

        return false
    }

    override fun onMapClick(latLng: LatLng) {
        // 구글 지도 클릭 시 이용하기 버튼 숨기기
        b2.visibility = View.INVISIBLE

    }

}