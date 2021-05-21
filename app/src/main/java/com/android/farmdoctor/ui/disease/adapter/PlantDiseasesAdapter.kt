package com.android.farmdoctor.ui.disease.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.factory.model.PlantDisease
import com.android.farmdoctor.databinding.ItemListDiseasesBinding
import com.android.farmdoctor.ui.disease.callback.OnDiseaseItemClickCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.util.*
import kotlin.collections.ArrayList

class PlantDiseasesAdapter(
        private val listPlantDiseases: ArrayList<PlantDisease>,
        private val callback: OnDiseaseItemClickCallback) :
        RecyclerView.Adapter<PlantDiseasesAdapter.GridViewHolder>(), Filterable {

    private var filterListPlantDiseases = listPlantDiseases
    companion object {
        private val TAG = PlantDiseasesAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val itemListDiseasesBinding =
            ItemListDiseasesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridViewHolder(itemListDiseasesBinding)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) =
        holder.bind(filterListPlantDiseases[position])

    override fun getItemCount(): Int = filterListPlantDiseases.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val itemSearch = "$constraint"
                filterListPlantDiseases = if (itemSearch.isEmpty()) listPlantDiseases
                else {
                    val itemList = ArrayList<PlantDisease>()
                    listPlantDiseases.forEach {
                        if (it.name?.toLowerCase(Locale.ROOT)
                            ?.contains(itemSearch.toLowerCase(Locale.ROOT)) == true)
                                itemList.add(it)
                    }
                    itemList
                }
                val filterResults = FilterResults()
                filterResults.values = filterListPlantDiseases
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filterListPlantDiseases = results?.values as ArrayList<PlantDisease>
                Log.d(TAG, "$filterListPlantDiseases")
                notifyDataSetChanged()
            }
        }
    }

    inner class GridViewHolder(private val binding: ItemListDiseasesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(plantDisease: PlantDisease) {
            with(binding) {
                Glide.with(itemView.context)
                    .load(plantDisease.picture)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                    .error(R.drawable.ic_no_image)
                    .into(ivItemPicture)
                tvItemName.text = plantDisease.name
                itemView.setOnClickListener { callback.onItemClicked(plantDisease) }
            }
        }
    }
}