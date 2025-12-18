package com.helper.beamnetworks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditProductViewModel(application: Application) : AndroidViewModel(application) {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    private val _lowStockThreshold = MutableStateFlow("")
    val lowStockThreshold: StateFlow<String> = _lowStockThreshold

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus

    private val database = FirebaseDatabase.getInstance().getReference("Products")

    fun getProduct(productName: String) {
        database.orderByChild("name").equalTo(productName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val product = snapshot.children.firstOrNull()?.getValue(Product::class.java)
                _product.value = product
                _lowStockThreshold.value = product?.lowStockThreshold?.toString() ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun onLowStockThresholdChange(threshold: String) {
        _lowStockThreshold.value = threshold
    }

    fun saveProduct(productName: String) {
        viewModelScope.launch {
            val threshold = _lowStockThreshold.value.toIntOrNull()

            if (threshold != null) {
                database.orderByChild("name").equalTo(productName).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val productSnapshot = snapshot.children.firstOrNull()
                        productSnapshot?.ref?.child("lowStockThreshold")?.setValue(threshold)
                            ?.addOnSuccessListener {
                                _saveStatus.value = "Threshold updated successfully"
                            }
                            ?.addOnFailureListener {
                                _saveStatus.value = "Failed to update threshold"
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _saveStatus.value = "Failed to update threshold: ${error.message}"
                    }
                })
            } else {
                _saveStatus.value = "Invalid threshold value"
            }
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }
}
