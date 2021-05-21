package com.android.farmdoctor.ui.detection.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.farmdoctor.data.source.factory.model.Detection
import com.android.farmdoctor.databinding.ItemPlantDiseaseBinding
import com.android.farmdoctor.ui.detection.callback.OnResultItemClickCallback

class ResultAdapter(
    private val resultDetection: ArrayList<Detection>,
    private val callback: OnResultItemClickCallback) :
    RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    fun setResultDetectionData(items: ArrayList<Detection>) {
        resultDetection.clear()
        items.sortByDescending { it.accuracy }
        resultDetection.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val itemPlantDiseaseBinding =
            ItemPlantDiseaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultViewHolder(itemPlantDiseaseBinding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) =
        holder.bind(resultDetection[position])

    override fun getItemCount(): Int {
        val limit = 1
        return if (resultDetection.size > limit) limit
        else resultDetection.size
    }

    inner class ResultViewHolder(private val binding: ItemPlantDiseaseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detection: Detection) {
            with(binding) {
                tvNameResult.text = StringBuilder("Name of Disease: ${detection.name}")
                tvAccResult.text = StringBuilder("Probability: ${detection.accuracy}%")
                itemView.setOnClickListener { callback.onItemClicked(detection) }
            }
        }
    }
}