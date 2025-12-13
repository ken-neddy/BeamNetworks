package com.helper.beamnetworks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProductSelection(val product: Product, var quantity: Int = 0)

class ProductSelectionViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val installationId: String = savedStateHandle.get("installationId")!!

    private val _products = MutableStateFlow<List<ProductSelection>>(emptyList())
    val products: StateFlow<List<ProductSelection>> = _products

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredProducts = _products.combine(_searchQuery) { products, query ->
        if (query.isBlank()) {
            products
        } else {
            products.filter { it.product.name.contains(query, ignoreCase = true) }
        }
    }

    private val database = FirebaseDatabase.getInstance()
    private val productsRef = database.getReference("products")
    private val stockRef = database.getReference("stock")
    private val installationsRef = database.getReference("installations")

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productList = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                    _products.value = productList.map { ProductSelection(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onQuantityChanged(productSelection: ProductSelection, quantity: String) {
        val newQuantity = quantity.toIntOrNull() ?: 0
        val index = _products.value.indexOf(productSelection)
        if (index != -1) {
            val updatedList = _products.value.toMutableList()
            updatedList[index] = productSelection.copy(quantity = newQuantity)
            _products.value = updatedList
        }
    }

    fun completeInstallation() {
        viewModelScope.launch {
            val usedProducts = _products.value.filter { it.quantity > 0 }

            // Update stock
            stockRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val stockMap = snapshot.children.associate {
                        it.key to it.getValue(StockItem::class.java)
                    }
                    usedProducts.forEach { selection ->
                        val stockItem = stockMap[selection.product.name]
                        if (stockItem != null) {
                            val newQuantity = stockItem.quantity - selection.quantity
                            stockRef.child(selection.product.name).child("quantity").setValue(newQuantity)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })

            // Update installation status
            installationsRef.child(installationId).child("status").setValue("Completed")
        }
    }
}