package com.piyushsatija.pollutionmonitor.view

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.piyushsatija.pollutionmonitor.*
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.RetrofitHelper.Companion.instance
import com.piyushsatija.pollutionmonitor.adapters.PollutantsAdapter
import com.piyushsatija.pollutionmonitor.model.*
import com.piyushsatija.pollutionmonitor.utils.GPSUtils
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener {
    //Views
    private var aqiTextView: TextView? = null
    private var temperatureTextView: TextView? = null
    private var locationTextView: TextView? = null
    private var pressureTextView: TextView? = null
    private var humidityTextView: TextView? = null
    private var windTextView: TextView? = null
    private var attributionTextView: TextView? = null
    private var pollutantsRecyclerView: RecyclerView? = null
    private var rateUsCard: ViewGroup? = null

    //Data
    private var aqiViewModel: AqiViewModel? = null
    private var data = Data()
    private var pollutantsAdapter: PollutantsAdapter? = null
    private val pollutantsList: MutableList<Pollutant> = ArrayList()
    private var sharedPrefUtils: SharedPrefUtils? = null

    //Location
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationManager: LocationManager? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var latestLocation: Location? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefUtils = SharedPrefUtils.getInstance(this)
        if (sharedPrefUtils!!.appInstallTime == 0L) sharedPrefUtils!!.appInstallTime = System.currentTimeMillis()
        if (sharedPrefUtils?.isDarkMode!!) setTheme(R.style.AppTheme_Dark) else setTheme(R.style.AppTheme_Light)
        setContentView(R.layout.activity_main)
        init()
        aqiViewModel = ViewModelProviders.of(this).get(AqiViewModel::class.java)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest!!.setInterval(20 * 1000.toLong())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        latestLocation = location
                        getAqiData(location.latitude.toString(), location.longitude.toString())
                    }
                }
            }
        }
        checkGPSAndRequestLocation()
        scheduleWidgetUpdater()
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("weather")
                    .addOnCompleteListener { task: Task<Void?>? -> Log.d("FCM", "Subscribed to \"weather\"") }
        } catch (e: Exception) {
            Log.e("FCM", "Unable to add FCM topic")
        }
    }

    private fun checkGPSAndRequestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission()
            } else {
                if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //Call for AQI data based on location is done in "locationCallback"
                    fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
                } else {
                    GPSUtils(this).turnGPSOn()
                }
            }
        }
    }

    private fun scheduleWidgetUpdater() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val periodicWorkRequest = PeriodicWorkRequest.Builder(DataUpdateWorker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        WorkManager.getInstance().enqueue(periodicWorkRequest)
    }

    private fun init() {
        aqiTextView = findViewById(R.id.aqi_text_view)
        temperatureTextView = findViewById(R.id.temperature_text_view)
        locationTextView = findViewById(R.id.location_text_view)
        pressureTextView = findViewById(R.id.pressure_text_view)
        humidityTextView = findViewById(R.id.humidity_text_view)
        windTextView = findViewById(R.id.wind_text_view)
        attributionTextView = findViewById(R.id.attribution_text_view)
        rateUsCard = findViewById(R.id.rateUsLayout)
        setupRecyclerView()
        setupClickListeners()
        setupRateCard()
    }

    private fun setupClickListeners() {
        findViewById<View>(R.id.scaleGood).setOnClickListener(this)
        findViewById<View>(R.id.scaleModerate).setOnClickListener(this)
        findViewById<View>(R.id.scaleUnhealthySensitive).setOnClickListener(this)
        findViewById<View>(R.id.scaleUnhealthy).setOnClickListener(this)
        findViewById<View>(R.id.scaleVeryUnhealthy).setOnClickListener(this)
        findViewById<View>(R.id.scaleHazardous).setOnClickListener(this)
        findViewById<View>(R.id.btnDarkMode).setOnClickListener(this)
        findViewById<View>(R.id.btnShare).setOnClickListener(this)
        findViewById<View>(R.id.rateYes).setOnClickListener(this)
        findViewById<View>(R.id.rateNo).setOnClickListener(this)
    }

    private fun setupRecyclerView() {
        pollutantsRecyclerView = findViewById(R.id.pollutants_recycler_view)
        pollutantsRecyclerView!!.setLayoutManager(LinearLayoutManager(this))
        pollutantsRecyclerView!!.setHasFixedSize(true)
        val dividerItemDecoration = DividerItemDecoration(pollutantsRecyclerView!!.getContext(),
                DividerItemDecoration.VERTICAL)
        pollutantsRecyclerView!!.addItemDecoration(dividerItemDecoration)
    }

    private fun addPollutantsToList(iaqi: Iaqi) {
        pollutantsList.clear()
        if (iaqi.co != null) pollutantsList.add(Pollutant("Carbon Monoxide - AQI", iaqi.co!!.v!!))
        if (iaqi.no2 != null) pollutantsList.add(Pollutant("Nitrous Dioxide - AQI", iaqi.no2!!.v!!))
        if (iaqi.o3 != null) pollutantsList.add(Pollutant("Ozone - AQI", iaqi.o3!!.v!!))
        if (iaqi.pm2_5 != null) pollutantsList.add(Pollutant("PM 2.5 - AQI", iaqi.pm2_5!!.v!!))
        if (iaqi.pm10 != null) pollutantsList.add(Pollutant("PM 10 - AQI", iaqi.pm10!!.v!!))
        if (iaqi.so2 != null) pollutantsList.add(Pollutant("Sulfur Dioxide - AQI", iaqi.so2!!.v!!))
        pollutantsAdapter = PollutantsAdapter(pollutantsList)
        pollutantsRecyclerView!!.adapter = pollutantsAdapter
    }

    private fun setAqiScaleGroup() {
        val aqi = data.aqi
        lateinit var aqiScaleText: TextView
        if (aqi != null) {
            aqiScaleText = if (aqi >= 0 && aqi <= 50) findViewById(R.id.scaleGood) else if (aqi >= 51 && aqi <= 100) findViewById(R.id.scaleModerate) else if (aqi >= 101 && aqi <= 150) findViewById(R.id.scaleUnhealthySensitive) else if (aqi >= 151 && aqi <= 200) findViewById(R.id.scaleUnhealthy) else if (aqi >= 201 && aqi <= 300) findViewById(R.id.scaleVeryUnhealthy) else if (aqi >= 301) findViewById(R.id.scaleHazardous) else findViewById(R.id.scaleGood)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            aqiScaleText.foreground = getDrawable(R.drawable.selected_aqi_foreground)
        }
    }

    private fun showDialog(s: String) {
        instance!!.showProgressDialog(this, s)
    }

    private fun dismissDialog() {
        instance!!.dismissProgressDialog()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle(R.string.alert_title_location_access)
                        .setMessage(R.string.alert_content_location_access)
                        .setPositiveButton("Ok") { dialogInterface: DialogInterface?, i: Int ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_LOCATION)
                        }
                        .setNegativeButton("Cancel") { dialogInterface: DialogInterface?, i: Int -> aqiData }
                        .create()
                        .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        checkGPSAndRequestLocation()
                    }
                } else {
                    aqiData
                }
            }
        }
    }

    private fun getAqiData(latitude: String, longitude: String) {
        val geo = "geo:$latitude;$longitude"
        aqiViewModel!!.status.observe(this@MainActivity, Observer { status: Status? ->
            if (status != null) {
                if (status === Status.FETCHING) {
                    showDialog("Getting data from nearest station...")
                } else dismissDialog()
            }
        })
        aqiViewModel!!.getGPSApiResponse(geo).observe(this@MainActivity, Observer { apiResponse: ApiResponse? ->
            if (apiResponse != null) {
                Log.d("api", apiResponse.toString())
                data = apiResponse.data!!
                aqiTextView!!.text = data.aqi.toString()
                //TODO: Find better implementation
                sharedPrefUtils!!.saveLatestAQI(data.aqi.toString())
                setAqiScaleGroup()
                val iaqi = data.iaqi
                if (iaqi?.temperature != null) temperatureTextView!!.text = getString(R.string.temperature_unit_celsius, data.iaqi?.temperature!!.v)
                if (iaqi?.pressure != null) pressureTextView!!.text = getString(R.string.pressure_unit, iaqi.pressure!!.v)
                if (iaqi?.humidity != null) humidityTextView!!.text = getString(R.string.humidity_unit, iaqi.humidity!!.v)
                if (iaqi?.wind != null) windTextView!!.text = getString(R.string.wind_unit, iaqi.wind!!.v)
                locationTextView!!.text = data.city?.name
                setupAttributions(data)
                addPollutantsToList(data.iaqi!!)
                pollutantsAdapter!!.notifyDataSetChanged()
                updateWidget()
            }
        })
    }

    //TODO: Find better implementation
    private val aqiData: Unit
        private get() {
            aqiViewModel!!.status.observe(this@MainActivity, Observer { status: Status? ->
                if (status != null) {
                    if (status === Status.FETCHING) {
                        showDialog("Getting data based on network...")
                    } else dismissDialog()
                }
            })
            aqiViewModel!!.apiResponse.observe(this@MainActivity, Observer { apiResponse: ApiResponse? ->
                if (apiResponse != null) {
                    Log.d("api", apiResponse.toString())
                    data = apiResponse.data!!
                    aqiTextView!!.text = data.aqi.toString()
                    //TODO: Find better implementation
                    sharedPrefUtils!!.saveLatestAQI(data.aqi.toString())
                    setAqiScaleGroup()
                    val iaqi = data.iaqi
                    if (iaqi?.temperature != null) temperatureTextView!!.text = getString(R.string.temperature_unit_celsius, data.iaqi?.temperature!!.v)
                    if (iaqi?.pressure != null) pressureTextView!!.text = getString(R.string.pressure_unit, iaqi.pressure!!.v)
                    if (iaqi?.humidity != null) humidityTextView!!.text = getString(R.string.humidity_unit, iaqi.humidity!!.v)
                    if (iaqi?.wind != null) windTextView!!.text = getString(R.string.wind_unit, iaqi.wind!!.v)
                    locationTextView!!.text = data.city?.name
                    setupAttributions(data)
                    addPollutantsToList(data.iaqi!!)
                    pollutantsAdapter!!.notifyDataSetChanged()
                    updateWidget()
                }
            })
        }

    private fun setupAttributions(data: Data) {
        var index = 1
        val attributionText = StringBuilder()
        for (attribution in data.attributions!!) {
            attributionText.append(index++)
                    .append(". ")
                    .append(attribution.name)
                    .append("\n")
                    .append(attribution.url)
                    .append("\n\n")
        }
        attributionTextView!!.text = attributionText
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GPSUtils.GPS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                checkGPSAndRequestLocation()
            } else {
                aqiData
            }
        }
    }

    private fun updateWidget() {
        val intent = Intent(this, AQIWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(application, AQIWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    private fun shareApp() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText))
        shareIntent.type = "text/plain"
        startActivity(shareIntent)
    }

    private fun setupRateCard() {
        if (!sharedPrefUtils!!.rateCardDone() && System.currentTimeMillis() - sharedPrefUtils!!.appInstallTime!! >= 86400000) {
            rateUsCard!!.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.scaleGood -> InfoDialog(this@MainActivity, PollutionLevels.GOOD).show()
            R.id.scaleModerate -> InfoDialog(this@MainActivity, PollutionLevels.MODERATE).show()
            R.id.scaleUnhealthySensitive -> InfoDialog(this@MainActivity, PollutionLevels.UNHEALTHY_FOR_SENSITIVE).show()
            R.id.scaleUnhealthy -> InfoDialog(this@MainActivity, PollutionLevels.UNHEALTHY).show()
            R.id.scaleVeryUnhealthy -> InfoDialog(this@MainActivity, PollutionLevels.VERY_UNHEALTHY).show()
            R.id.scaleHazardous -> InfoDialog(this@MainActivity, PollutionLevels.HAZARDOUS).show()
            R.id.btnDarkMode -> {
                sharedPrefUtils!!.isDarkMode(!sharedPrefUtils!!.isDarkMode)
                recreate()
            }
            R.id.btnShare -> shareApp()
            R.id.rateNo -> rateUsCard!!.animate().alpha(0.0f).translationX(200f).setDuration(500).withEndAction {
                rateUsCard!!.visibility = View.GONE
                sharedPrefUtils!!.rateCardDone(true)
            }
            R.id.rateYes -> {
                sharedPrefUtils!!.rateCardDone(true)
                rateUsCard!!.visibility = View.GONE
                val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.piyushsatija.pollutionmonitor")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            else -> {
            }
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}