package com.example.servicehub.ui.policy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.viewmodel.PolicyViewModel

private val POLICY_RED = Color(0xFFCC0000)
private val IMG_BASE   = "https://jmsn.in//images//appimage//"
private val PDF_BASE   = "https://jmsn.in/images/appimage/"

class PolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: PolicyViewModel = viewModel()
            PolicyListScreen(vm = vm, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyListScreen(vm: PolicyViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx   = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF2F2F2),
        topBar = {
            TopAppBar(
                title = { Text("Policies", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = Color.White,
                    titleContentColor      = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = POLICY_RED)
                    }
                }
                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.error}", color = Color.Red)
                    }
                }
                else -> {
                    val data    = state.data
                    val policy  = data?.policies?.firstOrNull()
                    val sources = data?.policiesSrc?.firstOrNull()

                    // ── Banner header ────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFBBDEFB), Color(0xFFE3F2FD))
                                )
                            )
                    ) {
                        // Policy banner image (right side)
                        if (!data?.policiesImage.isNullOrBlank()) {
                            AsyncImage(
                                model             = IMG_BASE + data?.policiesImage,
                                contentDescription = null,
                                contentScale      = ContentScale.Fit,
                                modifier          = Modifier
                                    .size(130.dp)
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                            )
                        }

                        // Text overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 20.dp, end = 150.dp)
                        ) {
                            Text(
                                "Browse our terms,\npolicies and\nguidelines",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = Color(0xFF1A237E),
                                lineHeight = 26.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Policy list card ─────────────────────────────────
                    Card(
                        shape     = RoundedCornerShape(14.dp),
                        colors    = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column {
                            val items = buildList {
                                policy?.tandc?.let         { add(it to sources?.tandcSrc.orEmpty()) }
                                policy?.privacy?.let       { add(it to sources?.privacySrc.orEmpty()) }
                                policy?.returnPolicy?.let  { add(it to sources?.returnSrc.orEmpty()) }
                                policy?.delivery?.let      { add(it to sources?.deliverySrc.orEmpty()) }
                            }

                            items.forEachIndexed { index, (name, pdfFile) ->
                                PolicyRow(
                                    title   = name,
                                    onClick = {
                                        if (pdfFile.isNotBlank()) {
                                            ctx.startActivity(
                                                Intent(ctx, PDFViewerActivity::class.java).apply {
                                                    putExtra("title",   name)
                                                    putExtra("pdf_url", PDF_BASE + pdfFile)
                                                }
                                            )
                                        }
                                    }
                                )
                                if (index < items.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicyRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = title,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Medium,
            color      = Color(0xFF1A1A1A),
            modifier   = Modifier.weight(1f)
        )
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint     = Color(0xFFAAAAAA),
            modifier = Modifier.size(20.dp)
        )
    }
}
