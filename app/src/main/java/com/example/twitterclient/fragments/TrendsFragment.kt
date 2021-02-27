package com.example.twitterclient.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.twitterclient.Commons
import com.example.twitterclient.R
import com.example.twitterclient.adapters.TrendsAdapter
import com.example.twitterclient.adapters.TrendsAdapterCallbacks
import com.example.twitterclient.dataClasses.Trend
import kotlinx.android.synthetic.main.fragment_trends.*

class TrendsFragment : Fragment(), TrendsAdapterCallbacks, SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private var INSTANCE: TrendsFragment? = null
        fun getInstance(): TrendsFragment {
            val tempInstance = INSTANCE
            return if (tempInstance != null) {
                tempInstance
            } else {
                val t = TrendsFragment()
                INSTANCE = t
                t
            }
        }
    }

    private var viewModel: TrendsViewModel? = null
    private val trends = ArrayList<Trend>()
    private val adapter by lazy {
        TrendsAdapter(
            this,
            trends
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trends, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this.requireActivity()).get(TrendsViewModel::class.java)

        rvTrends.adapter = adapter
        rvTrends.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        viewModel?.getTrendsLiveData()?.observe(viewLifecycleOwner, {
            trends.clear()
            trends.addAll(it)
            adapter.notifyDataSetChanged()
        })

        viewModel?.getTrends()

        viewModel?.getRefreshStatus()?.observe(viewLifecycleOwner, {
            swipeRefreshLayout.isRefreshing = it
        })
        viewModel?.getLogOutStatus()?.observe(viewLifecycleOwner, {
            if(it){ Commons.logOut(requireActivity()) }
        })
    }

    override fun onTrendClicked(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onRefresh() {
        viewModel?.getTrends()
    }

}