package com.android.farmdoctor.ui.disease

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.factory.model.PlantDisease
import com.android.farmdoctor.databinding.FragmentPlantDiseasesBinding
import com.android.farmdoctor.ui.disease.DetailPlantDiseaseFragment.Companion.EXTRA_DETAIL
import com.android.farmdoctor.ui.disease.DetailPlantDiseaseFragment.Companion.EXTRA_NAME
import com.android.farmdoctor.ui.disease.DetailPlantDiseaseFragment.Companion.EXTRA_PICTURE
import com.android.farmdoctor.ui.disease.adapter.PlantDiseasesAdapter
import com.android.farmdoctor.ui.disease.callback.OnDiseaseItemClickCallback
import com.android.farmdoctor.viewmodel.PlantDiseaseViewModelFactory
import com.android.farmdoctor.viewmodel.PlantDiseaseViewModel

class PlantDiseasesFragment : Fragment(), OnDiseaseItemClickCallback {

    private lateinit var fragmentPlantDiseasesBinding: FragmentPlantDiseasesBinding
    private lateinit var plantDiseaseAdapter: PlantDiseasesAdapter
    private var list = ArrayList<PlantDisease>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentPlantDiseasesBinding =
            FragmentPlantDiseasesBinding.inflate(layoutInflater, container, false)
        return fragmentPlantDiseasesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBar()
        setHasOptionsMenu(true)
        getViewModelData()
        showPlantDiseaseList()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_plant_disease, menu)
        val searchItem = menu.findItem(R.id.app_bar_search)
        (searchItem.actionView as SearchView).apply {
            queryHint = "Search Plant Disease Name"
            isSubmitButtonEnabled = true
            setOnQueryTextListener(object : OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    plantDiseaseAdapter.filter.filter(newText)
                    if (newText.isNullOrEmpty())
                        fragmentPlantDiseasesBinding.tvListDisease.visibility = View.GONE
                    else {
                        fragmentPlantDiseasesBinding.tvListDisease.visibility = View.VISIBLE
                        when {
                            plantDiseaseAdapter.itemCount > 0 ->
                                fragmentPlantDiseasesBinding.tvListDisease.text =
                                    StringBuilder("Items Found: ${plantDiseaseAdapter.itemCount}")
                            else ->
                                fragmentPlantDiseasesBinding.tvListDisease.text = "Item not Found"
                        }
                    }
                    return true
                }
            })
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onNavigateUp(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClicked(data: PlantDisease) {
        if (view != null) {
            val mBundle = Bundle().apply {
                putString(EXTRA_NAME, data.name)
                putInt(EXTRA_PICTURE, data.picture as Int)
                data.detail?.let { putInt(EXTRA_DETAIL, it) }
            }
            NavHostFragment
                .findNavController(this)
                .navigate(R.id.action_plantDiseasesFragment_to_detailPlantDiseaseFragment, mBundle)
            closeKeyboard()
        }
    }

    private fun setActionBar() {
        (requireContext() as? AppCompatActivity?)?.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Plant Diseases"
        }
    }

    private fun onNavigateUp(itemId: Int) {
        if (itemId == android.R.id.home) {
            activity?.onBackPressed()
            closeKeyboard()
        }
    }

    private fun getViewModelData() {
        if (activity != null) {
            val factory = PlantDiseaseViewModelFactory.getInstance(requireActivity())
            val viewModel = ViewModelProvider(this, factory)[PlantDiseaseViewModel::class.java]
            list = viewModel.getPlantDiseases()
        }
    }

    private fun showPlantDiseaseList() {
        plantDiseaseAdapter = PlantDiseasesAdapter(list, this).apply { notifyDataSetChanged() }
        with(fragmentPlantDiseasesBinding.rvListDiseases) {
            layoutManager = GridLayoutManager(requireActivity(), 2)
            setHasFixedSize(true)
            adapter = plantDiseaseAdapter
        }
    }

    private fun closeKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}