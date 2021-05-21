package com.android.farmdoctor.ui.detection

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.factory.model.Detection
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.databinding.FragmentCameraBinding
import com.android.farmdoctor.helper.DateHelper.getCurrentDate
import com.android.farmdoctor.manager.PermissionManager
import com.android.farmdoctor.preference.CameraPreference
import com.android.farmdoctor.ui.detection.adapter.ResultAdapter
import com.android.farmdoctor.ui.detection.callback.OnResultItemClickCallback
import com.android.farmdoctor.ui.disease.DetailPlantDiseaseFragment.Companion.EXTRA_DETAIL
import com.android.farmdoctor.ui.disease.DetailPlantDiseaseFragment.Companion.EXTRA_NAME
import com.android.farmdoctor.ui.disease.DetailPlantDiseaseFragment.Companion.EXTRA_PICTURE
import com.android.farmdoctor.viewmodel.*
import com.android.farmdoctor.vo.Status
import com.priyankvasa.android.cameraviewex.Modes
import kotlinx.coroutines.*

class CameraFragment : Fragment(), View.OnClickListener, OnResultItemClickCallback {

    private lateinit var fragmentCameraBinding: FragmentCameraBinding
    private lateinit var resultAdapter: ResultAdapter
    private lateinit var detectionViewModel: DetectionViewModel
    private lateinit var locationHelperViewModel: LocationHelperViewModel
    private lateinit var cameraPreference: CameraPreference
    companion object {
        private const val REQUEST_CAMERA_CODE = 100
        private const val REQUEST_FINE_LOCATION_CODE = 110
        private const val REQUEST_COARSE_LOCATION_CODE = 120
        var flashModes: Int = 1
    }
    private var dataImage: ByteArray? = null
    private var startTime = 0L
    private var endTime = 0L
    private var firstTime = 0L
    private var lastTime = 0L
    private var latency = 0L
    private var inferenceTime = 0L
    private var latitude = ""
    private var longitude = ""
    private val list = ArrayList<Detection>()
    private var isButtonClicked = false
    private var canUseCamera = true
    private var canAccessFineLocation = true
    private var canAccessCoarseLocation = true
    private var isDetectionHistoryEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentCameraBinding = FragmentCameraBinding.inflate(layoutInflater, container, false)
        return fragmentCameraBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBar()
        checkCameraPermission()
        setCamera()
        showListResult()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) canUseCamera = true
            else {
                canUseCamera = false
                Toast.makeText(activity, "Can't Use Camera!", Toast.LENGTH_LONG).show()
                checkCameraPermission()
            }
        }
        if (requestCode == REQUEST_FINE_LOCATION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) canAccessFineLocation = true
            else {
                canAccessFineLocation = false
                Toast.makeText(activity, "Can't Access Fine Location!", Toast.LENGTH_LONG).show()
                checkLocationPermissions()
            }
        }
        if (requestCode == REQUEST_COARSE_LOCATION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) canAccessCoarseLocation = true
            else {
                canAccessCoarseLocation = false
                Toast.makeText(activity, "Can't Access Coarse Location!", Toast.LENGTH_LONG).show()
                checkLocationPermissions()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onNavigateUp(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        try {
            super.onResume()
            fragmentCameraBinding.camera.start()
            canUseCamera = true
        } catch (e: Exception) {
            canUseCamera = false
            Toast.makeText(activity, "Camera can't be accessed", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        fragmentCameraBinding.camera.stop()
        if (isButtonClicked) {
            isButtonClicked = false
            stateLoadingProgress(false)
        }
        super.onPause()
    }

    @SuppressLint("MissingPermission")
    override fun onClick(v: View?) {
        with(fragmentCameraBinding) {
            when (v) {
                btnDetect -> if (!isButtonClicked && canUseCamera) {
                    isButtonClicked = true
                    camera.capture()
                } else if (isButtonClicked) {
                    isButtonClicked = false
                    stateLoadingProgress(false)
                }
                ivFlashLightMode -> {
                    flashModes++
                    cameraPreference.setFlashLightMode(requireActivity())
                    setFlashModes()
                }
                cbEnableHistory -> {
                    if (cbEnableHistory.isChecked) {
                        isDetectionHistoryEnabled = true
                        cameraPreference.setDetectionHistory(requireActivity())
                    } else {
                        isDetectionHistoryEnabled = false
                        cameraPreference.unsetDetectionHistory(requireActivity())
                    }
                    enableDetectionHistory()
                }
            }
        }
    }

    override fun onItemClicked(data: Detection) {
        setSelectedPlantDisease(data)
    }

    private fun setActionBar() {
        (requireActivity() as? AppCompatActivity?)?.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Diagnose Plant Diseases"
        }
    }

    private fun onNavigateUp(itemId: Int) {
        if (itemId == android.R.id.home) activity?.onBackPressed()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setCamera() {
        if (!canUseCamera) checkCameraPermission()
        else {
            cameraPreference = CameraPreference()
            fragmentCameraBinding.apply {
                btnDetect.setOnClickListener(this@CameraFragment)
                ivFlashLightMode.setOnClickListener(this@CameraFragment)
                cbEnableHistory.setOnClickListener(this@CameraFragment)
            }
            setFlashModes()
            setDetectionMode()
            loadDetectionMode()
            enableLocation()
            loadLocationPrefs()
            enableDetectionHistory()
            getViewModelLiveData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setDetectionMode() {
        fragmentCameraBinding.swDetectionMode.apply {
            isChecked = cameraPreference.getAutoDetect(requireActivity())
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) cameraPreference.setAutoDetect(requireActivity())
                else cameraPreference.unsetAutoDetect(requireActivity())
                loadDetectionMode()
                getViewModelLiveData()
            }
        }
    }

    private fun loadDetectionMode() {
        with(fragmentCameraBinding) {
            swDetectionMode.isChecked = cameraPreference.getAutoDetect(requireActivity())
            if (swDetectionMode.isChecked) {
                camera.apply {
                    disableCameraMode(Modes.CameraMode.SINGLE_CAPTURE)
                    setCameraMode(Modes.CameraMode.CONTINUOUS_FRAME)
                }
                btnDetect.visibility = View.GONE
                groupBar2.visibility = View.GONE
                tvEnableLocation.visibility = View.GONE
                swEnableLocation.visibility = View.GONE
                tvEnableHistory.visibility = View.GONE
                cbEnableHistory.visibility = View.GONE
                groupBar3.visibility = View.VISIBLE
            } else {
                camera.apply {
                    disableCameraMode(Modes.CameraMode.CONTINUOUS_FRAME)
                    setCameraMode(Modes.CameraMode.SINGLE_CAPTURE)
                }
                groupBar3.visibility = View.GONE
                btnDetect.visibility = View.VISIBLE
                groupBar2.visibility = View.VISIBLE
                tvEnableLocation.visibility = View.VISIBLE
                swEnableLocation.visibility = View.VISIBLE
                tvEnableHistory.visibility = View.VISIBLE
                cbEnableHistory.visibility = View.VISIBLE
            }
        }
    }

    private fun enableLocation() {
        if (!fragmentCameraBinding.swEnableLocation.isChecked)
            fragmentCameraBinding.swEnableLocation.apply {
            cameraPreference.getLocation(requireActivity())
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkLocationPermissions()
                    cameraPreference.setLocation(requireActivity())
                } else cameraPreference.unsetLocation(requireActivity())
                loadLocationPrefs()
            }
        }
    }

    private fun checkCameraPermission() =
        PermissionManager.check(requireActivity(),
            requireActivity(),
            Manifest.permission.CAMERA, REQUEST_CAMERA_CODE)

    private fun checkLocationPermissions() {
        PermissionManager.check(requireActivity(),
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION_CODE)
        PermissionManager.check(requireActivity(),
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_COARSE_LOCATION_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getViewModelLiveData() {
        if (activity != null) {
            fragmentCameraBinding.swDetectionMode.isChecked =
                cameraPreference.getAutoDetect(activity as Activity)
            val factory = PlantDiseaseViewModelFactory.getInstance(requireActivity())
            detectionViewModel = ViewModelProvider(this, factory)[DetectionViewModel::class.java]
            if (fragmentCameraBinding.swDetectionMode.isChecked) {
                detectPreviewFrames()
                detectionViewModel.getRecognizePreviewFrames().observe(viewLifecycleOwner, {
                    if (it != null) when (it.status) {
                        Status.LOADING -> {}
                        Status.SUCCESS -> {
                            showDetectionItems(it.data as ArrayList<Detection>)
                            lastTime = SystemClock.uptimeMillis()
                            inferenceTime = lastTime - firstTime
                            fragmentCameraBinding.tvLatency.text = StringBuilder("$inferenceTime ms")
                        }
                        Status.ERROR ->
                            Toast.makeText(activity, "${it.message}", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                detectTakenPicture()
                detectionViewModel.getRecognizeTakenPicture().observe(viewLifecycleOwner, {
                    if (it != null) when (it.status) {
                        Status.LOADING -> stateLoadingProgress(true)
                        Status.SUCCESS -> {
                            endTime = SystemClock.uptimeMillis()
                            showDetectionItems(it.data as ArrayList<Detection>)
                            latency = endTime - startTime
                            Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show()
                            stateLoadingProgress(false)
                            isButtonClicked = false
                            val detectionItems = it.data
                            detectionItems.sortByDescending { detection -> detection.accuracy }
                            val isAccSame = detectionViewModel.checkAcc("${detectionItems[0]}")
                            if (isDetectionHistoryEnabled && !isAccSame && latency < 2000)
                                addToDetectionHistory()
                        }
                        Status.ERROR -> {
                            stateLoadingProgress(false)
                            Toast.makeText(activity, "${it.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                })
            }
        }
    }

    private fun addToDetectionHistory() {
        val items = detectionViewModel.getRecognizeTakenPicture().value?.data
        items?.sortByDescending { it.accuracy }
        val detectionHistory = DetectionHistoryEntity(0,
            dataImage,
            "${items?.get(0)?.name}",
            "${items?.get(0)?.accuracy}",
            getCurrentDate(),
            "$latency",
            latitude,
            longitude,
            "Camera")
        detectionViewModel.addToDetectionHistory(detectionHistory)
    }

    private fun detectTakenPicture() {
        fragmentCameraBinding.camera.addPictureTakenListener {
            startTime = SystemClock.uptimeMillis()
            detectionViewModel.setRecognizeTakenPicture(it.data)
            dataImage = it.data
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun detectPreviewFrames() {
        fragmentCameraBinding.camera.setContinuousFrameListener(0f) {
            firstTime = SystemClock.uptimeMillis()
            detectionViewModel.setRecognizePreviewFrames(it)
        }
    }

    private fun showDetectionItems(detectionItems: ArrayList<Detection>) {
        resultAdapter.setResultDetectionData(detectionItems)
    }

    private fun setFlashModes() {
        flashModes = cameraPreference.getFlashLightMode(requireActivity())
        with(fragmentCameraBinding) {
            when (flashModes) {
                1 -> {
                    ivFlashLightMode.setImageResource(R.drawable.ic_flash_auto)
                    camera.flash = Modes.Flash.FLASH_AUTO
                }
                2 -> {
                    ivFlashLightMode.setImageResource(R.drawable.ic_flash_on)
                    camera.flash = Modes.Flash.FLASH_ON
                }
                3 -> {
                    ivFlashLightMode.setImageResource(R.drawable.ic_flash_off)
                    camera.flash = Modes.Flash.FLASH_OFF
                }
                4 -> {
                    ivFlashLightMode.setImageResource(R.drawable.ic_flash_torch)
                    camera.flash = Modes.Flash.FLASH_TORCH
                    flashModes = 0
                }
            }
        }
    }

    private fun loadLocationPrefs() {
        if (activity != null) {
            val locationManager =
                requireActivity().getSystemService(Activity.LOCATION_SERVICE) as LocationManager
            val factory = PlantDiseaseViewModelFactory.getInstance(requireActivity())
            locationHelperViewModel =
                ViewModelProvider(this, factory)[LocationHelperViewModel::class.java]
            fragmentCameraBinding.swEnableLocation.isChecked =
                cameraPreference.getLocation(requireActivity())
            if (fragmentCameraBinding.swEnableLocation.isChecked) {
                val isFineLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isCoarseLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (isFineLocationEnabled || isCoarseLocationEnabled) {
                    checkLocationPermissions()
                    fragmentCameraBinding.tvProcess.text = "Getting Location..."
                    stateLoadingProgress(true)
                    if (canAccessFineLocation || canAccessCoarseLocation)
                        GlobalScope.launch(Dispatchers.Default) {
                            locationHelperViewModel.getLastLocation()
                            delay(1000L)
                            with(locationHelperViewModel) {
                                latitude = "${setLastLocation().latitude}"
                                longitude = setLastLocation().longitude.toString()
                                withContext(Dispatchers.Main) {
                                    fragmentCameraBinding.tvProcess.text = "Processing..."
                                    stateLoadingProgress(false)
                                }
                            }
                    } else Toast.makeText(activity, "Can't Access Location", Toast.LENGTH_LONG).show()
                } else {
                    requireActivity().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    if (locationManager.isProviderEnabled(LocationManager.MODE_CHANGED_ACTION))
                        loadLocationPrefs()
                }
            } else {
                latitude = "Disabled"
                longitude = "Disabled"
            }
        }
    }

    private fun enableDetectionHistory() {
        if (!fragmentCameraBinding.swDetectionMode.isChecked) {
            fragmentCameraBinding.cbEnableHistory.isChecked =
                cameraPreference.getDetectionHistory(activity as Activity)
            isDetectionHistoryEnabled = fragmentCameraBinding.cbEnableHistory.isChecked
        }
    }

    private fun showListResult() {
        resultAdapter = ResultAdapter(list, this)
        with(fragmentCameraBinding.rvDetectionResult) {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = resultAdapter
        }
    }

    private fun setSelectedPlantDisease(data: Detection) {
        val mBundle = Bundle().apply {
            putString(EXTRA_NAME, data.name)
            data.picture?.let { putInt(EXTRA_PICTURE, it) }
            putInt(EXTRA_DETAIL, data.detail as Int)
        }
        NavHostFragment
            .findNavController(this)
            .navigate(R.id.action_detectionFragment_to_detailPlantDiseaseFragment, mBundle)
    }

    private fun stateLoadingProgress(state: Boolean) {
        if (state) with(fragmentCameraBinding) {
            groupProcess.visibility = View.VISIBLE
            pbProcess.visibility = View.VISIBLE
            tvProcess.visibility = View.VISIBLE
        } else fragmentCameraBinding.apply {
            groupProcess.visibility = View.GONE
            pbProcess.visibility = View.GONE
            tvProcess.visibility = View.GONE
        }
    }
}