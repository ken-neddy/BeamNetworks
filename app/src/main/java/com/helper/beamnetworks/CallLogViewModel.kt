package com.helper.beamnetworks

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CallLogViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().reference

    private val _installations = MutableStateFlow<List<InstallationData>>(emptyList())
    val installations = _installations.asStateFlow()

    init {
        fetchInstallations()
    }

    private fun fetchInstallations() {
        database.child("installations").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val installationList = snapshot.children.mapNotNull {
                    it.getValue(InstallationData::class.java)
                }
                _installations.value = installationList
                Log.d("CallLogViewModel", "Loaded ${installationList.size} installations")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CallLogViewModel", "Failed to fetch installations", error.toException())
            }
        })
    }
}