package com.helper.beamnetworks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditStockSettingViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    private val database = FirebaseDatabase.getInstance().getReference("products")

    val productName: StateFlow<String> = savedStateHandle.getStateFlow("productName", "")

    private val _minStockLevel = MutableStateFlow("")
    val minStockLevel: StateFlow<String> = _minStockLevel

    private val _product = MutableStateFlow<Product?>(null)

    init {
        viewModelScope.launch {
            productName.collect { name ->
                if (name.isNotBlank()) {
                    fetchProduct(name)
                }
            }
        }
    }

    private fun fetchProduct(productName: String) {
        database.child(productName).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val product = snapshot.getValue(Product::class.java)
                _product.value = product
                _minStockLevel.value = product?.minStockLevel?.toString() ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun onMinStockLevelChange(newLevel: String) {
        _minStockLevel.value = newLevel
    }

    fun saveSettings() {
        val product = _product.value
        val minStock = _minStockLevel.value.toLongOrNull()
        if (product != null && minStock != null) {
            val updatedProduct = product.copy(minStockLevel = minStock)
            database.child(product.name).setValue(updatedProduct)
        }
    }
}
