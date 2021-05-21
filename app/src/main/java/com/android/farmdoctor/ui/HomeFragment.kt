package com.android.farmdoctor.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.fragment.NavHostFragment
import com.android.farmdoctor.R
import com.android.farmdoctor.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var fragmentHomeBinding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return fragmentHomeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBar()
        setHasOptionsMenu(true)
        with(fragmentHomeBinding) {
            cvSearchPlant.setOnClickListener(this@HomeFragment)
            cvPlantDisease.setOnClickListener(this@HomeFragment)
            cvDetection.setOnClickListener(this@HomeFragment)
            cvDetectionHistory.setOnClickListener(this@HomeFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        selectOptionsMenu(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        with(fragmentHomeBinding) {
            when(v) {
                cvSearchPlant -> NavHostFragment
                        .findNavController(this@HomeFragment)
                        .navigate(R.id.action_homeFragment_to_plantSpeciesFragment)
                cvPlantDisease -> NavHostFragment
                        .findNavController(this@HomeFragment)
                        .navigate(R.id.action_homeFragment_to_plantDiseasesFragment)
                cvDetection -> NavHostFragment
                        .findNavController(this@HomeFragment)
                        .navigate(R.id.action_homeFragment_to_detectionFragment)
                cvDetectionHistory -> NavHostFragment
                        .findNavController(this@HomeFragment)
                        .navigate(R.id.action_homeFragment_to_detectionHistoriesFragment)
            }
        }
    }

    private fun setActionBar() {
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.fd_logo, null)
        val bitmap = drawable?.toBitmap()
        val scaledDrawable = BitmapDrawable(resources,
            bitmap?.let { Bitmap.createScaledBitmap(it, 60, 60, true) })
        (activity as? AppCompatActivity?)?.supportActionBar?.setHomeAsUpIndicator(scaledDrawable)
        (activity as? AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity?)?.supportActionBar?.title = "Farm Doctor"
    }

    private fun selectOptionsMenu(selectedMenu: Int) {
        when (selectedMenu) {
            R.id.app_bar_about -> NavHostFragment
                .findNavController(this)
                .navigate(R.id.action_homeFragment_to_aboutActivity)
            R.id.app_bar_exit -> {
                requireActivity().onBackPressed()
                activity?.finish()
                super.onDestroy()
            }
        }
    }
}