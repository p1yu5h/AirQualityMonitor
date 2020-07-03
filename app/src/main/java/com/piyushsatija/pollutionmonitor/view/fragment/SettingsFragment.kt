package com.piyushsatija.pollutionmonitor.view.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.piyushsatija.pollutionmonitor.BuildConfig
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.utils.Constants
import com.piyushsatija.pollutionmonitor.utils.SharedPrefUtils
import com.piyushsatija.pollutionmonitor.view.MainActivity
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment() {
    private var sharedPrefUtils: SharedPrefUtils? = null
    private lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefUtils = SharedPrefUtils.getInstance(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appVersion.text = BuildConfig.VERSION_NAME
        windSpeedCTA.text = when (sharedPrefUtils!!.getStringValue(Constants.WINDSPEED_UNIT, Constants.WINDSPEED_MPS)) {
            Constants.WINDSPEED_MPS -> getString(R.string.mps)
            Constants.WINDSPEED_KMPH -> getString(R.string.kmph)
            else -> getString(R.string.mps) //fallback
        }

        windSpeedCTA.setOnClickListener {
            val previousSelection = sharedPrefUtils!!.getStringValue(Constants.WINDSPEED_UNIT, Constants.WINDSPEED_MPS)
            showSingleChoiceDialog("Wind speed units", arrayOf(Constants.WINDSPEED_MPS, Constants.WINDSPEED_KMPH), previousSelection) { selectedItem: String ->
                sharedPrefUtils!!.saveStringValue(Constants.WINDSPEED_UNIT, selectedItem)
                windSpeedCTA.text = when (selectedItem) {
                    Constants.WINDSPEED_MPS -> getString(R.string.mps)
                    Constants.WINDSPEED_KMPH -> getString(R.string.kmph)
                    else -> getString(R.string.mps) //fallback
                }
            }
        }


        temperatureCTA.text = when (sharedPrefUtils!!.getStringValue(Constants.TEMPERATURE_UNIT, Constants.TEMPERATURE_CELSIUS)) {
            Constants.TEMPERATURE_CELSIUS -> getString(R.string.celsiusSymbol)
            Constants.TEMPERATURE_FAHRENHEIT -> getString(R.string.fahrenheitSymbol)
            else -> getString(R.string.celsiusSymbol) //fallback
        }

        temperatureCTA.setOnClickListener {
            val previousSelection = sharedPrefUtils!!.getStringValue(Constants.TEMPERATURE_UNIT, Constants.TEMPERATURE_CELSIUS)
            showSingleChoiceDialog("Temperature Units", arrayOf(Constants.TEMPERATURE_CELSIUS, Constants.TEMPERATURE_FAHRENHEIT), previousSelection) { selectedItem: String ->
                sharedPrefUtils!!.saveStringValue(Constants.TEMPERATURE_UNIT, selectedItem)
                temperatureCTA.text = when (selectedItem) {
                    Constants.TEMPERATURE_CELSIUS -> getString(R.string.celsiusSymbol)
                    Constants.TEMPERATURE_FAHRENHEIT -> getString(R.string.fahrenheitSymbol)
                    else -> getString(R.string.celsiusSymbol) //fallback
                }
            }
        }

        feedbackCTA.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${BuildConfig.EmailId}")
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for Air Pollution Monitor - AQI android app")
            }
            if (intent.resolveActivity(context!!.packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(
                        context,
                        "Please install an email client to send feedback.",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }

        shareCTA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareText))
            shareIntent.type = "text/plain"
            startActivity(shareIntent)
        }
    }

    private fun showSingleChoiceDialog(title: String, items: Array<String>, previousSelection: String, onConfirmSelection: (selectedItem: String) -> Unit) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context!!)
        builder.setTitle(title)
        var checkedItem = items.indexOf(previousSelection)
        builder.setSingleChoiceItems(items, checkedItem) { _, which ->
            checkedItem = which
        }
        builder.setPositiveButton("OK") { dialog, _ ->
            onConfirmSelection(items[checkedItem])
            if (::mainActivity.isInitialized) mainActivity.updateValues = true
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                SettingsFragment()
    }
}