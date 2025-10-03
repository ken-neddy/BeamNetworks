package com.helper.beamnetworks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helper.beamnetworks.ui.theme.BeamNetworksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeamNetworksTheme {
            }
        }
    }
}

@Composable
fun ButtonCard() {
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ){
        Card(modifier = Modifier.padding(16.dp).width(150.dp).height(150.dp),

        ) { }
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BeamNetworksTheme {
        ButtonCard()
    }
}
