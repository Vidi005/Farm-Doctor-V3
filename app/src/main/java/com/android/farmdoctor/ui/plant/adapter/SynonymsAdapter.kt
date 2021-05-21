package com.android.farmdoctor.ui.plant.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.farmdoctor.data.source.local.entity.SynonymEntity
import com.android.farmdoctor.databinding.ItemListSynonymsBinding

class SynonymsAdapter(private val listSynonyms: ArrayList<SynonymEntity>) :
    RecyclerView.Adapter<SynonymsAdapter.SynonymViewHolder>() {

    fun setSynonym(synonymItems: List<SynonymEntity>) {
        if (synonymItems.isNullOrEmpty()) return
        listSynonyms.clear()
        listSynonyms.addAll(synonymItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SynonymViewHolder {
        val itemListSynonymsBinding =
            ItemListSynonymsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SynonymViewHolder(itemListSynonymsBinding)
    }

    override fun onBindViewHolder(holder: SynonymViewHolder, position: Int) =
        holder.bind(listSynonyms[position])

    override fun getItemCount(): Int = listSynonyms.size

    inner class SynonymViewHolder(private val binding: ItemListSynonymsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(synonym: SynonymEntity) {
            with(binding) {
                tvNoScientificNameSynonym.text = StringBuilder("${synonym.number}.")
                tvItemScientificNameSynonym.text = synonym.synonym ?: "-"
            }
        }
    }
}