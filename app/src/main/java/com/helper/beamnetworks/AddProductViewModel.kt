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

class AddProductViewModel(application: Application) : AndroidViewModel(application) {

    private val _productNames = MutableStateFlow<List<String>>(emptyList())
    val productNames: StateFlow<List<String>> = _productNames

    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> = _productName

    private val _quantity = MutableStateFlow("")
    val quantity: StateFlow<String> = _quantity

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus

    private val database = FirebaseDatabase.getInstance().getReference("Products")

    init {
        fetchProductNames()
    }

    private fun fetchProductNames() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val names = snapshot.children.mapNotNull { it.child("name").getValue(String::class.java) }
                _productNames.value = names
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun onProductNameChange(name: String) {
        _productName.value = name
    }

    fun onQuantityChange(q: String) {
        _quantity.value = q
    }

    fun saveProduct() {
        viewModelScope.launch {
            val name = _productName.value
            val quant = _quantity.value.toIntOrNull()

            if (name.isNotBlank() && quant != null) {
                database.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // Product exists, update quantity
                            val productSnapshot = snapshot.children.first()
                            val currentQuantity = productSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                            productSnapshot.ref.child("quantity").setValue(currentQuantity + quant)
                                .addOnSuccessListener {
                                    _saveStatus.value = "Product updated successfully"
                                }
                                .addOnFailureListener {
                                    _saveStatus.value = "Failed to update product"
                                }
                        } else {
                            // New product
                            val newProduct = Product(name, quant)
                            database.push().setValue(newProduct)
                                .addOnSuccessListener {
                                    _saveStatus.value = "Product saved successfully"
                                }
                                .addOnFailureListener {
                                    _saveStatus.value = "Failed to save product"
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                         _saveStatus.value = "Failed to save product: ${error.message}"
                    }
                })
            } else {
                _saveStatus.value = "Invalid product name or quantity"
            }
        }
    }
     fun clearSaveStatus() {
        _saveStatus.value = null
    }
}
