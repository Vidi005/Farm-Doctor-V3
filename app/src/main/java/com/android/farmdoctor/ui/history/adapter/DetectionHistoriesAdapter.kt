package com.android.farmdoctor.ui.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.databinding.ItemDetectionHistoryBinding
import com.android.farmdoctor.ui.history.callback.OnHistoryItemClickCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class DetectionHistoriesAdapter(private val callback: OnHistoryItemClickCallback) :
        PagedListAdapter<DetectionHistoryEntity,
        DetectionHistoriesAdapter.DetectionHistoriesViewHolder>(DIFF_CALLBACK)
{

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DetectionHistoryEntity>() {
            override fun areItemsTheSame(
                oldItem: DetectionHistoryEntity,
                newItem: DetectionHistoryEntity
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: DetectionHistoryEntity,
                newItem: DetectionHistoryEntity
            ): Boolean = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionHistoriesViewHolder {
        val itemDetectionHistoryBinding =
            ItemDetectionHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetectionHistoriesViewHolder(itemDetectionHistoryBinding)
    }

    override fun onBindViewHolder(holder: DetectionHistoriesViewHolder, position: Int) {
        val listDetectionHistory = getItem(position)
        if (listDetectionHistory != null) holder.bind(listDetectionHistory)
    }

    fun getSwipedData(swipedPosition: Int): DetectionHistoryEntity =
        getItem(swipedPosition) as DetectionHistoryEntity

    inner class DetectionHistoriesViewHolder(private val binding: ItemDetectionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detectionHistory: DetectionHistoryEntity) {
            with(binding) {
                tvHistoryItemName.text = StringBuilder(": ${detectionHistory.name}")
                tvHistoryItemAcc.text = StringBuilder(": ${detectionHistory.accuracy}%")
                tvHistoryItemDate.text = StringBuilder(": ${detectionHistory.date}")
                if (detectionHistory.detectionBased == "Camera") Glide.with(itemView.context)
                    .load(R.drawable.ic_camera)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                    .error(R.drawable.ic_no_image)
                    .into(ivDetectionBased)
                else Glide.with(itemView.context)
                    .load(R.drawable.ic_gallery)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder))
                    .error(R.drawable.ic_no_image)
                    .into(ivDetectionBased)
                itemView.setOnClickListener { callback.onItemClicked(detectionHistory) }
            }
        }
    }
}