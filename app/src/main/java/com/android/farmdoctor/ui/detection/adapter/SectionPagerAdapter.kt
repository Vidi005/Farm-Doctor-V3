package com.android.farmdoctor.ui.detection.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.android.farmdoctor.ui.detection.CameraFragment
import com.android.farmdoctor.ui.detection.ImportGalleryFragment

class SectionPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> CameraFragment()
        1 -> ImportGalleryFragment()
        else -> Fragment()
    }
}