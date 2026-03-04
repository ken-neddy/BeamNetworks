package com.helper.beamnetworks

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CompletedTicketsViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("tickets")

    private val _completedTickets = MutableStateFlow<List<TicketData>>(emptyList())
    val completedTickets: StateFlow<List<TicketData>> = _completedTickets.asStateFlow()

    init {
        fetchCompletedTickets()
    }

    private fun fetchCompletedTickets() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ticketList = mutableListOf<TicketData>()
                for (child in snapshot.children) {
                    try {
                        val ticket = child.getValue(TicketData::class.java)
                        if (ticket != null && (ticket.status == "resolved" || ticket.status == "completed")) {
                            ticket.id = child.key ?: ""
                            ticketList.add(ticket)
                        }
                    } catch (e: Exception) {
                        Log.e("CompletedTicketsVM", "Error parsing ticket", e)
                    }
                }
                _completedTickets.value = ticketList.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CompletedTicketsVM", "Database error: ${error.message}")
            }
        })
    }
}
