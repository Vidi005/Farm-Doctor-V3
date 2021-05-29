package com.android.farmdoctor.ui.history

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.farmdoctor.R
import com.android.farmdoctor.data.source.local.entity.DetectionHistoryEntity
import com.android.farmdoctor.databinding.FragmentDetectionHistoryBinding
import com.android.farmdoctor.helper.FilterUtils.CAMERA_ONLY
import com.android.farmdoctor.helper.FilterUtils.DATE_ASC
import com.android.farmdoctor.helper.FilterUtils.DATE_DESC
import com.android.farmdoctor.helper.FilterUtils.FILE_ONLY
import com.android.farmdoctor.helper.FilterUtils.NAME_ASC
import com.android.farmdoctor.helper.FilterUtils.NAME_DESC
import com.android.farmdoctor.preference.FilterPreference
import com.android.farmdoctor.ui.history.DetailDetectionHistoryFragment.Companion.EXTRA_DETECTION_ID
import com.android.farmdoctor.ui.history.adapter.DetectionHistoriesAdapter
import com.android.farmdoctor.ui.history.callback.OnHistoryItemClickCallback
import com.android.farmdoctor.viewmodel.DetectionHistoryViewModel
import com.android.farmdoctor.viewmodel.DetectionHistoryViewModelFactory
import com.android.farmdoctor.vo.Status
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class DetectionHistoriesFragment : Fragment(), OnHistoryItemClickCallback, OnQueryTextListener {

    companion object {
        private const val SWIPE_LEFT_CODE = 10
    }
    private lateinit var fragmentDetectionHistoryBinding: FragmentDetectionHistoryBinding
    private lateinit var detectionHistoriesAdapter: DetectionHistoriesAdapter
    private lateinit var filterPreference: FilterPreference
    private lateinit var viewModel: DetectionHistoryViewModel
    private var list = ArrayList<DetectionHistoryEntity>()
    private var detectionHistory: DetectionHistoryEntity? = null
    private var menu: Menu? = null
    private var filter = NAME_ASC
    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder): Int =
                makeMovementFlags(0, ItemTouchHelper.LEFT)

        override fun onMove(recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder): Boolean = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (this@DetectionHistoriesFragment.isVisible) {
                val swipedPosition = viewHolder.layoutPosition
                detectionHistory = detectionHistoriesAdapter.getSwipedData(swipedPosition)
                showAlertDialog(SWIPE_LEFT_CODE)
            }
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentDetectionHistoryBinding =
            FragmentDetectionHistoryBinding.inflate(layoutInflater, container, false)
        return fragmentDetectionHistoryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActionBar()
        setHasOptionsMenu(true)
        itemTouchHelper.attachToRecyclerView(fragmentDetectionHistoryBinding.rvDetectionHistory)
        showDetectionHistoriesList()
        filterPreference = FilterPreference(requireActivity())
        filter = filterPreference.getFilterBy()
        loadFilterPref()
        val factory = DetectionHistoryViewModelFactory.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, factory)[DetectionHistoryViewModel::class.java]
        getViewModelLiveData(filterPreference.getFilterBy())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detection_history, menu)
        this.menu = menu
        showSearchResult(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onNavigateUp(item.itemId)
        showAlertDialog(item.itemId)
        filterItems(item.itemId)
        item.isChecked = true
        filterPreference.setFilterBy(filter)
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClicked(data: DetectionHistoryEntity) {
        val mBundle = Bundle().apply { putInt(EXTRA_DETECTION_ID, data.id) }
        NavHostFragment
            .findNavController(this)
            .navigate(R.id.action_detectionHistoriesFragment_to_detailDetectionHistoryFragment, mBundle)
        closeKeyboard()
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(newText: String?): Boolean {
        getSearchQueryData(newText.toString())
        if (newText.isNullOrEmpty()) getViewModelLiveData(filter)
        return true
    }

    private fun setActionBar() {
        (requireActivity() as AppCompatActivity?)?.supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = "Detection Histories"
        }
    }

    private fun onNavigateUp(itemId: Int) {
        if (itemId == android.R.id.home) {
            requireActivity().onBackPressed()
            closeKeyboard()
        }
    }

    private fun filterItems(itemId: Int) {
        when (itemId) {
            R.id.app_bar_sort_by_name -> filter = NAME_ASC
            R.id.app_bar_sort_by_name_desc -> filter = NAME_DESC
            R.id.app_bar_sort_by_date -> filter = DATE_ASC
            R.id.app_bar_sort_by_date_desc -> filter = DATE_DESC
            R.id.app_bar_show_from_cam -> filter = CAMERA_ONLY
            R.id.app_bar_show_from_file -> filter = FILE_ONLY
        }
        getViewModelLiveData(filter)
    }

    private fun loadFilterPref() {
        if (menu == null) return
        val menuItem: MenuItem?
        when (filter) {
            NAME_ASC -> {
                menuItem = menu?.findItem(R.id.app_bar_sort_by_name)
                menuItem?.isChecked = true
            }
            NAME_DESC -> {
                menuItem = menu?.findItem(R.id.app_bar_sort_by_name_desc)
                menuItem?.isChecked = true
            }
            DATE_ASC -> {
                menuItem = menu?.findItem(R.id.app_bar_sort_by_date)
                menuItem?.isChecked = true
            }
            DATE_DESC -> {
                menuItem = menu?.findItem(R.id.app_bar_sort_by_date_desc)
                menuItem?.isChecked = true
            }
            CAMERA_ONLY -> {
                menuItem = menu?.findItem(R.id.app_bar_show_from_cam)
                menuItem?.isChecked = true
            }
            FILE_ONLY -> {
                menuItem = menu?.findItem(R.id.app_bar_show_from_file)
                menuItem?.isChecked = true
            }
        }
    }

    private fun showAlertDialog(showAlertDialog: Int) {
        if (this.isVisible) {
            if (showAlertDialog == R.id.app_bar_delete_all) {
                val dialogTitle = "Delete All Histories"
                val dialogMessage = "Are you sure want to delete all histories?"
                val alertDialogBuilder = AlertDialog.Builder(requireActivity())
                alertDialogBuilder.setTitle(dialogTitle)
                alertDialogBuilder
                    .setMessage(dialogMessage)
                    .setPositiveButton("Yes") { _, _ -> deleteAllHistories() }
                    .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            } else if (showAlertDialog == SWIPE_LEFT_CODE) {
                val dialogTitle = "Delete History"
                val dialogMessage = "Are you sure want to delete this history?"
                val alertDialogBuilder = AlertDialog.Builder(requireActivity())
                alertDialogBuilder.setTitle(dialogTitle)
                alertDialogBuilder
                    .setMessage(dialogMessage)
                    .setPositiveButton("Yes") { _, _ ->
                        if (detectionHistory != null) {
                            deleteDetectionHistory(detectionHistory as DetectionHistoryEntity)
                            Snackbar.make(view as View, "Undo Delete?", Snackbar.LENGTH_LONG).apply {
                                setAction("OK") {
                                    viewModel.addToDetectionHistory(detectionHistory as DetectionHistoryEntity)
                                    getViewModelLiveData(filter)
                                }
                                show()
                            }
                        }
                        else getViewModelLiveData(filter)
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.cancel()
                        getViewModelLiveData(filter)
                    }
                    .setCancelable(false)
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }
    }

    private fun showDetectionHistoriesList() {
        detectionHistoriesAdapter = DetectionHistoriesAdapter(this)
        with(fragmentDetectionHistoryBinding.rvDetectionHistory) {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = detectionHistoriesAdapter
        }
    }

    private fun getViewModelLiveData(filter: String) {
        if (activity != null) {
            viewModel.getDetectionHistories(filter).observe(viewLifecycleOwner, {
                if (it != null) when (it.status) {
                    Status.LOADING -> {
                        fragmentDetectionHistoryBinding.tvSavedHistories.text = "Loading Data..."
                        showLoadingState(true)
                    }
                    Status.SUCCESS -> {
                        list.apply {
                            clear()
                            addAll(it.data as PagedList)
                        }
                        showDetectionHistoryItems(it.data)
                        showLoadingState(false)
                    }
                    Status.ERROR -> {
                        showLoadingState(false)
                        Snackbar.make(requireView(), "${it.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    private fun getSearchQueryData(query: String) {
        viewModel.getSearchQuery("%$query%").observe(viewLifecycleOwner, {
            if (it != null) when (it.status) {
                Status.LOADING -> {
                    fragmentDetectionHistoryBinding.tvSavedHistories.text = "Loading Data..."
                    showLoadingState(true)
                }
                Status.SUCCESS -> {
                    showDetectionHistoryItems(it.data)
                    showLoadingState(false)
                }
                Status.ERROR -> {
                    showLoadingState(false)
                    Snackbar.make(requireView(), "${it.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun showDetectionHistoryItems(detectionHistoryItems: PagedList<DetectionHistoryEntity>?) {
        detectionHistoriesAdapter.apply {
            submitList(detectionHistoryItems)
            notifyDataSetChanged()
        }
        with(fragmentDetectionHistoryBinding) {
            if (!detectionHistoryItems.isNullOrEmpty())
                tvSavedHistories.text =
                    StringBuilder("Saved Detection Results: ${detectionHistoryItems.lastIndex + 1}")
            else tvSavedHistories.text = "No Detection History was Saved"
        }
    }

    private fun showSearchResult(menu: Menu) {
        val searchItem = menu.findItem(R.id.app_bar_search)
        (searchItem.actionView as SearchView).apply {
            queryHint = "Search Name"
            isSubmitButtonEnabled = true
            setOnQueryTextListener(this@DetectionHistoriesFragment)
        }
    }

    private fun deleteAllHistories() {
        if (!list.isNullOrEmpty()) {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val deferredDeleteAll = async(Dispatchers.IO) { viewModel.deleteAll(list) }
                    deferredDeleteAll.await()
                    showDetectionHistoryItems(null)
                    getViewModelLiveData(filter)
                    Snackbar.make(requireView(),
                        "All histories successfully deleted", Snackbar.LENGTH_SHORT).apply {
                        setAction("UNDO") {
                            viewModel.addAllDetectionHistories(list)
                            getViewModelLiveData(filter)
                        }
                        show()
                    }
                } catch (e: Exception) {
                    Snackbar.make(requireView(), "${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        } else if (list.size == 0)
            Snackbar.make(requireView(), "All histories have been deleted", Snackbar.LENGTH_SHORT).show()
        else Snackbar.make(requireView(), "Failed to delete all histories!", Snackbar.LENGTH_LONG).show()
        showLoadingState(false)
    }

    private fun deleteDetectionHistory(detectionHistory: DetectionHistoryEntity) =
        viewModel.deleteDetectionHistory(detectionHistory)

    private fun showLoadingState(state: Boolean) {
        if (state) fragmentDetectionHistoryBinding.pbHistory.visibility = View.VISIBLE
        else fragmentDetectionHistoryBinding.pbHistory.visibility = View.GONE
    }

    private fun closeKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}