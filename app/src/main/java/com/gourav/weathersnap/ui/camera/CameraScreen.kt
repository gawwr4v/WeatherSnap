package com.gourav.weathersnap.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gourav.weathersnap.ui.theme.WeatherPrimary
import com.gourav.weathersnap.ui.theme.WeatherPrimaryDark
import com.gourav.weathersnap.ui.theme.WeatherSnapTheme
import com.gourav.weathersnap.ui.theme.WeatherText
import java.io.File

// this is the main entry for the camera screen
@Composable
fun CameraRoute(
    onClose: () -> Unit,
    onPhotoCaptured: (String) -> Unit,
) {
    val context = LocalContext.current
    
    // we need to check if the user has already granted camera access
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    
    // this launcher handles the pop up that asks the user for permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
    }

    // if permission is not granted yet we ask for it as soon as the screen opens
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    CameraScreen(
        hasPermission = hasPermission,
        onClose = onClose,
        onPhotoCaptured = onPhotoCaptured,
    )
}

// this is the actual layout for the camera interface
@Composable
fun CameraScreen(
    hasPermission: Boolean,
    onClose: () -> Unit,
    onPhotoCaptured: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // we remember the image capture object so it stays the same across recompositions
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(20.dp),
    ) {
        if (hasPermission) {
            // we use androidview because camerax preview view is a classic android view
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PreviewView(viewContext).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener(
                        {
                            val cameraProvider = cameraProviderFuture.get()
                            // set up the camera preview so the user can see what they are aiming at
                            val preview = CameraPreview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            // make sure to unbind everything before binding again
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture,
                            )
                        },
                        ContextCompat.getMainExecutor(context),
                    )
                },
            )

            // when this screen is closed we make sure to shut down the camera
            DisposableEffect(Unit) {
                onDispose {
                    ProcessCameraProvider.getInstance(context).get().unbindAll()
                }
            }
        } else {
            // show a simple text if the user denied permission
            Text(
                text = "Camera permission is needed to capture report evidence.",
                color = WeatherText,
                modifier = Modifier.align(Alignment.Center),
                fontWeight = FontWeight.Bold,
            )
        }

        // top bar with the screen title and close button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Custom Camera",
                color = WeatherText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = onClose,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = WeatherText,
                ),
                border = BorderStroke(1.dp, WeatherText.copy(alpha = 0.5f)),
            ) {
                Text("Close")
            }
        }

        // bottom section with capture button and error messages
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = WeatherText,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = {
                    capturePhoto(
                        context = context,
                        imageCapture = imageCapture,
                        onError = { errorMessage = it },
                        onPhotoCaptured = onPhotoCaptured,
                    )
                },
                enabled = hasPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WeatherPrimary,
                    contentColor = WeatherPrimaryDark,
                ),
            ) {
                Text("Capture", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// helper function to handle the actual photo taking process
private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onError: (String) -> Unit,
    onPhotoCaptured: (String) -> Unit,
) {
    // we save the original photo in the cache directory
    val photoDir = File(context.cacheDir, "captures").apply { mkdirs() }
    val photoFile = File(photoDir, "original_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    // trigger the camera to snap a picture
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // success, return the path to the original photo
                onPhotoCaptured(photoFile.absolutePath)
            }

            override fun onError(exception: ImageCaptureException) {
                // failure, let the user know what went wrong
                onError(exception.message ?: "Could not capture photo.")
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CameraScreenPreview() {
    WeatherSnapTheme {
        CameraScreen(
            hasPermission = false,
            onClose = {},
            onPhotoCaptured = {},
        )
    }
}
