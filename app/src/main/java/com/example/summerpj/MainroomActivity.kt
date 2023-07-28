package com.example.summerpj

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback


class MainroomActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var toolbar:Toolbar
    lateinit var main_drawer_layout:DrawerLayout
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainroom)

        //메인 drawer
        main_drawer_layout=findViewById(R.id.main_drawer_layout)

        //툴바 설정
        toolbar = findViewById(R.id.main_layout_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_menu_24) // 홈버튼 이미지 변경


        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        // 이제 구글 지도 객체를 사용하여 지도를 커스터마이징하고 작업할 수 있습니다.
        // 예를 들어 마커 추가, 맵 타입 설정, 위치 활성화 등을 할 수 있습니다.
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