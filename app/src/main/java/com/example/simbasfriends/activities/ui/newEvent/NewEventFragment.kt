package com.example.simbasfriends.activities.ui.newEvent

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.simbasfriends.R
import com.example.simbasfriends.activities.dialogs.EditFieldDialog
import com.example.simbasfriends.activities.dialogs.LoadingDialog
import com.example.simbasfriends.databinding.FragmentNewEventBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.collection.LLRBNode.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewEventFragment : Fragment() {
    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel : NewEventViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView : MapView
    private lateinit var googleMap: GoogleMap
    private var currentMarker: Marker? = null
    private var selectedDate : String? = null
    private var selectedTime : String? = null
    private var selectedLocation : LatLng? = null
    private var selectedAddress : String = ""
    private var selectedImageUri: Uri? = null

    private var loadingDialog : LoadingDialog? = null

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }

        }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri.let {
                selectedImageUri = it
                binding.eventImagePreview.visibility = View.VISIBLE
                binding.eventImagePreview.setImageURI(it) // Set the selected image to the ImageView
                validateCreateEventButton()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewEventBinding.inflate(inflater,container,false)
        // Initialize the location of the client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            googleMap = map
            setupMap(LatLng(0.0,0.0))
        }
        return binding.root
    }

    private fun setupMap(userLatLng: LatLng) {
        googleMap.uiSettings.isZoomControlsEnabled = true
        //Remove previous marker if it exists
        currentMarker?.remove()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
        //Add new marker
        currentMarker = googleMap.addMarker(MarkerOptions().position(userLatLng).title("Selected Location"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[NewEventViewModel::class.java]
        initListeners()
        //Observe event creation
        viewModel.eventCreated.observe(viewLifecycleOwner){ isCreated ->
            if(isCreated){
                hideLoading()
                Toast.makeText(requireContext(), "Event created successfully", Toast.LENGTH_SHORT).show()
                resetForm()
                viewModel.resetEventCreationStatus()
            }
        }
    }

    private fun initListeners() {
        binding.selectDateBTN.setOnClickListener { showDatePicker() }
        binding.selectTimeBTN.setOnClickListener { showTimePicker() }
        binding.useCurrentLocationBTN.setOnClickListener { getCurrentLocation() }
        binding.chooseFromMapBTN.setOnClickListener { chooseFromMap() }
        binding.uploadEventImageBTN.setOnClickListener { selectImage() }
        binding.createEventBTN.setOnClickListener { createEvent() }
    }

    private fun createEvent() {
        val eventDescription = binding.eventDescriptionET.text.toString().trim()
        if(selectedDate == null || selectedTime == null || selectedLocation == null){
            Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading()
        viewModel.createEvent(
            eventDate = selectedDate!!,
            eventTime = selectedTime!!,
            eventLocation = selectedLocation!!,
            eventAddress = selectedAddress,
            eventDescription = eventDescription,
            imageUri = selectedImageUri
        )
    }

    private fun selectImage() {
       imagePickerLauncher.launch("image/*")
    }

    @SuppressLint("SuspiciousIndentation")
    private fun chooseFromMap() {
        val dialog = EditFieldDialog("address of the event")
            dialog.setOnFieldEditedListener { address ->
                if(address.isNotEmpty()) getCoordinatesFromAddress(address)
                else Toast.makeText(requireContext(), "Please enter an address", Toast.LENGTH_SHORT).show()
            }
        dialog.show(parentFragmentManager,"EditFieldDialog")
        validateCreateEventButton()
    }

    private fun getCoordinatesFromAddress(address: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try{
            val addresses = geocoder.getFromLocationName(address,1)
            if(addresses!!.isNotEmpty()){
                val latLng = LatLng(addresses[0].latitude,addresses[0].longitude)
                selectedLocation = latLng
                selectedAddress = address
                setupMap(latLng)
                validateCreateEventButton()
        }else{
            Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
            }
        }catch (e : Exception){
            Log.e("NewEventFragment", "Error getting coordinates from address", e)
        }
    }

    private fun getCurrentLocation() {
        //Second check for permission in case the user denied it once or changed the settings after grating
        if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if(location != null){
                val userLatLng = LatLng(location.latitude,location.longitude)
                val address = getAddressFromCoordinates(location.latitude,location.longitude)
                selectedLocation = userLatLng
                selectedAddress = address
                setupMap(userLatLng)
                validateCreateEventButton()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (addresses!!.isNotEmpty()) {
                addresses[0].getAddressLine(0) // Full address as a string
            } else {
                "Unknown Address"
            }
        } catch (e: Exception) {
            "Unknown Address"
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val isToday = selectedDate == SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
            Date()
        )

        val timePickerDialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            if (isToday && (selectedHour < currentHour || (selectedHour == currentHour && selectedMinute < currentMinute))) {
                Toast.makeText(requireContext(), "You cannot select a past time", Toast.LENGTH_SHORT).show()
            } else {
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                binding.selectedTimeTXT.text = formattedTime
                selectedTime = formattedTime
                validateCreateEventButton()
            }
    }, currentHour, currentMinute, true)
        timePickerDialog.show()
    }
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = String.format(Locale.getDefault(),"%02d/%02d/%d",selectedDay,selectedMonth+1,selectedYear)
            binding.selectedDateTXT.text = selectedDate
            validateCreateEventButton()
            binding.selectTimeBTN.isEnabled = true
            val enabledColor = ContextCompat.getColor(requireContext(), R.color.blue)
            binding.selectTimeBTN.setBackgroundColor(enabledColor)
        },year,month,day)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun validateCreateEventButton() {
        val isReady = selectedDate != null &&
                selectedTime != null &&
                selectedLocation != null &&
                selectedImageUri != null

        binding.createEventBTN.isEnabled = isReady
        val buttonColor = if (isReady) {
            ContextCompat.getColor(requireContext(), R.color.light_green) // Enabled color
        } else {
            ContextCompat.getColor(requireContext(), R.color.disabled_light_green) // Disabled color
        }
        binding.createEventBTN.setBackgroundColor(buttonColor)
    }

    fun showLoading(){
        if(loadingDialog == null){
            loadingDialog = LoadingDialog()
        }
        loadingDialog?.show(childFragmentManager, "LoadingDialog")
    }

    fun hideLoading(){
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun resetForm() {
        selectedDate = null
        selectedTime = null
        selectedLocation = null
        selectedAddress = ""
        selectedImageUri = null

        // Clear UI fields
        binding.selectedDateTXT.text = ""
        binding.selectedTimeTXT.text = ""
        binding.eventDescriptionET.text?.clear()
        binding.eventImagePreview.setImageDrawable(null)
        binding.eventImagePreview.visibility = View.GONE

        // Disable "Choose Time" button again
        binding.selectTimeBTN.isEnabled = false
        binding.selectTimeBTN.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.disabled_blue))

        // Disable the create event button until new input is provided
        binding.createEventBTN.isEnabled = false
        binding.createEventBTN.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.disabled_light_green))

        // Reset Map Marker
        setupMap(LatLng(0.0, 0.0))
    }



    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}
