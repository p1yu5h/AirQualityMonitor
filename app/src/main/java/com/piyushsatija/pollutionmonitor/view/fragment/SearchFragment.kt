package com.piyushsatija.pollutionmonitor.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.piyushsatija.pollutionmonitor.AqiViewModel
import com.piyushsatija.pollutionmonitor.R
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var aqiViewModel: AqiViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aqiViewModel = ViewModelProvider(this).get(AqiViewModel::class.java)
        searchView.setOnQueryTextListener(this)
        aqiViewModel.searchResponse.observe(activity!!, Observer {
        })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.apply { aqiViewModel.searchKeyword(this) }
        searchView.clearFocus()
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }
}