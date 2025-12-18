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

data class UsedProduct(val name: String, var quantity: Int)

class CompleteInstallationViewModel(application: Application) : AndroidViewModel(application) {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _usedProducts = MutableStateFlow<MutableList<UsedProduct>>(mutableListOf())
    val usedProducts: StateFlow<List<UsedProduct>> = _usedProducts

    private val _completionStatus = MutableStateFlow<String?>(null)
    val completionStatus: StateFlow<String?> = _completionStatus

    private val productsRef = FirebaseDatabase.getInstance().getReference("Products")
    private val installationsRef = FirebaseDatabase.getInstance().getReference("installations")

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _products.value = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun onProductQuantityChanged(productName: String, quantity: String) {
        val quant = quantity.toIntOrNull() ?: 0
        val existing = _usedProducts.value.find { it.name == productName }
        if (existing != null) {
            existing.quantity = quant
        } else {
            _usedProducts.value.add(UsedProduct(productName, quant))
        }
    }

    fun completeInstallation(installationId: String) {
        viewModelScope.launch {
            // Update installation status
            installationsRef.child(installationId).child("status").setValue("Completed")
                .addOnSuccessListener {
                    _completionStatus.value = "Installation marked as completed."
                }
                .addOnFailureListener {
                    _completionStatus.value = "Failed to update installation status."
                }

            // Update product quantities
            _usedProducts.value.forEach { usedProduct ->
                if (usedProduct.quantity > 0) {
                    productsRef.orderByChild("name").equalTo(usedProduct.name)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val productSnapshot = snapshot.children.firstOrNull()
                                if (productSnapshot != null) {
                                    val currentQuantity = productSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                                    productSnapshot.ref.child("quantity").setValue(currentQuantity - usedProduct.quantity)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                _completionStatus.value = "Failed to update stock for ${usedProduct.name}."
                            }
                        })
                }
            }
        }
    }

    fun clearCompletionStatus() {
        _completionStatus.value = null
    }
}
