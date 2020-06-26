package com.piyushsatija.pollutionmonitor.view.fragment

import android.Manifest
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.location.*
import com.piyushsatija.pollutionmonitor.AQIWidget
import com.piyushsatija.pollutionmonitor.AqiViewModel
import com.piyushsatija.pollutionmonitor.DataUpdateWorker
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.adapters.PollutantsAdapter
import com.piyushsatija.pollutionmonitor.api.ApiResponse
import com.piyushsatija.pollutionmonitor.api.RetrofitHelper
import com.piyushsatija.pollutionmonitor.model.*
import com.piyushsatija.pollutionmonitor.utils.GPSUtils
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils
import com.piyushsatija.pollutionmonitor.view.InfoDialog
import kotlinx.android.synthetic.main.aqi_color_scale.*
import kotlinx.android.synthetic.main.fragment_aqi.*
import kotlinx.android.synthetic.main.layout_rate_us.*
import java.util.*
import java.util.concurrent.TimeUnit


class AQIFragment : Fragment(), View.OnClickListener {
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

        sharedPrefUtils = SharedPrefUtils.getInstance(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R
                .layout.fragment_aqi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupRecyclerView()
        setupRateCard()
        aqiViewModel = ViewModelProvider(this).get(AqiViewModel::class.java)

        locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)

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
    }

    private fun checkGPSAndRequestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context!!.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context!!.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission()
            } else {
                if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //Call for AQI data based on location is done in "locationCallback"
                    fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
                } else {
//                    context?.run {
                    GPSUtils(context!!).turnGPSOn()
//                    }
                }
            }
        }
    }

    private fun checkLocationPermission() {
        context?.apply {
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    AlertDialog.Builder(this)
                            .setTitle(R.string.alert_title_location_access)
                            .setMessage(R.string.alert_content_location_access)
                            .setPositiveButton("Ok") { dialogInterface: DialogInterface?, i: Int ->
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                        GPSUtils.MY_PERMISSIONS_REQUEST_LOCATION)
                            }
                            .setNegativeButton("Cancel") { dialogInterface: DialogInterface?, i: Int -> aqiData }
                            .create()
                            .show()
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            GPSUtils.MY_PERMISSIONS_REQUEST_LOCATION)
                }
            }
        }
    }

    private fun getAqiData(latitude: String, longitude: String) {
        val geo = "geo:$latitude;$longitude"
        aqiViewModel.status.observe(activity!!, Observer { status: Status? ->
            if (status != null) {
                if (status === Status.FETCHING) {
                    showDialog("Getting data from nearest station...")
                } else dismissDialog()
            }
        })
        aqiViewModel.getGPSApiResponse(geo).observe(activity!!, Observer { apiResponse: ApiResponse? ->
            if (apiResponse != null) {
                Log.d("api", apiResponse.toString())
                data = apiResponse.data
                aqiTextView?.text = data?.aqi?.toString()
                //TODO: Find better implementation
                sharedPrefUtils?.saveLatestAQI(data?.aqi?.toString())
                setAqiScaleGroup()

                data?.iaqi?.apply {
                    temperatureTextView?.text = getString(R.string.temperature_unit_celsius, this.temperature?.v)
                    airPropertiesLayout.findViewById<TextView>(R.id.pressureTextView)?.text = getString(R.string.pressure_unit, this.pressure?.v)
                    airPropertiesLayout.findViewById<TextView>(R.id.humidityTextView)?.text = getString(R.string.humidity_unit, this.humidity?.v)
                    airPropertiesLayout.findViewById<TextView>(R.id.windTextView)?.text = getString(R.string.wind_unit, this.wind?.v)
                    locationTextView?.text = data?.city?.name
                    setupAttributions(data)
                    addPollutantsToList(this)
                    pollutantsAdapter?.notifyDataSetChanged()
                    updateWidget()
                }
            }
        })
    }

    private val aqiData: Unit
        get() {
            if (::aqiViewModel.isInitialized) {
                aqiViewModel.status.observe(activity!!, Observer { status: Status? ->
                    if (status != null) {
                        if (status === Status.FETCHING) {
                            showDialog("Getting data based on network...")
                        } else dismissDialog()
                    }
                })
                aqiViewModel.apiResponse.observe(activity!!, Observer { apiResponse: ApiResponse? ->
                    if (apiResponse != null) {
                        Log.d("api", apiResponse.toString())
                        data = apiResponse.data
                        aqiTextView?.text = data?.aqi?.toString()
                        //TODO: Find better implementation
                        sharedPrefUtils?.saveLatestAQI(data?.aqi?.toString())
                        setAqiScaleGroup()
                        data?.iaqi?.apply {
                            temperatureTextView?.text = getString(R.string.temperature_unit_celsius, this.temperature?.v)
                            airPropertiesLayout.findViewById<TextView>(R.id.pressureTextView)?.text = getString(R.string.pressure_unit, this.pressure?.v)
                            airPropertiesLayout.findViewById<TextView>(R.id.humidityTextView)?.text = getString(R.string.humidity_unit, this.humidity?.v)
                            airPropertiesLayout.findViewById<TextView>(R.id.windTextView)?.text = getString(R.string.wind_unit, this.wind?.v)
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

    private fun setupRecyclerView() {
        pollutantsRecyclerView.layoutManager = LinearLayoutManager(context)
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

    private fun updateWidget() {
        val intent = Intent(context, AQIWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        val ids = AppWidgetManager.getInstance(context?.applicationContext).getAppWidgetIds(ComponentName(context?.applicationContext!!, AQIWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context?.sendBroadcast(intent)
    }

    private fun setAqiScaleGroup() {
        var aqiScaleText: TextView? = null
        data?.aqi?.apply {
            aqiScaleText = when (this) {
                in 0..50 -> scaleGood
                in 51..100 -> scaleModerate
                in 101..150 -> scaleUnhealthySensitive
                in 151..200 -> scaleUnhealthy
                in 201..300 -> scaleVeryUnhealthy
                else -> scaleHazardous
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            aqiScaleText?.foreground = ContextCompat.getDrawable(context!!, R.drawable.selected_aqi_foreground)
        }
    }

    private fun showDialog(s: String) {
        RetrofitHelper.instance?.showProgressDialog(context, s)
    }

    private fun dismissDialog() {
        RetrofitHelper.instance?.dismissProgressDialog()
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

    private fun setupClickListeners() {
        scaleGood.setOnClickListener(this)
        scaleModerate.setOnClickListener(this)
        scaleUnhealthySensitive.setOnClickListener(this)
        scaleUnhealthy.setOnClickListener(this)
        scaleVeryUnhealthy.setOnClickListener(this)
        scaleHazardous.setOnClickListener(this)
        rateYes.setOnClickListener(this)
        rateNo.setOnClickListener(this)
    }

    public fun locationPermissionResult(success: Boolean) {
        if (success) checkGPSAndRequestLocation()
        else aqiData
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.scaleGood -> InfoDialog(activity!!, PollutionLevels.GOOD).show()
            R.id.scaleModerate -> InfoDialog(activity!!, PollutionLevels.MODERATE).show()
            R.id.scaleUnhealthySensitive -> InfoDialog(activity!!, PollutionLevels.UNHEALTHY_FOR_SENSITIVE).show()
            R.id.scaleUnhealthy -> InfoDialog(activity!!, PollutionLevels.UNHEALTHY).show()
            R.id.scaleVeryUnhealthy -> InfoDialog(activity!!, PollutionLevels.VERY_UNHEALTHY).show()
            R.id.scaleHazardous -> InfoDialog(activity!!, PollutionLevels.HAZARDOUS).show()
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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                AQIFragment().apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}