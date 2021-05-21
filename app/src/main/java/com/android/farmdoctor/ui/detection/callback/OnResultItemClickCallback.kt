package com.android.farmdoctor.ui.detection.callback

import com.android.farmdoctor.data.source.factory.model.Detection

interface OnResultItemClickCallback {
    fun onItemClicked(data: Detection)
}
