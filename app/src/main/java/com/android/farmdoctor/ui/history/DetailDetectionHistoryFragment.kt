package com.android.farmdoctor.ui.history

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageView.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.data.source.remote.service.LocationAddress
import com.android.farmdoctor.databinding.FragmentDetailDetectionHistoryBinding
import com.android.farmdoctor.manager.PermissionManager
import com.android.farmdoctor.viewmodel.DetailDetectionHistoryViewModel
import com.android.farmdoctor.viewmodel.DetectionHistoryViewModelFactory
import com.android.farmdoctor.viewmodel.LocationAddressViewModel
import com.android.farmdoctor.vo.Status
import com.bumptech.glide.Glide

class DetailDetectionHistoryFragment : Fragment() {

    companion object {
        const val EXTRA_DETECTION_ID = "extra_detection_id"
        private const val REQUEST_READ_EXTERNAL_STORAGE = 140
        private const val REQUEST_WRITE_EXTERNAL_STORAGE = 150
    }
    private lateinit var fragmentDetailDetectionHistoryBinding: FragmentDetailDetectionHistoryBinding
    private lateinit var detailDetectionHistoryViewModel: DetailDetectionHistoryViewModel
    private lateinit var locationAddressViewModel: LocationAddressViewModel
    private var canReadExtStorage = false
    private var canWriteExtStorage = false
    private var detectionHistory: DetectionHistoryEntity? = null
    private var detectionId = -1
    private var dataImage: ByteArray? = null
    private var dataLatitude = ""
    private var dataLongitude = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentDetailDetectionHistoryBinding =
            FragmentDetailDetectionHistoryBinding.inflate(layoutInflater, container, false)
        return fragmentDetailDetectionHistoryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBar()
        setHasOptionsMenu(true)
        checkStoragePermission()
        getSelectedDetectionHistory()
        getDetailViewModelLiveData()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) canReadExtStorage = true
            else {
                canReadExtStorage = false
                Toast.makeText(activity, "Can't Access Storage!", Toast.LENGTH_LONG).show()
                checkStoragePermission()
            }
        }
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) canWriteExtStorage = true
            else {
                canWriteExtStorage = false
                Toast.makeText(activity, "Can't Write to Memory!", Toast.LENGTH_LONG).show()
                checkStoragePermission()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail_detection_history, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onNavigateUp(item.itemId)
        showAlertDialog(item.itemId)
        shareImageResult(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    private fun setActionBar() {
        (requireActivity() as? AppCompatActivity?)?.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Detail Detection History"
        }
    }

    private fun onNavigateUp(itemId: Int) {
        if (itemId == android.R.id.home) requireActivity().onBackPressed()
    }

    private fun checkStoragePermission() {
        PermissionManager.apply {
            check(requireActivity(),
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE,
                REQUEST_READ_EXTERNAL_STORAGE)
            check(requireActivity(),
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                REQUEST_WRITE_EXTERNAL_STORAGE)
        }

    }

    private fun getSelectedDetectionHistory() {
        if (arguments != null) detectionId = arguments?.getInt(EXTRA_DETECTION_ID, -1) as Int
    }

    private fun getDetailViewModelLiveData() {
        if (parentFragment != null) {
            val factory = DetectionHistoryViewModelFactory.getInstance(requireActivity())
            detailDetectionHistoryViewModel =
                ViewModelProvider(this, factory)[DetailDetectionHistoryViewModel::class.java]
            detailDetectionHistoryViewModel
                .getDetailDetectionHistoryById(detectionId).observe(viewLifecycleOwner, {
                if (it != null) {
                    when (it.status) {
                        Status.LOADING -> showLoadingState(true)
                        Status.SUCCESS -> {
                            detectionHistory = it.data
                            populateDetailDetectionHistory(it.data as DetectionHistoryEntity)
                            showLoadingState(false)
                        }
                        Status.ERROR -> {
                            showLoadingState(false)
                            Toast.makeText(activity, "${it.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
        }
    }

    private fun getLocationAddressLiveData() {
        val factory = DetectionHistoryViewModelFactory.getInstance(requireActivity())
        locationAddressViewModel =
            ViewModelProvider(this, factory)[LocationAddressViewModel::class.java]
        locationAddressViewModel.apply {
            geocodeLocationAddress(dataLatitude.toDouble(), dataLongitude.toDouble())
            getLocationAddress().observe(viewLifecycleOwner, {
                if (it != null) {
                    when (it.status) {
                        Status.LOADING -> {}
                        Status.SUCCESS -> showLocationAddress(it.data as LocationAddress)
                        Status.ERROR ->
                            Toast.makeText(activity, "${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    private fun showLocationAddress(locationAddress: LocationAddress) =
        if (locationAddress.address.isNullOrBlank())
            fragmentDetailDetectionHistoryBinding.tvHistoryLocationReceived.text =
                StringBuilder("$dataLatitude, $dataLongitude")
        else fragmentDetailDetectionHistoryBinding.tvHistoryLocationReceived.text =
                locationAddress.address

    private fun populateDetailDetectionHistory(detailDetectionHistory: DetectionHistoryEntity) {
        fragmentDetailDetectionHistoryBinding.apply {
            dataImage = detailDetectionHistory.image
            Glide.with(this@DetailDetectionHistoryFragment)
                .load(dataImage).into(ivHistoryImageReceived)
            if (detailDetectionHistory.detectionBased == "Camera")
                ivDetectionBasedReceived.setImageResource(R.drawable.ic_camera)
            else ivDetectionBasedReceived.setImageResource(R.drawable.ic_gallery)
            tvHistoryNameReceived.text = StringBuilder("Name: ${detailDetectionHistory.name}")
            tvHistoryAccReceived.text =
                StringBuilder("Probability: ${detailDetectionHistory.accuracy}%")
            tvHistoryDateReceived.text = detailDetectionHistory.date
            tvHistoryLatencyReceived.text = StringBuilder("${detailDetectionHistory.latency} ms")
            dataLatitude = "${detailDetectionHistory.latitude}"
            dataLongitude = "${detailDetectionHistory.longitude}"
            if ((dataLatitude == "Disabled" && dataLongitude == "Disabled") ||
                detailDetectionHistory.detectionBased == "Gallery")
                    groupLocation.visibility = View.GONE
            else if (dataLatitude.isEmpty() || dataLongitude.isEmpty()) {
                tvHistoryLocationReceived.text = "Location Not Available"
                ivHistoryImageReceived.scaleType = ScaleType.FIT_CENTER
            } else {
                ivHistoryImageReceived.scaleType = ScaleType.FIT_CENTER
                tvHistoryLocationReceived.text = StringBuilder("$dataLatitude, $dataLongitude")
                getLocationAddressLiveData()
            }
        }
    }

    private fun shareImageResult(sharedImage: Int) {
        if (sharedImage == R.id.app_bar_share) {
            val shareImage = BitmapFactory.decodeByteArray(dataImage, 0, dataImage?.size as Int)
            val shareIntent = Intent(Intent.ACTION_SEND)
//            val values = ContentValues().apply {
//                put(MediaStore.Images.Media.TITLE, "${detectionHistory?.name}")
//                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//            }
//            val uri = requireParentFragment()
//                    .requireActivity()
//                    .contentResolver
//                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//            val outputStream: OutputStream?
//            try {
//                outputStream = requireParentFragment()
//                        .requireActivity()
//                        .contentResolver
//                        .openOutputStream(uri as Uri)
//                shareImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
//                outputStream?.close()
//            } catch (e: Exception) {
//                Toast.makeText(activity, "${e.message}", Toast.LENGTH_LONG).show()
//            }
            @Suppress("DEPRECATION")
            val path = MediaStore.Images.Media.insertImage(
                    requireActivity().contentResolver,
                    shareImage,
                    "${detectionHistory?.name}",
                    null)
            val uri = Uri.parse(path)
            shareIntent.type = "image/jpeg"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            requireParentFragment().startActivity(Intent.createChooser(shareIntent, "Share Using"))
        }
    }

    private fun showAlertDialog(showAlertDialog: Int) {
        if (this.isVisible) if (showAlertDialog == R.id.app_bar_delete) {
            val dialogTitle = "Delete History"
            val dialogMessage = "Are you sure want to delete this history?"
            val alertDialogBuilder = AlertDialog.Builder(requireActivity())
            alertDialogBuilder.setTitle(dialogTitle)
            alertDialogBuilder
                .setMessage(dialogMessage)
                .setPositiveButton("Yes") { _, _ -> deleteDetectionHistory() }
                .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    private fun showLoadingState(state: Boolean) {
        if (state) fragmentDetailDetectionHistoryBinding.pbDetailHistory.visibility = View.VISIBLE
        else fragmentDetailDetectionHistoryBinding.pbDetailHistory.visibility = View.GONE
    }

    private fun deleteDetectionHistory() {
        if (detectionId != -1 || detectionHistory != null) {
            detectionHistory?.let { detailDetectionHistoryViewModel.deleteDetectionHistory(it) }
            requireActivity().onBackPressed()
            Toast.makeText(activity, "History successfully deleted", Toast.LENGTH_SHORT).show()
        } else Toast.makeText(activity, "Failed to delete the history!", Toast.LENGTH_LONG).show()
    }
}