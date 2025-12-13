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

data class Product(
    val name: String = "",
    val minStockLevel: Long = 0
)

class AddProductViewModel(application: Application) : AndroidViewModel(application) {

    private val productsDatabase = FirebaseDatabase.getInstance().getReference("products")
    private val stockDatabase = FirebaseDatabase.getInstance().getReference("stock")

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> = _productName

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount

    private val _isDropdownExpanded = MutableStateFlow(false)
    val isDropdownExpanded: StateFlow<Boolean> = _isDropdownExpanded

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            productsDatabase.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productList = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                    _products.value = productList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    fun onProductNameChange(name: String) {
        _productName.value = name
    }

    fun onAmountChange(newAmount: String) {
        _amount.value = newAmount
    }

    fun onDropdownDismiss() {
        _isDropdownExpanded.value = false
    }

    fun onDropdownClick() {
        _isDropdownExpanded.value = !_isDropdownExpanded.value
    }
    
    fun onProductSelected(product: Product) {
        _productName.value = product.name
        _isDropdownExpanded.value = false
    }

    fun addProduct() {
        val newProductName = _productName.value
        val newAmount = _amount.value.toLongOrNull() ?: 0L
        
        if (newProductName.isNotBlank() && newAmount > 0) {
            val newProduct = Product(name = newProductName)
            productsDatabase.child(newProductName).setValue(newProduct)
            
            val stockItem = StockItem(name = newProductName, quantity = newAmount)
            stockDatabase.child(newProductName).setValue(stockItem)
        }
    }
}
