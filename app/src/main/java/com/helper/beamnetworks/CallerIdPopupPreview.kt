package com.helper.beamnetworks

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView

@Preview(showBackground = true)
@Composable
fun CallerIdPopupPreview() {
    AndroidView(
        factory = { context ->
            val view = android.view.LayoutInflater.from(context)
                .inflate(R.layout.caller_id_popup, null)

            view.findViewById<TextView>(R.id.caller_name).text = "John Doe"
            view.findViewById<TextView>(R.id.caller_number).text = "+1 123-456-7890"
            view.findViewById<TextView>(R.id.caller_status).text = "Upcoming"
            view.findViewById<TextView>(R.id.caller_location).text = "123 Main St, Anytown, USA"

            view
        }
    )
}
