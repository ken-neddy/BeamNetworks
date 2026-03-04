package com.helper.beamnetworks

import com.google.firebase.database.PropertyName

data class CustomerData(
    @get:PropertyName("Name")
    @set:PropertyName("Name")
    var name: String = "",
    
    @get:PropertyName("Phone")
    @set:PropertyName("Phone")
    var phone: String = "",
    
    @get:PropertyName("Package")
    @set:PropertyName("Package")
    var packageName: String = ""
)