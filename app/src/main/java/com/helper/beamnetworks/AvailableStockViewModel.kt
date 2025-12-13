package com.helper.beamnetworks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class StockItem(
    val name: String = "",
    val quantity: Long = 0,
)

class AvailableStockViewModel(application: Application) : AndroidViewModel(application) {

    private val _stockItems = MutableStateFlow<List<StockItem>>(emptyList())
    val stockItems: StateFlow<List<StockItem>> = _stockItems

    init {
        val database = FirebaseDatabase.getInstance().getReference("stock")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull {
                    it.getValue(StockItem::class.java)
                }
                _stockItems.value = items
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
