package com.helper.beamnetworks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StockRunningLowViewModel(application: Application) : AndroidViewModel(application) {

    private val productsRef = FirebaseDatabase.getInstance().getReference("products")
    private val stockRef = FirebaseDatabase.getInstance().getReference("stock")

    private val _stockRunningLowItems = MutableStateFlow<List<StockSettingItem>>(emptyList())
    val stockRunningLowItems: StateFlow<List<StockSettingItem>> = _stockRunningLowItems

    private val productsListener: ValueEventListener
    private val stockListener: ValueEventListener

    private var productList = listOf<Product>()
    private var stockList = listOf<StockItem>()

    init {
        productsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                combineData()
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        }

        stockListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                stockList = snapshot.children.mapNotNull { it.getValue(StockItem::class.java) }
                combineData()
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        }

        productsRef.addValueEventListener(productsListener)
        stockRef.addValueEventListener(stockListener)
    }

    private fun combineData() {
        val stockMap = stockList.associateBy { it.name }
        val combinedList = productList.map {
            StockSettingItem(
                name = it.name,
                quantity = stockMap[it.name]?.quantity ?: 0L,
                minStockLevel = it.minStockLevel
            )
        }.filter { it.quantity < it.minStockLevel }
        _stockRunningLowItems.value = combinedList
    }

    override fun onCleared() {
        super.onCleared()
        productsRef.removeEventListener(productsListener)
        stockRef.removeEventListener(stockListener)
    }
}