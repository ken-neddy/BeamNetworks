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

class OpenTicketsViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("tickets")

    private val _openTickets = MutableStateFlow<List<TicketData>>(emptyList())
    val openTickets: StateFlow<List<TicketData>> = _openTickets.asStateFlow()

    init {
        fetchOpenTickets()
    }

    private fun fetchOpenTickets() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ticketList = mutableListOf<TicketData>()
                for (child in snapshot.children) {
                    try {
                        val ticket = child.getValue(TicketData::class.java)
                        if (ticket != null && ticket.status == "open") {
                            ticket.id = child.key ?: ""
                            ticketList.add(ticket)
                        }
                    } catch (e: Exception) {
                        Log.e("OpenTicketsViewModel", "Error parsing ticket", e)
                    }
                }
                // Sort by timestamp descending (newest first)
                _openTickets.value = ticketList.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OpenTicketsViewModel", "Database error: ${error.message}")
            }
        })
    }

    fun markAsResolved(ticketId: String) {
        if (ticketId.isNotBlank()) {
            database.child(ticketId).child("status").setValue("resolved")
        }
    }
}
