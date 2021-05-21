package com.android.farmdoctor.ui.detection

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.android.farmdoctor.databinding.FragmentDetectionBinding
import com.android.farmdoctor.ui.detection.adapter.SectionPagerAdapter

class DetectionFragment : Fragment() {

    private lateinit var fragmentDetectionBinding: FragmentDetectionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentDetectionBinding =
            FragmentDetectionBinding.inflate(layoutInflater, container, false)
        return fragmentDetectionBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBar()
        setHasOptionsMenu(true)
        setPagerAdapter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onNavigateUp(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        childFragmentManager.fragments.forEach { it.onActivityResult(requestCode, resultCode, data) }

    private fun setActionBar() {
        (requireActivity() as? AppCompatActivity?)?.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Diagnose Plant Diseases"
        }
    }

    private fun onNavigateUp(itemId: Int) {
        if (itemId == android.R.id.home) requireActivity().onBackPressed()
    }

    private fun setPagerAdapter() {
        val sectionPagerAdapter = SectionPagerAdapter(childFragmentManager)
        fragmentDetectionBinding.viewPager.adapter = sectionPagerAdapter
    }
}