package org.wit.inventorymanager.ui.stockDetail

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.wit.inventorymanager.R

class StockDetailFragment : Fragment() {

    companion object {
        fun newInstance() = StockDetailFragment()
    }

    private lateinit var viewModel: StockDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.stock_detail_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(StockDetailViewModel::class.java)
        // TODO: Use the ViewModel
    }

}