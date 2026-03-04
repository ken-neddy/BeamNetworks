package com.helper.beamnetworks

data class TicketData(
    var id: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val issue: String = "",
    val status: String = "open",
    val dateTime: String = "",
    val timestamp: Long = 0L
)