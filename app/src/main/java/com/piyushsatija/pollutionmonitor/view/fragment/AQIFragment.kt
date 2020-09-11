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
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.location.*
import com.piyushsatija.pollutionmonitor.*
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.api.ApiResponse
import com.piyushsatija.pollutionmonitor.api.RetrofitHelper
import com.piyushsatija.pollutionmonitor.model.*
import com.piyushsatija.pollutionmonitor.utils.AppUtils
import com.piyushsatija.pollutionmonitor.utils.Constants
import com.piyushsatija.pollutionmonitor.utils.GPSUtils
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils
import com.piyushsatija.pollutionmonitor.view.InfoDialog
import com.piyushsatija.pollutionmonitor.view.MainActivity
import kotlinx.android.synthetic.main.fragment_aqi.*
import kotlinx.android.synthetic.main.layout_rate_us.*
import java.util.*
import java.util.concurrent.TimeUnit


class AQIFragment : Fragment(), View.OnClickListener {
    private val logTag = javaClass.simpleName
    private lateinit var mainActivity: MainActivity

    //Data
    private lateinit var aqiViewModel: AqiViewModel
    private var data: Data? = Data()
    private val pollutantsList: MutableList<Pollutant> = ArrayList()
    private var sharedPrefUtils: SharedPrefUtils? = null
    private var temperature: Double? = null
    private var windSpeed: Double? = null

