package com.example.servicehub.ui.policy

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

class PDFViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title  = intent.getStringExtra("title").orEmpty()
        val pdfUrl = intent.getStringExtra("pdf_url").orEmpty()
        setContent {
            PDFViewerScreen(title = title, pdfUrl = pdfUrl, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PDFViewerScreen(title: String, pdfUrl: String, onBack: () -> Unit) {
    var progress by remember { mutableIntStateOf(0) }
    val viewerUrl = "https://docs.google.com/viewer?url=${
        java.net.URLEncoder.encode(pdfUrl, "UTF-8")
    }&embedded=true"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor             = Color.White,
                titleContentColor          = Color.Black,
                navigationIconContentColor = Color.Black
            )
        )

        // Loading progress bar
        if (progress < 100) {
            LinearProgressIndicator(
                progress      = { progress / 100f },
                modifier      = Modifier.fillMaxWidth(),
                color         = Color(0xFFCC0000),
                trackColor    = Color(0xFFEEEEEE)
            )
        }

        HorizontalDivider(color = Color(0xFFE0E0E0))

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }
                        }
                        settings.javaScriptEnabled    = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort      = true
                        settings.builtInZoomControls  = true
                        settings.displayZoomControls  = false
                        setBackgroundColor(android.graphics.Color.WHITE)
                        loadUrl(viewerUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Show spinner while loading
            if (progress < 100) {
                CircularProgressIndicator(
                    color    = Color(0xFFCC0000),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
