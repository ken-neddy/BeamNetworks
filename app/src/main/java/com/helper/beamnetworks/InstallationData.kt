package com.helper.beamnetworks

data class InstallationData(
    var id: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientLocation: String = "",
    val installationDate: String = "",
    val moreNotes: String = "",
    var status: String = "Upcoming"
)