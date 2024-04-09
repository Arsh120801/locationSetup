package com.example.locationapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationapp.ui.theme.LocationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel:LocationViewModel = viewModel()
            LocationAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun MyApp(viewModel: LocationViewModel){
    val context = LocalContext.current
    val locationUtils = LocationUtils(context)
    LocationDisplay(locationUtils = locationUtils, viewModel,context = context )
}

@Composable
fun LocationDisplay(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    context : Context
){

    val location = viewModel.location.value
    val address = location?.let{
        locationUtils.reverseGeoCoder(locationData = location)
    }

    // NOTE: THIS requestPermissionLauncher CAN BE USED FOR ASKING ANY PERMISSION SO USE IT ANYWHERE //
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult ={
            permissions->
            if(permissions[Manifest.permission.ACCESS_FINE_LOCATION]==true && permissions[Manifest.permission.ACCESS_COARSE_LOCATION]==true){
                // I have permission for location
                locationUtils.requestLocationUpdates(viewModel=viewModel)
                address
            }
            else{
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)

                if (rationaleRequired){
                    Toast.makeText(context,
                        "Permission Reruired for this feature", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(context,
                        "Enable location permission from settings!!", Toast.LENGTH_LONG).show()
                }

            }
        }
    )

    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if(location!=null){
            Text(text = "longitude: ${location.longitude} || latitude: ${location.latitude}")
            if (address != null) {
                Text(text = "this is address    ${ address }")
            }
        }else{
            Text(text = "Location Not Available")
        }

        Button(onClick = {
            if (locationUtils.hasLocationPermission(context)){
                // display
                locationUtils.requestLocationUpdates(viewModel=viewModel)
            }else{
                //Request for permission
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }) {
            Text(text = "Get Location")
        }
    }
}