package com.helper.beamnetworks

import com.google.firebase.database.PropertyName

data class PaymentData(
    var id: String = "",
    
    @get:PropertyName("Created at")
    @set:PropertyName("Created at")
    var date: String = "",
    
    @get:PropertyName("Customer name")
    @set:PropertyName("Customer name")
    var customerName: String = "",
    
    @get:PropertyName("Amount")
    @set:PropertyName("Amount")
    var amount: String = "",

    @get:PropertyName("Notes")
    @set:PropertyName("Notes")
    var notes: String = ""
)