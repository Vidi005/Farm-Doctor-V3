package com.android.farmdoctor.ui.plant

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesEntity
import com.android.farmdoctor.data.source.local.entity.SynonymEntity
import com.android.farmdoctor.databinding.FragmentDetailPlantSpeciesBinding
import com.android.farmdoctor.databinding.LayoutSynonymBinding
import com.android.farmdoctor.ui.plant.adapter.SynonymsAdapter
import com.android.farmdoctor.viewmodel.DetailPlantSpeciesViewModel
import com.android.farmdoctor.viewmodel.PlantSpeciesViewModelFactory
import com.android.farmdoctor.vo.Status
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class DetailPlantSpeciesFragment : Fragment() {

    private lateinit var fragmentDetailPlantSpeciesBinding: FragmentDetailPlantSpeciesBinding
    private lateinit var layoutSynonymBinding: LayoutSynonymBinding
    private lateinit var synonymsAdapter: SynonymsAdapter
    private lateinit var viewModel: DetailPlantSpeciesViewModel
    private val list = ArrayList<SynonymEntity>()
    companion object {
        const val EXTRA_PLANT_SPECIES = "extra_plant_species"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentDetailPlantSpeciesBinding =
            FragmentDetailPlantSpeciesBinding.inflate(layoutInflater, container, false)
        layoutSynonymBinding = fragmentDetailPlantSpeciesBinding.layoutSynonym
        return fragmentDetailPlantSpeciesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBar()
        setHasOptionsMenu(true)
        showSynonymList()
        getDetailPlantSpeciesLiveData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onNavigateUp(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    private fun setActionBar() {
        (activity as AppCompatActivity?)?.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Detail Plant Species"
        }
    }

    private fun onNavigateUp(itemId: Int) {
        if (itemId == android.R.id.home) requireActivity().onBackPressed()
    }

    private fun getDetailPlantSpeciesLiveData() {
        val factory = PlantSpeciesViewModelFactory.getInstance(activity as Activity)
        viewModel = ViewModelProvider(this, factory)[DetailPlantSpeciesViewModel::class.java]
        if (arguments != null) {
            val plantSpeciesData =
                arguments?.getParcelable<PlantSpeciesEntity>(EXTRA_PLANT_SPECIES) as PlantSpeciesEntity
            viewModel.apply {
                setSelectedPlantSpecies("${plantSpeciesData.scientificName}")
                detailPlantSpecies.observe(viewLifecycleOwner, {
                    if (it != null) {
                        when (it.status) {
                            Status.LOADING -> showLoading(true)
                            Status.SUCCESS -> {
                                populateDetailPlantSpecies(it.data?.mPlantSpecies as PlantSpeciesEntity)
                                showSynonyms(it.data.mSynonym as ArrayList<SynonymEntity>)
                                showLoading(false)
                            }
                            Status.ERROR -> {
                                showLoading(false)
                                Toast.makeText(activity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                })
            }
        }
    }

    private fun populateDetailPlantSpecies(detailPlantSpecies: PlantSpeciesEntity) {
        fragmentDetailPlantSpeciesBinding.apply {
            Glide.with(this@DetailPlantSpeciesFragment)
                .load(detailPlantSpecies.image)
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                .error(R.drawable.ic_no_image)
                .into(rivImageReceived)
            tvScientificNameReceived.text = detailPlantSpecies.scientificName
            if (detailPlantSpecies.commonName.isNullOrBlank() || detailPlantSpecies.commonName == "null")
                tvCommonNameReceived.text = ""
            else tvCommonNameReceived.text = "${detailPlantSpecies.commonName}"
            if (detailPlantSpecies.author.isNullOrBlank() || detailPlantSpecies.author == "null")
                tvAuthorReceived.text = "-"
            else tvAuthorReceived.text = "${detailPlantSpecies.author}"
            if (detailPlantSpecies.rank.isNullOrBlank() || detailPlantSpecies.rank == "null")
                tvRankReceived.text = "-"
            else tvRankReceived.text = "${detailPlantSpecies.rank}"
            if (detailPlantSpecies.year.isNullOrBlank() || detailPlantSpecies.year == "null")
                tvYearReceived.text = "-"
            else tvYearReceived.text = "${detailPlantSpecies.year}"
            if (detailPlantSpecies.status.isNullOrBlank() || detailPlantSpecies.status == "null")
                tvStatusReceived.text = "-"
            else tvStatusReceived.text = "${detailPlantSpecies.status}"
            if (detailPlantSpecies.genus.isNullOrBlank() || detailPlantSpecies.genus == "null")
                tvGenusReceived.text = "-"
            else tvGenusReceived.text = "${detailPlantSpecies.genus}"
            if (detailPlantSpecies.family.isNullOrBlank() || detailPlantSpecies.family == "null")
                tvFamilyReceived.text = "-"
            else tvFamilyReceived.text = "${detailPlantSpecies.family}"
        }
    }

    private fun showSynonymList() {
        synonymsAdapter = SynonymsAdapter(list)
        with(layoutSynonymBinding.rvListSynonym) {
            layoutManager = LinearLayoutManager(requireActivity())
            setHasFixedSize(true)
            adapter = synonymsAdapter
            val dividerItemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }
    }

    private fun showSynonyms(synonymItems: ArrayList<SynonymEntity>) {
        synonymsAdapter.setSynonym(synonymItems)
        when {
            synonymItems.size > 0 -> layoutSynonymBinding.apply {
                tvSynonymBar.text = StringBuilder("Synonyms: ${synonymItems.size}")
                ivNoSynonym.visibility = View.GONE
                tvNoSynonym.visibility = View.GONE
            }
            else -> layoutSynonymBinding.apply {
                tvSynonymBar.text = "Synonym: 0"
                ivNoSynonym.visibility = View.VISIBLE
                tvNoSynonym.visibility = View.VISIBLE
            }
        }
    }

    private fun showLoading(state: Boolean) {
        if (state) fragmentDetailPlantSpeciesBinding.pbDetailSpecies.visibility = View.VISIBLE
        else fragmentDetailPlantSpeciesBinding.pbDetailSpecies.visibility = View.GONE
    }
}