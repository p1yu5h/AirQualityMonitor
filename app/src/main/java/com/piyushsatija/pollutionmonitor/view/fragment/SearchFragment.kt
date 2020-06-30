@file:Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")

package com.piyushsatija.pollutionmonitor.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.piyushsatija.pollutionmonitor.AqiViewModel
import com.piyushsatija.pollutionmonitor.R
import com.piyushsatija.pollutionmonitor.adapters.SearchResultAdapter
import com.piyushsatija.pollutionmonitor.model.Status
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var aqiViewModel: AqiViewModel
    private lateinit var searchResultAdapter: SearchResultAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView.setOnQueryTextListener(this)
        searchRecyclerView.layoutManager = LinearLayoutManager(context)
        searchRecyclerView.setHasFixedSize(true)

        aqiViewModel = ViewModelProvider(this).get(AqiViewModel::class.java)
        aqiViewModel.status.observe(activity!!, Observer {status ->
            when (status) {
                Status.DONE -> {
                    searchProgressBar.visibility = View.GONE
                    searchRecyclerView.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    searchProgressBar.visibility = View.GONE
                    searchRecyclerView.visibility = View.GONE
                }
                Status.FETCHING -> {
                    searchProgressBar.visibility = View.VISIBLE
                    searchRecyclerView.visibility = View.GONE
                }
            }
        })

        aqiViewModel.searchResponse.observe(activity!!, Observer {
            it.data?.apply {
                val dataList = this.filter { data ->  data.aqi != "-" }
                searchResultAdapter = SearchResultAdapter(dataList)
                searchRecyclerView.adapter = searchResultAdapter
            }
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

    override fun onDestroy() {
        super.onDestroy()
        if (::aqiViewModel.isInitialized && isAdded) {
            activity?.apply {
                aqiViewModel.status.removeObservers(this)
                aqiViewModel.searchResponse.removeObservers(this)
            }
        }
    }
}