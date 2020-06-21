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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.location.*
import com.google.firebase.messaging.FirebaseMessaging
import com.piyushsatija.pollutionmonitor.*
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.api.RetrofitHelper.Companion.instance
import com.piyushsatija.pollutionmonitor.adapters.PollutantsAdapter
import com.piyushsatija.pollutionmonitor.api.ApiResponse
import com.piyushsatija.pollutionmonitor.model.*
import com.piyushsatija.pollutionmonitor.utils.GPSUtils
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val logTag = javaClass.simpleName

    //Data
    private lateinit var aqiViewModel: AqiViewModel
    private var data: Data? = Data()
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
        try {
            sharedPrefUtils = SharedPrefUtils.getInstance(this)
            if (sharedPrefUtils?.appInstallTime == 0L) sharedPrefUtils?.appInstallTime = System.currentTimeMillis()
            if (sharedPrefUtils?.isDarkMode == true) setTheme(R.style.AppTheme_Dark) else setTheme(R.style.AppTheme_Light)

            setContentView(R.layout.activity_main)

            aqiViewModel = ViewModelProvider(this).get(AqiViewModel::class.java)

            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(20 * 1000.toLong())
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
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
            FirebaseMessaging.getInstance().subscribeToTopic("weather")
                    .addOnCompleteListener { Log.d("FCM", "Subscribed to \"weather\"") }
            init()
        } catch (e: Exception) {
            Log.e(logTag, e.toString())
        }

    }

    private fun checkGPSAndRequestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission()
            } else {
                if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    //Call for AQI data based on location is done in "locationCallback"
                    fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
                } else {
                    GPSUtils(this).turnGPSOn()
                }
            }
        }
    }

    private fun scheduleWidgetUpdater() {
        try {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            val periodicWorkRequest = PeriodicWorkRequest.Builder(DataUpdateWorker::class.java, 15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
            WorkManager.getInstance().enqueue(periodicWorkRequest)
        } catch (e: Exception) {
            Log.e(logTag, e.toString())
        }
    }

    private fun init() {
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
        pollutantsRecyclerView.layoutManager = LinearLayoutManager(this)
        pollutantsRecyclerView.setHasFixedSize(true)
        val dividerItemDecoration = DividerItemDecoration(pollutantsRecyclerView.context,
                DividerItemDecoration.VERTICAL)
        pollutantsRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun addPollutantsToList(iaqi: Iaqi) {
        pollutantsList.clear()
        iaqi.co?.apply { pollutantsList.add(Pollutant("Carbon Monoxide - AQI", iaqi.co?.v ?: 0.0)) }
        iaqi.no2?.apply {
            pollutantsList.add(Pollutant("Nitrous Dioxide - AQI", iaqi.no2?.v ?: 0.0))
        }
        iaqi.o3?.apply { pollutantsList.add(Pollutant("Ozone - AQI", iaqi.o3?.v ?: 0.0)) }
        iaqi.pm2_5?.apply { pollutantsList.add(Pollutant("PM 2.5 - AQI", iaqi.pm2_5?.v ?: 0.0)) }
        iaqi.pm10?.apply { pollutantsList.add(Pollutant("PM 10 - AQI", iaqi.pm10?.v ?: 0.0)) }
        iaqi.so2?.apply {
            pollutantsList.add(Pollutant("Sulfur Dioxide - AQI", iaqi.so2?.v ?: 0.0))
        }
        pollutantsAdapter = PollutantsAdapter(pollutantsList)
        pollutantsRecyclerView?.adapter = pollutantsAdapter
    }

    private fun setAqiScaleGroup() {
        var aqiScaleText: TextView? = null
        data?.aqi?.apply {
            aqiScaleText = when (this) {
                in 0..50 -> findViewById(R.id.scaleGood)
                in 51..100 -> findViewById(R.id.scaleModerate)
                in 101..150 -> findViewById(R.id.scaleUnhealthySensitive)
                in 151..200 -> findViewById(R.id.scaleUnhealthy)
                in 201..300 -> findViewById(R.id.scaleVeryUnhealthy)
                else -> findViewById(R.id.scaleHazardous)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            aqiScaleText?.foreground = getDrawable(R.drawable.selected_aqi_foreground)
        }
    }

    private fun showDialog(s: String) {
        instance?.showProgressDialog(this, s)
    }

    private fun dismissDialog() {
        instance?.dismissProgressDialog()
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
                if (grantResults.isNotEmpty()
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
        aqiViewModel.status.observe(this@MainActivity, Observer { status: Status? ->
            if (status != null) {
                if (status === Status.FETCHING) {
                    showDialog("Getting data from nearest station...")
                } else dismissDialog()
            }
        })
        aqiViewModel.getGPSApiResponse(geo).observe(this@MainActivity, Observer { apiResponse: ApiResponse? ->
            if (apiResponse != null) {
                Log.d("api", apiResponse.toString())
                data = apiResponse.data
                aqiTextView?.text = data?.aqi?.toString()
                //TODO: Find better implementation
                sharedPrefUtils?.saveLatestAQI(data?.aqi?.toString())
                setAqiScaleGroup()

                data?.iaqi?.apply {
                    temperatureTextView?.text = getString(R.string.temperature_unit_celsius, this.temperature?.v)
                    pressureTextView?.text = getString(R.string.pressure_unit, this.pressure?.v)
                    humidityTextView?.text = getString(R.string.humidity_unit, this.humidity?.v)
                    windTextView?.text = getString(R.string.wind_unit, this.wind?.v)
                    locationTextView?.text = data?.city?.name
                    setupAttributions(data)
                    addPollutantsToList(this)
                    pollutantsAdapter?.notifyDataSetChanged()
                    updateWidget()
                }
            }
        })
    }

    //TODO: Find better implementation
    private val aqiData: Unit
        get() {
            if (::aqiViewModel.isInitialized) {
                aqiViewModel.status.observe(this@MainActivity, Observer { status: Status? ->
                    if (status != null) {
                        if (status === Status.FETCHING) {
                            showDialog("Getting data based on network...")
                        } else dismissDialog()
                    }
                })
                aqiViewModel.apiResponse.observe(this@MainActivity, Observer { apiResponse: ApiResponse? ->
                    if (apiResponse != null) {
                        Log.d("api", apiResponse.toString())
                        data = apiResponse.data
                        aqiTextView?.text = data?.aqi?.toString()
                        //TODO: Find better implementation
                        sharedPrefUtils?.saveLatestAQI(data?.aqi?.toString())
                        setAqiScaleGroup()
                        data?.iaqi?.apply {
                            temperatureTextView?.text = getString(R.string.temperature_unit_celsius, this.temperature?.v)
                            pressureTextView?.text = getString(R.string.pressure_unit, this.pressure?.v)
                            humidityTextView?.text = getString(R.string.humidity_unit, this.humidity?.v)
                            windTextView?.text = getString(R.string.wind_unit, this.wind?.v)
                            locationTextView?.text = data?.city?.name
                            setupAttributions(data)
                            addPollutantsToList(this)
                            pollutantsAdapter?.notifyDataSetChanged()
                            updateWidget()
                        }
                    }
                })
            }
        }

    private fun setupAttributions(data: Data?) {
        data?.attributions?.apply {
            var index = 1
            val attributionText = StringBuilder()
            for (attribution in this) {
                attributionText.append(index++)
                        .append(". ")
                        .append(attribution.name)
                        .append("\n")
                        .append(attribution.url)
                        .append("\n\n")
            }
            attributionTextView?.text = attributionText
        }
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
            rateUsCard.visibility = View.VISIBLE
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
                sharedPrefUtils?.isDarkMode(!sharedPrefUtils!!.isDarkMode)
                recreate()
            }
            R.id.btnShare -> shareApp()
            R.id.rateNo -> rateUsCard.animate().alpha(0.0f).translationX(200f).setDuration(500).withEndAction {
                rateUsCard.visibility = View.GONE
                sharedPrefUtils?.rateCardDone(true)
            }
            R.id.rateYes -> {
                sharedPrefUtils?.rateCardDone(true)
                rateUsCard.visibility = View.GONE
                val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.piyushsatija.pollutionmonitor")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}