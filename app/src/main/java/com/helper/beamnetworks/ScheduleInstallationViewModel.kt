package com.helper.beamnetworks

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ScheduleInstallationViewModel(application: Application) : AndroidViewModel(application) {

    private val _clientName = MutableStateFlow("")
    val clientName: StateFlow<String> = _clientName

    private val _clientPhone = MutableStateFlow("")
    val clientPhone: StateFlow<String> = _clientPhone

    private val _clientLocation = MutableStateFlow("")
    val clientLocation: StateFlow<String> = _clientLocation

    private val _installationDate = MutableStateFlow("")
    val installationDate: StateFlow<String> = _installationDate

    private val _hasRouter = MutableStateFlow(false)
    val hasRouter: StateFlow<Boolean> = _hasRouter

    private val _moreNotes = MutableStateFlow("")
    val moreNotes: StateFlow<String> = _moreNotes

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    fun onClientNameChange(name: String) {
        _clientName.value = name
    }

    fun onClientPhoneChange(phone: String) {
        _clientPhone.value = phone
    }

    fun onClientLocationChange(location: String) {
        _clientLocation.value = location
    }

    fun onInstallationDateChange(date: String) {
        _installationDate.value = date
    }

    fun onHasRouterChange(has: Boolean) {
        _hasRouter.value = has
    }

    fun onMoreNotesChange(notes: String) {
        _moreNotes.value = notes
    }

    fun saveInstallation() {
        val installation = InstallationData(
            id = UUID.randomUUID().toString(),
            clientName = _clientName.value,
            clientPhone = _clientPhone.value,
            clientLocation = _clientLocation.value,
            installationDate = _installationDate.value,
            hasRouter = _hasRouter.value,
            moreNotes = _moreNotes.value,
            status = "Upcoming"
        )

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("installations")
        ref.child(installation.id).setValue(installation).addOnCompleteListener { task ->
            _saveStatus.value = if (task.isSuccessful) "Success" else "Failed"
        }
    }

    fun getCurrentLocation(context: Context) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val geocoder = Geocoder(context)
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val addressParts = mutableListOf<String>()
                        address.thoroughfare?.let { addressParts.add(it) }
                        address.subLocality?.let { addressParts.add(it) }
                        _clientLocation.value = addressParts.joinToString()
                    }
                }
            }
        } catch (e: SecurityException) {
            // Handle exception
        }
    }
}