    //Location
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationManager: LocationManager? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var latestLocation: Location? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefUtils = SharedPrefUtils.getInstance(requireContext())
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_aqi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupRateCard()
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
                    GPSUtils(context!!).turnGPSOn()
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
        if (::aqiViewModel.isInitialized && isAdded) {
            activity?.apply {
                aqiViewModel.status.observe(this, Observer { status: Status? ->
                    if (status != null) {
                        if (status === Status.FETCHING) {
                            showDialog("Getting data from nearest station...")
                        } else dismissDialog()
                    }
                })
                aqiViewModel.getGPSApiResponse(geo).observe(this, Observer { apiResponse: ApiResponse? ->
                    if (apiResponse != null) {
                        Log.d("api", apiResponse.toString())
                        data = apiResponse.data
                        aqiCircle.findViewById<TextView>(R.id.aqiTextView)?.text = data?.aqi?.toString()
                        sharedPrefUtils?.saveLatestAQI(data?.aqi?.toString())
                        setAqiScaleGroup()

                        data?.iaqi?.apply {
                            aqiCircle.findViewById<TextView>(R.id.temperatureTextView)?.text = getTemperatureValue(this.temperature?.v)
                            airPropertiesLayout.findViewById<TextView>(R.id.pressureTextView)?.text = getString(R.string.pressure_unit, this.pressure?.v)
                            airPropertiesLayout.findViewById<TextView>(R.id.humidityTextView)?.text = getString(R.string.humidity_unit, this.humidity?.v)
                            airPropertiesLayout.findViewById<TextView>(R.id.windTextView)?.text = getWindSpeedValue(this.wind?.v)
                            locationTextView?.text = data?.city?.name
                            setupAttributions(data)
                            addPollutantsToList(this)
                            updateWidget()
                        }
                    }
                })
            }
        }
    }

    private val aqiData: Unit
        get() {
            if (::aqiViewModel.isInitialized && isAdded) {
                activity?.apply {
                    aqiViewModel.status.observe(this, Observer { status: Status? ->
                        if (status != null) {
                            if (status === Status.FETCHING) {
                                showDialog("Getting data based on network...")
                            } else dismissDialog()
                        }
                    })
                    aqiViewModel.apiResponse.observe(this, Observer { apiResponse: ApiResponse? ->
                        if (apiResponse != null) {
                            Log.d("api", apiResponse.toString())
                            data = apiResponse.data
                            aqiCircle.findViewById<TextView>(R.id.aqiTextView)?.text = data?.aqi?.toString()
                            sharedPrefUtils?.saveLatestAQI(data?.aqi?.toString())
                            setAqiScaleGroup()
                            data?.iaqi?.apply {
                                aqiCircle.findViewById<TextView>(R.id.temperatureTextView)?.text = getTemperatureValue(this.temperature?.v)
                                airPropertiesLayout.findViewById<TextView>(R.id.pressureTextView)?.text = getString(R.string.pressure_unit, this.pressure?.v)
                                airPropertiesLayout.findViewById<TextView>(R.id.humidityTextView)?.text = getString(R.string.humidity_unit, this.humidity?.v)
                                airPropertiesLayout.findViewById<TextView>(R.id.windTextView)?.text = getWindSpeedValue(this.wind?.v)
                                locationTextView?.text = data?.city?.name
                                setupAttributions(data)
                                addPollutantsToList(this)
                                updateWidget()
                            }
                        }
                    })
                }
            }
        }

    private fun getTemperatureValue(temperature: Double?): String {
        if (temperature == null) return getString(R.string.place_holder)
        this.temperature = temperature
        val unit = sharedPrefUtils!!.getStringValue(Constants.TEMPERATURE_UNIT, Constants.TEMPERATURE_CELSIUS)
        return if (unit == Constants.TEMPERATURE_CELSIUS) getString(R.string.temperature_unit_celsius, temperature)
        else getString(R.string.temperature_unit_fahrenheit, AppUtils.convertToFahrenheit(temperature))
    }

    private fun getWindSpeedValue(windSpeed: Double?): String {
        if (windSpeed == null) return getString(R.string.place_holder)
        this.windSpeed = windSpeed
        val unit = sharedPrefUtils!!.getStringValue(Constants.WINDSPEED_UNIT, Constants.WINDSPEED_MPS)
        return if (unit == Constants.WINDSPEED_MPS) getString(R.string.wind_unit_mps, windSpeed)
        else getString(R.string.wind_unit_kmph, AppUtils.convertToKmph(windSpeed))
    }

    private fun addPollutantsToList(iaqi: Iaqi) {
        pollutantsList.clear()
        iaqi.co?.apply { pollutantsList.add(Pollutant("Carbon Monoxide", iaqi.co?.v ?: 0.0)) }
        iaqi.no2?.apply {
            pollutantsList.add(Pollutant("Nitrous Dioxide", iaqi.no2?.v ?: 0.0))
        }
        iaqi.o3?.apply { pollutantsList.add(Pollutant("Ozone", iaqi.o3?.v ?: 0.0)) }
        iaqi.pm2_5?.apply { pollutantsList.add(Pollutant("PM 2.5", iaqi.pm2_5?.v ?: 0.0)) }
        iaqi.pm10?.apply { pollutantsList.add(Pollutant("PM 10", iaqi.pm10?.v ?: 0.0)) }
        iaqi.so2?.apply {
            pollutantsList.add(Pollutant("Sulfur Dioxide", iaqi.so2?.v ?: 0.0))
        }

        val pollutantsContainer = pollutantCard.findViewById<LinearLayout>(R.id.pollutantsContainer)
        pollutantsContainer.removeAllViews()
        pollutantsList.forEach {
            val pollutantRow = layoutInflater.inflate(R.layout.item_pollutant, pollutantsContainer, false)
            pollutantRow.findViewById<TextView>(R.id.pollutantName).text = it.pollutantName
            pollutantRow.findViewById<TextView>(R.id.pollutantValue).text = it.pollutantValue.toString()
            pollutantRow.findViewById<ProgressBar>(R.id.pollutantProgressBar).progress = it.pollutantValue.toInt()
            pollutantsContainer.addView(pollutantRow)
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
            attributionCard.findViewById<TextView>(R.id.attributionTextView)?.text = attributionText
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
        data?.aqi?.apply {
            val data = AppUtils.getAQIInfo(this, context!!)
            aqiInfoCard.findViewById<TextView>(R.id.aqiInfoTitle).text = data.getString("title")
            aqiInfoCard.findViewById<TextView>(R.id.aqiInfoTitle).setTextColor(AppUtils.getAQIColor(this, context!!))
            aqiInfoCard.findViewById<TextView>(R.id.aqiInfoDesc).text = data.getString("desc")
            aqiInfoCard.findViewById<AppCompatImageView>(R.id.aqiInfoIcon)
                    .setColorFilter(AppUtils.getAQIColor(this, context!!), android.graphics.PorterDuff.Mode.SRC_IN)
            aqiInfoCard.findViewById<AppCompatImageView>(R.id.aqiInfoIcon).setOnClickListener {
                val dialogInfoBundle = AppUtils.getAQIDialogInfo(this, context!!)
                val infoBottomSheetFragment = InfoBottomSheetFragment(dialogInfoBundle)
                infoBottomSheetFragment.show(parentFragmentManager, infoBottomSheetFragment.tag)
            }
        }
    }

    private fun showDialog(s: String) {
        if (isAdded) RetrofitHelper.instance?.showProgressDialog(context, s)
    }

    private fun dismissDialog() {
        if (isAdded) RetrofitHelper.instance?.dismissProgressDialog()
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
        rateYes.setOnClickListener(this)
        rateNo.setOnClickListener(this)
    }

    public fun locationPermissionResult(success: Boolean) {
        if (success) checkGPSAndRequestLocation()
        else aqiData
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.rateNo -> rateUsCard.animate().alpha(0.0f).translationX(200f).setDuration(500).withEndAction {
                rateUsCard.visibility = View.GONE
                sharedPrefUtils?.rateCardDone(true)
            }
            R.id.rateYes -> {
                sharedPrefUtils?.rateCardDone(true)
                rateUsCard.visibility = View.GONE
                val uri = Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }

    fun updateValues() {
        if (::mainActivity.isInitialized) {
            mainActivity.updateValues = false
            aqiCircle.findViewById<TextView>(R.id.temperatureTextView)?.text = getTemperatureValue(this.temperature)
            airPropertiesLayout?.findViewById<TextView>(R.id.windTextView)?.text = getWindSpeedValue(this.windSpeed)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::aqiViewModel.isInitialized && isAdded) {
            activity?.apply {
                aqiViewModel.status.removeObservers(this)
                aqiViewModel.apiResponse.removeObservers(this)
                aqiViewModel.getGPSApiResponse("").removeObservers(this)
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