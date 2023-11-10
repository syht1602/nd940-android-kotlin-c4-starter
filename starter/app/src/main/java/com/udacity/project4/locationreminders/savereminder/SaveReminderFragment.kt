package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private val supportQVersion = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private var permissionDialog: AlertDialog? = null

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(requireActivity(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_FENCE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_save_reminder
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.lifecycleOwner = this

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.selectLocation.setOnClickListener {
//            // Navigate to another fragment to get the user location
//            val directions = SaveReminderFragmentDirections
//                .actionSaveReminderFragmentToSelectLocationFragment()
//            _viewModel.navigationCommand.value = NavigationCommand.To(directions)
//        }
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

//        binding.saveReminder.setOnClickListener {
//            val title = _viewModel.reminderTitle.value
//            val description = _viewModel.reminderDescription
//            val location = _viewModel.reminderSelectedLocationStr.value
//            val latitude = _viewModel.latitude
//            val longitude = _viewModel.longitude.value
//
//            // TODO: use the user entered reminder details to:
//            //  1) add a geofencing request
//            //  2) save the reminder to the local db
//        }
        with(_viewModel) {
            isNavigateToSelectLocation.observe(viewLifecycleOwner) {
                it?.let {
                    if (it) {
                        val directions = SaveReminderFragmentDirections
                            .actionSaveReminderFragmentToSelectLocationFragment()
                        _viewModel.navigationCommand.value = NavigationCommand.To(directions)
                        onNavigateToSelectLocationCompleted()
                    }
                }
            }
            isSaveReminderClick.observe(viewLifecycleOwner) {
                it?.let {
                    if (it) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (!backgroundLocationPermissionApproved()) {
                                requestBackgroundLocationPermissions()
                            }
                            if (!foreGroundLocationPermissionApproved()) {
                                requestForeGroundLocationPermissions()
                            }
                        }
                        checkDeviceLocationSettingsAndStartGeofence()
                        onSaveReminderCompleted()
                    }
                }
            }
            isAddGeoFenceData.observe(viewLifecycleOwner) {
                it?.let {
                    if (it) {
                        reminderData.value?.let {
                            addGeofenceForClue(it)
                            onAddGeoFenceDataCompleted()
                        }
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    @SuppressLint("MissingPermission")
    private fun addGeofenceForClue(currentGeofenceData: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(currentGeofenceData.id)
            .setCircularRegion(
                currentGeofenceData.latitude ?: 0.0,
                currentGeofenceData.longitude ?: 0.0,
                360f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(request, pendingIntent).run {
            addOnSuccessListener {
                _viewModel.onGeoFenceUpdateSuccessfully()
            }
            addOnFailureListener {
                _viewModel.onGeoFenceUpdateError()
                it.message?.let { msg ->
                    Log.e("geofencingClient error", msg)
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun backgroundLocationPermissionApproved(): Boolean {
        val backgroundPermissionApproved =
            if (supportQVersion) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return backgroundPermissionApproved
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun foreGroundLocationPermissionApproved(): Boolean {
        val backgroundPermissionApproved =
            if (supportQVersion) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                        )
            } else {
                true
            }
        return backgroundPermissionApproved
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermissions() {
        if (backgroundLocationPermissionApproved() && foreGroundLocationPermissionApproved())
            return

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            showPermissionDialog(positiveAction = {
                openPermissionSetting()
            })
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionDialog(positiveAction = {
                openPermissionSetting()
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestForeGroundLocationPermissions() {
        if (backgroundLocationPermissionApproved())
            return

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionDialog(positiveAction = {
                openPermissionSetting()
            })
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    requestPermissionLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    _viewModel.onCheckLocationUnknownError()
                    Log.e("check location error :: ", sendEx.message.toString())
                }
            } else {
                _viewModel.onCheckLocationError()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                _viewModel.validateAndSaveReminder()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            lifecycleScope.launch {
                delay(100)
                checkDeviceLocationSettingsAndStartGeofence(false)
            }
        }

    private fun openPermissionSetting() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        requireActivity().startActivity(intent)
    }

    private fun showPermissionDialog(positiveAction: () -> Unit) {
        if (permissionDialog == null) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.app_name))
            builder.setMessage(getString(R.string.location_required_error))

            builder.setPositiveButton(R.string.settings) { dialog, _ ->
                positiveAction.invoke()
                dialog.dismiss()
            }

            builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            permissionDialog = builder.show()
        } else {
            permissionDialog?.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        internal const val ACTION_GEOFENCE_FENCE =
            "SaveReminderFragment.ACTION_GEOFENCE_FENCE"
    }
}