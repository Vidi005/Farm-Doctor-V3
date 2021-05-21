package com.android.farmdoctor.ui.plant.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.local.entity.PlantSpeciesEntity
import com.android.farmdoctor.databinding.ItemListPlantSpeciesBinding
import com.android.farmdoctor.ui.plant.callback.OnPlantItemClickCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class PlantSpeciesAdapter(
    private val listPlantSpecies: ArrayList<PlantSpeciesEntity>,
    private val callback: OnPlantItemClickCallback) :
        RecyclerView.Adapter<PlantSpeciesAdapter.PlantSpeciesViewHolder>() {

    fun setPlantSpecies(plantSpeciesItem: List<PlantSpeciesEntity>?) {
        this.listPlantSpecies.clear()
        plantSpeciesItem?.let { this.listPlantSpecies.addAll(it) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantSpeciesViewHolder {
        val itemListPlantSpeciesBinding =
            ItemListPlantSpeciesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlantSpeciesViewHolder(itemListPlantSpeciesBinding)
    }

    override fun onBindViewHolder(holder: PlantSpeciesViewHolder, position: Int) =
        holder.bind(listPlantSpecies[position])

    override fun getItemCount(): Int = listPlantSpecies.size

    inner class PlantSpeciesViewHolder(private val binding: ItemListPlantSpeciesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(plantSpecies: PlantSpeciesEntity) {
            with(binding) {
                Glide.with(itemView.context)
                    .load(plantSpecies.image)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                    .error(R.drawable.ic_no_image)
                    .into(rivItemImage)
                tvItemScientificName.text = plantSpecies.scientificName ?: "-"
                if (plantSpecies.commonName.isNullOrEmpty() || plantSpecies.commonName == "null")
                    tvItemCommonName.text = StringBuilder("Common Name: -")
                else tvItemCommonName.text = StringBuilder("Common Name: ${plantSpecies.commonName}")
                if (plantSpecies.author.isNullOrEmpty() || plantSpecies.author == "null")
                    tvItemAuthor.text = StringBuilder("Author: -")
                else tvItemAuthor.text = StringBuilder("Author: ${plantSpecies.author}")
                if (plantSpecies.rank.isNullOrEmpty() || plantSpecies.rank == "null")
                    tvItemRank.text = StringBuilder("Taxonomy Rank: -")
                else tvItemRank.text = StringBuilder("Taxonomy Rank: ${plantSpecies.rank}")
                itemView.setOnClickListener { callback.onItemClicked(plantSpecies) }
            }
        }

    }
}