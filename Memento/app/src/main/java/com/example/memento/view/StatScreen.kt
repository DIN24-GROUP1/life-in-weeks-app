package com.example.memento.view

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage

@Composable
fun StatScreen() {
    var imageUrl by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        FirebaseStorage.getInstance()
            .reference
            .child("qpad-xiao.jpg")
            .downloadUrl
            .addOnSuccessListener { uri -> imageUrl = uri }
            .addOnFailureListener { e -> error = e.message }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageUrl != null -> AsyncImage(
                model = imageUrl,
                contentDescription = "Test image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            error != null -> Text("Error: $error")
            else -> CircularProgressIndicator()
        }
    }
}