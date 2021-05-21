package com.android.farmdoctor.ui.history.callback

import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity

interface OnHistoryItemClickCallback {
    fun onItemClicked(data: DetectionHistoryEntity)
}