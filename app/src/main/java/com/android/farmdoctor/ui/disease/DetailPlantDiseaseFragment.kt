package com.android.farmdoctor.ui.disease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.farmdoctor.databinding.FragmentDetailPlantDiseaseBinding
import com.bumptech.glide.Glide

class DetailPlantDiseaseFragment : Fragment() {

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_PICTURE = "extra_picture"
        const val EXTRA_DETAIL = "extra_detail"
    }
    private lateinit var fragmentDetailPlantDiseaseBinding: FragmentDetailPlantDiseaseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentDetailPlantDiseaseBinding =
            FragmentDetailPlantDiseaseBinding.inflate(layoutInflater, container, false)
        return fragmentDetailPlantDiseaseBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBar()
        setHasOptionsMenu(true)
        getSelectedPlantDisease()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onNavigateUp(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    private fun setActionBar() {
        (requireActivity() as? AppCompatActivity?)?.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Detail Plant Disease"
        }
    }

    private fun onNavigateUp(itemId: Int) {
        if (itemId == android.R.id.home) activity?.onBackPressed()
    }

    private fun getSelectedPlantDisease() {
        if (arguments != null) {
            val image = arguments?.getInt(EXTRA_PICTURE, 0)
            val name = arguments?.getString(EXTRA_NAME).toString()
            val detail = arguments?.getInt(EXTRA_DETAIL, 0)
            with(fragmentDetailPlantDiseaseBinding) {
                Glide.with(this@DetailPlantDiseaseFragment).load(image).into(rivPictureReceived)
                tvNameReceived.text = name
                vsDetailReceived.apply {
                    layoutResource = detail as Int
                    inflate()
                }
            }
        }
    }
}