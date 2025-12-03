package com.zybooks.inspobook.ui.fragment

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zybooks.inspobook.R
import com.zybooks.inspobook.adapter.UnsplashPhotoAdapter
import com.zybooks.inspobook.model.UnsplashPhoto
import com.zybooks.inspobook.network.UnsplashApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import android.hardware.SensorManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.sqrt
import androidx.lifecycle.lifecycleScope
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomnavigation.BottomNavigationView


class UnsplashSearchFragment : Fragment(),
    UnsplashPhotoAdapter.OnPhotoInteractionListener,
    SensorEventListener {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var sortButton: Button
    private lateinit var filterButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UnsplashPhotoAdapter

    private var currentQuery: String = "inspiration"
    private var currentOrderBy: String? = null // "relevant" or "latest"
    private var currentColor: String? = null   // e.g. "red", "blue", "green"

    // private val uiScope = CoroutineScope(Dispatchers.Main + Job())

    // shake detection
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var accelCurrent = 9.81f
    private var accelLast = 9.81f
    private var shake = 0f

    private var currentPage = 1
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_unsplash_search, container, false)
        val bottomNavView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        //set inspobooks page as starting page after login
        bottomNavView.selectedItemId = R.id.search

        searchEditText = v.findViewById(R.id.searchEditText)
        searchButton = v.findViewById(R.id.searchButton)
        sortButton = v.findViewById(R.id.sortButton)
        filterButton = v.findViewById(R.id.filterButton)
        recyclerView = v.findViewById(R.id.photosRecyclerView)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = UnsplashPhotoAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        // initial load
        performSearch(currentQuery)

        searchButton.setOnClickListener {
            val q = searchEditText.text.toString().trim()
            if (q.isNotEmpty()) {
                currentQuery = q
                performSearch(currentQuery)
            } else {
                Toast.makeText(requireContext(), "Enter a search term", Toast.LENGTH_SHORT).show()
            }
        }
        // pressing "enter" will trigger search
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
            val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN

            if (isSearchAction || isEnterKey) {
                val q = searchEditText.text.toString().trim()
                if (q.isNotEmpty()) {
                    currentQuery = q
                    performSearch(currentQuery)
                    hideKeyboard()
                } else {
                    Toast.makeText(requireContext(), "Enter a search term", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        sortButton.setOnClickListener { showSortDialog() }
        filterButton.setOnClickListener { showFilterDialog() }

        // shake detection setup
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        return v
    }

    // unsplash API calls

    private fun performSearch(query: String, reset: Boolean = true) {
        if (isLoading) return
        isLoading = true

        if (reset) {
            currentPage = 1
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = UnsplashApi.service.searchPhotos(
                    query = query,
                    page = currentPage,
                    perPage = 30,
                    orderBy = currentOrderBy,
                    color = currentColor
                )

                if (reset) {
                    adapter.updatePhotos(response.results)
                } else {
                    adapter.appendPhotos(response.results)
                }

                currentPage++
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error fetching images", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadRandomPhoto(count: Int = 6) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val photos = UnsplashApi.service.getRandomPhotos(
                    count = count
                )
                adapter.updatePhotos(photos)
                Toast.makeText(
                    requireContext(),
                    "Loaded random photos",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Error fetching random photos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



    // sort/filter dialogues

    private fun showSortDialog() {
        val options = arrayOf("Relevant", "Latest", "Clear")
        AlertDialog.Builder(requireContext())
            .setTitle("Sort by")
            .setItems(options) { _, which ->
                currentOrderBy = when (which) {
                    0 -> "relevant"
                    1 -> "latest"
                    else -> null
                }
                performSearch(currentQuery)
            }
            .show()
    }

    private fun showFilterDialog() {
        val colors = arrayOf("Any", "Black & White", "Red", "Orange", "Yellow", "Green", "Blue", "Purple")
        val apiValues = arrayOf<String?>(null, "black_and_white", "red", "orange", "yellow", "green", "blue", "purple")

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by color")
            .setItems(colors) { _, which ->
                currentColor = apiValues[which]
                performSearch(currentQuery)
            }
            .show()
    }

    // adapter callbacks

    override fun onPhotoClicked(photo: UnsplashPhoto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Unsplash Photo: ${photo.id}")
            .setMessage("Would you like to download this image?")
            .setPositiveButton("Save Image") { _, _ ->
                saveImageToDevice(photo)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    override fun onPhotoLongClicked(photo: UnsplashPhoto): Boolean {
        // none
        return true
    }


    // image download

    private fun saveImageToDevice(photo: UnsplashPhoto) {
        try {
            val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(photo.urls.full)

            val request = DownloadManager.Request(uri)
                .setTitle("InspoBook Unsplash Image")
                .setDescription("Downloading from Unsplash")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,
                    "InspoBook/${photo.id}.jpg"
                )

            dm.enqueue(request)
            Toast.makeText(requireContext(), "Download started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to start download", Toast.LENGTH_SHORT).show()
        }
    }

    // shake detection

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager?.registerListener(
                this,
                accel,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            accelLast = accelCurrent
            accelCurrent = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = accelCurrent - accelLast
            shake = shake * 0.9f + delta

            if (shake > 20) {
                loadRandomPhoto(count = 8)
            }

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // not used
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
