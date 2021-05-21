package com.android.farmdoctor.ui.plant

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.local.entity.ItemsCountEntity
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesEntity
import com.android.farmdoctor.data.source.remote.StatusResponse
import com.android.farmdoctor.databinding.FragmentPlantSpeciesBinding
import com.android.farmdoctor.ui.plant.DetailPlantSpeciesFragment.Companion.EXTRA_PLANT_SPECIES
import com.android.farmdoctor.ui.plant.adapter.PlantSpeciesAdapter
import com.android.farmdoctor.ui.plant.callback.OnPlantItemClickCallback
import com.android.farmdoctor.viewmodel.PlantSpeciesViewModel
import com.android.farmdoctor.viewmodel.PlantSpeciesViewModelFactory

class PlantSpeciesFragment : Fragment(), OnPlantItemClickCallback, View.OnClickListener {

    private lateinit var fragmentPlantSpeciesBinding: FragmentPlantSpeciesBinding
    private lateinit var plantSpeciesAdapter: PlantSpeciesAdapter
    private lateinit var viewModel: PlantSpeciesViewModel
    private val list = ArrayList<PlantSpeciesEntity>()
    private var searchQuery = ""
    private var page = 1
    private var totalItems = 1
    private var totalPages = 1
    private val orderBy = HashMap<String, String>()
    private var isImgBtnClicked = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentPlantSpeciesBinding =
            FragmentPlantSpeciesBinding.inflate(layoutInflater, container, false)
        return fragmentPlantSpeciesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBar()
        setHasOptionsMenu(true)
        orderBy["order[scientific_name]"] = "asc"
        showPlantSpeciesList()
        val factory = PlantSpeciesViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[PlantSpeciesViewModel::class.java]
        getItemsCountData()
        searchPlantSpecies()
        checkPages()
        fragmentPlantSpeciesBinding.apply {
            imgBtnPrevPage.setOnClickListener(this@PlantSpeciesFragment)
            imgBtnNextPage.setOnClickListener(this@PlantSpeciesFragment)
        }
    }

    override fun onItemClicked(data: PlantSpeciesEntity) {
        val mBundle = Bundle().apply { putParcelable(EXTRA_PLANT_SPECIES, data) }
        NavHostFragment
            .findNavController(this)
            .navigate(R.id.action_plantSpeciesFragment_to_detailPlantSpeciesFragment, mBundle)
        closeKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_plant_species, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onNavigateUp(item.itemId)
        orderItems(item.itemId)
        item.isChecked = true
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        with(fragmentPlantSpeciesBinding) {
            when (v) {
                imgBtnPrevPage -> if (!isImgBtnClicked || !list.isNullOrEmpty() ||
                    fragmentPlantSpeciesBinding.rvListPlantSpecies.isVisible) {
                        isImgBtnClicked = true
                        fragmentPlantSpeciesBinding.tvSearchResult.text = "Loading..."
                        showLoading(true)
                        page -= 1
                        getViewModelLiveData(searchQuery, orderBy)
                } else {
                    isImgBtnClicked = false
                    showLoading(false)
                }
                imgBtnNextPage -> if (!isImgBtnClicked || !list.isNullOrEmpty() ||
                    fragmentPlantSpeciesBinding.rvListPlantSpecies.isVisible) {
                        isImgBtnClicked = true
                        fragmentPlantSpeciesBinding.tvSearchResult.text = "Loading..."
                        showLoading(true)
                        page += 1
                        getViewModelLiveData(searchQuery, orderBy)
                } else {
                    isImgBtnClicked = false
                    showLoading(false)
                }
            }
        }
    }

    private fun setActionBar() {
        (requireActivity() as? AppCompatActivity?)?.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Plant Species"
        }
    }

    private fun orderItems(itemId: Int) {
        fragmentPlantSpeciesBinding.tvSearchResult.text = "Loading..."
        showLoading(true)
        orderBy.clear()
        when (itemId) {
            R.id.app_bar_order_by_name -> orderBy["order[scientific_name]"] = "asc"
            R.id.app_bar_order_by_name_desc -> orderBy["order[scientific_name]"] = "desc"
            R.id.app_bar_order_by_year -> orderBy["order[year]"] = "asc"
            R.id.app_bar_order_by_year_desc -> orderBy["order[year]"] = "desc"
        }
        getViewModelLiveData(searchQuery, orderBy)
    }

    private fun showPlantSpeciesList() {
        plantSpeciesAdapter = PlantSpeciesAdapter(list, this)
        with(fragmentPlantSpeciesBinding.rvListPlantSpecies) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = plantSpeciesAdapter
        }
    }

    private fun onNavigateUp(itemId: Int) {
        if (itemId == android.R.id.home) {
            requireActivity().onBackPressed()
            closeKeyboard()
        }
    }

    private fun searchPlantSpecies() {
        fragmentPlantSpeciesBinding
            .svPlantSpecies.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                page = 1
                if (!query.isNullOrEmpty()) {
                    searchQuery = query
                    showLoading(true)
                    getViewModelLiveData(query, orderBy)
                } else {
                    totalItems = 1
                    checkPages()
                    fragmentPlantSpeciesBinding.tvPageIndicator.text = "1 / 1"
                    showLoading(false)
                }
                closeKeyboard()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                page = 1
                if (newText?.isNotEmpty() as Boolean) {
                    searchQuery = newText
                    showLoading(true)
                    getViewModelLiveData(newText, orderBy)
                    fragmentPlantSpeciesBinding.tvSearchResult.text = "Searching..."
                } else {
                    totalItems = 1
                    checkPages()
                    showLoading(false)
                    list.clear()
                    fragmentPlantSpeciesBinding.apply {
                        tvSearchResult.text = "Click and Type to Search"
                        tvPageIndicator.text = "1 / 1"
                    }
                }
                return true
            }
        })
        fragmentPlantSpeciesBinding.svPlantSpecies.isSubmitButtonEnabled = true
    }

    private fun getItemsCountData() {
        viewModel.itemCounts.observe(viewLifecycleOwner, {
            if (it != null) {
                page = it.page ?: 1
                totalItems = it.totalItems ?: 1
            }
        })
    }

    private fun getViewModelLiveData(searchQuery: String, orderedBy: Map<String, String>) {
        if (parentFragment != null && this.searchQuery.isNotBlank()) {
            fragmentPlantSpeciesBinding.apply {
                rvListPlantSpecies.visibility = View.GONE
                ivSearchNotFound.visibility = View.GONE
            }
            viewModel.getPlantSpecies(searchQuery, page, orderedBy).observe(viewLifecycleOwner, {
                when (it.status) {
                    StatusResponse.SUCCESS -> showPlantSpeciesItems(it.body)
                    StatusResponse.EMPTY -> {
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        showLoading(false)
                    }
                    StatusResponse.ERROR ->
                        Toast.makeText(activity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
            })
            viewModel.getMeta(searchQuery, page, orderedBy).observe(viewLifecycleOwner, {
                when (it.status) {
                    StatusResponse.SUCCESS -> {
                        totalItems = it.body.total ?: 1
                        with(fragmentPlantSpeciesBinding) {
                            tvSearchResult.text = StringBuilder("Items Found: $totalItems")
                            val pages = totalItems.toDouble() / 20.0
                            val modTotalPages = totalItems.toDouble() % 20.0
                            if (modTotalPages == 0.0) totalPages = pages.toInt()
                            else totalPages = pages.toInt() + 1
                            tvPageIndicator.text = StringBuilder("$page / $totalPages")
                        }
                        checkPages()
                        viewModel.getItemCounts(ItemsCountEntity(page, totalItems))
                        isImgBtnClicked = false
                        showLoading(false)
                    }
                    StatusResponse.EMPTY -> {
                        totalItems = 0
                        isImgBtnClicked = false
                        checkPages()
                    }
                    StatusResponse.ERROR -> {
                        totalItems = 0
                        Toast.makeText(activity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    private fun showPlantSpeciesItems(plantSpeciesItems: List<PlantSpeciesEntity>) {
        plantSpeciesAdapter.setPlantSpecies(plantSpeciesItems)
        when {
            plantSpeciesItems.isNotEmpty() -> {
                fragmentPlantSpeciesBinding.apply {
                    rvListPlantSpecies.visibility = View.VISIBLE
                    ivSearchNotFound.visibility = View.GONE
                }
            }
            else -> {
                list.clear()
                fragmentPlantSpeciesBinding.apply {
                    tvSearchResult.text = "Search Result not Found"
                    ivSearchNotFound.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkPages() {
        with(fragmentPlantSpeciesBinding) {
            if (page <= 1) imgBtnPrevPage.isClickable = false
            if (totalItems <= 20 || page == totalPages) imgBtnNextPage.isClickable = false
            if (page > 1) imgBtnPrevPage.isClickable = true
            if (page.toDouble() < totalItems.toDouble()/20.0) imgBtnNextPage.isClickable = true
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) fragmentPlantSpeciesBinding.pbSearchSpecies.visibility = View.VISIBLE
        else fragmentPlantSpeciesBinding.pbSearchSpecies.visibility = View.GONE
    }

    private fun closeKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}