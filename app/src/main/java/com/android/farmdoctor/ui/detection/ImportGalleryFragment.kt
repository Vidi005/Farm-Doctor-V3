package com.android.farmdoctor.ui.detection

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.factory.model.Detection
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.databinding.FragmentImportGalleryBinding
import com.android.farmdoctor.helper.BitmapToByteArrayHelper.bitmapToByteArrayConverter
import com.android.farmdoctor.helper.CropToSquaredBitmapHelper.cropToSquaredBitmap
import com.android.farmdoctor.helper.DateHelper.getCurrentDate
import com.android.farmdoctor.preference.ImportGalleryPreference
import com.android.farmdoctor.ui.detection.adapter.ResultAdapter
import com.android.farmdoctor.ui.detection.callback.OnResultItemClickCallback
import com.android.farmdoctor.ui.disease.DetailPlantDiseaseFragment
import com.android.farmdoctor.viewmodel.DetectionViewModel
import com.android.farmdoctor.viewmodel.PlantDiseaseViewModelFactory
import com.android.farmdoctor.vo.Status
import com.bumptech.glide.Glide

class ImportGalleryFragment : Fragment(), View.OnClickListener, OnResultItemClickCallback {

    private lateinit var fragmentImportGalleryBinding: FragmentImportGalleryBinding
    private lateinit var resultAdapter: ResultAdapter
    private lateinit var viewModel: DetectionViewModel
    private lateinit var importGalleryPreference: ImportGalleryPreference
    companion object {
        private const val REQUEST_GALLERY_CODE = 130
    }
    private var bitmap: Bitmap? = null
    private var startTime = 0L
    private var endTime = 0L
    private var latency = 0L
    private var isDetectionHistoryEnabled = false
    private val list = ArrayList<Detection>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentImportGalleryBinding =
            FragmentImportGalleryBinding.inflate(layoutInflater, container, false)
        return fragmentImportGalleryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getViewModelLiveData()
        getBitmapData()
        importGalleryPreference = ImportGalleryPreference()
        fragmentImportGalleryBinding.apply {
            btnImport.setOnClickListener(this@ImportGalleryFragment)
            cbEnableHistory.setOnClickListener(this@ImportGalleryFragment)
        }
        enableDetectionHistory()
        showListResult()
    }

    override fun onClick(v: View?) {
        if (v == fragmentImportGalleryBinding.btnImport) {
            val callGalleryIntent = Intent(Intent.ACTION_PICK)
            callGalleryIntent.type = "image/*"
            requireParentFragment().startActivityForResult(callGalleryIntent, REQUEST_GALLERY_CODE)
            parentFragment
                ?.activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (v == fragmentImportGalleryBinding.cbEnableHistory) {
            if (fragmentImportGalleryBinding.cbEnableHistory.isChecked) {
                isDetectionHistoryEnabled = true
                importGalleryPreference.setDetectionHistory(requireActivity())
            } else {
                isDetectionHistoryEnabled = false
                importGalleryPreference.unsetDetectionHistory(requireActivity())
            }
            enableDetectionHistory()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY_CODE) {
            if (data != null) {
                val uri = data.data
                @Suppress("DEPRECATION")
                bitmap = if (Build.VERSION.SDK_INT < 29)
                    cropToSquaredBitmap(MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri))
                else {
                    val bitmapData =
                        uri?.let { ImageDecoder.createSource(requireActivity().contentResolver, it) }
                    cropToSquaredBitmap(ImageDecoder.decodeBitmap(bitmapData as ImageDecoder.Source))
                }
                try {
                    Glide.with(this).load(bitmap).into(fragmentImportGalleryBinding.ivPicture)
                    viewModel.getBitmap(bitmap as Bitmap)
                    startTime = SystemClock.uptimeMillis()
                    bitmap?.let { viewModel.setRecognizeImage(it) }
                } catch (e: Exception) {
                    Toast.makeText(activity, "${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(activity, "Gallery can't be Accessed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onItemClicked(data: Detection) = setSelectedPlantDisease(data)

    private fun enableDetectionHistory() {
        fragmentImportGalleryBinding.cbEnableHistory.isChecked =
            importGalleryPreference.getDetectionHistory(requireActivity())
        isDetectionHistoryEnabled = fragmentImportGalleryBinding.cbEnableHistory.isChecked
    }

    private fun getBitmapData() {
        viewModel.bitmap.observe(viewLifecycleOwner, {
            if (it != null) {
                bitmap = it
                Glide.with(this).load(it).into(fragmentImportGalleryBinding.ivPicture)
            }
        })
    }

    private fun getViewModelLiveData() {
        if (parentFragment != null) {
            val factory = PlantDiseaseViewModelFactory.getInstance(requireActivity())
            viewModel = ViewModelProvider(this, factory)[DetectionViewModel::class.java]
            viewModel.getRecognizeImage().observe(viewLifecycleOwner, {
                if (it != null) when (it.status) {
                    Status.LOADING -> stateLoadingProgress(true)
                    Status.SUCCESS -> {
                        endTime = SystemClock.uptimeMillis()
                        showDetectionItems(it.data)
                        latency = endTime - startTime
                        stateLoadingProgress(false)
                        Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show()
                        fragmentImportGalleryBinding.groupBarImportGallery.visibility = View.VISIBLE
                        val detectionItems = it.data
                        detectionItems?.sortByDescending { detection -> detection.accuracy }
                        val isAccSame = viewModel.checkAcc("${detectionItems?.get(0)}")
                        if (isDetectionHistoryEnabled && !isAccSame && latency < 1000)
                            addToDetectionHistory()
                    }
                    Status.ERROR ->
                        Toast.makeText(requireActivity(), "${it.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun showDetectionItems(detectionItems: ArrayList<Detection>?) {
        resultAdapter.setResultDetectionData(detectionItems as ArrayList<Detection>)
    }

    private fun showListResult() {
        resultAdapter = ResultAdapter(list, this)
        with(fragmentImportGalleryBinding.rvDetectionResult) {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = resultAdapter
        }
    }

    private fun addToDetectionHistory() {
        val items = viewModel.getRecognizeImage().value?.data
        items?.sortByDescending { it.accuracy }
        val detectionHistory = DetectionHistoryEntity(0,
            bitmapToByteArrayConverter(cropToSquaredBitmap(bitmap as Bitmap)),
            "${items?.get(0)?.name}",
            "${items?.get(0)?.accuracy}",
            getCurrentDate(),
            "$latency",
            "",
            "",
            "Gallery")
        viewModel.addToDetectionHistory(detectionHistory)
    }

    private fun setSelectedPlantDisease(data: Detection) {
        val mBundle = Bundle().apply {
            putString(DetailPlantDiseaseFragment.EXTRA_NAME, data.name)
            data.picture?.let { putInt(DetailPlantDiseaseFragment.EXTRA_PICTURE, it) }
            putInt(DetailPlantDiseaseFragment.EXTRA_DETAIL, data.detail as Int)
        }
        NavHostFragment
            .findNavController(this)
            .navigate(R.id.action_detectionFragment_to_detailPlantDiseaseFragment, mBundle)
    }

    private fun stateLoadingProgress(state: Boolean) {
        if (state) with(fragmentImportGalleryBinding) {
            groupProcess.visibility = View.VISIBLE
            pbProcess.visibility = View.VISIBLE
            tvProcess.visibility = View.VISIBLE
        } else fragmentImportGalleryBinding.apply {
            groupProcess.visibility = View.GONE
            pbProcess.visibility = View.GONE
            tvProcess.visibility = View.GONE
        }
    }
}