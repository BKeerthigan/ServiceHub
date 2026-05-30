package com.example.servicehub.ui.schemes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.data.model.SchemeItem
import com.example.servicehub.session.UserSession
import com.example.servicehub.viewmodel.TargetSchemeViewModel

private val TS_PURPLE       = Color(0xFF7B1FA2)
private val TS_PURPLE_DARK  = Color(0xFF4A0072)
private val TS_PURPLE_LIGHT = Color(0xFFCE93D8)
private val IMG_BASE        = "https://jmsn.in//images//appimage//"

class TargetSchemeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: TargetSchemeViewModel = viewModel()
            val companyId = UserSession.companyId
            LaunchedEffect(companyId) { vm.load(companyId) }
            TargetSchemeScreen(vm = vm, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetSchemeScreen(vm: TargetSchemeViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    val activeLabel = state.info?.active.orEmpty()
    val pastLabel   = state.info?.past.orEmpty()
    val emptyMsg    = state.info?.content ?: "No schemes available right now.\nPlease check again later."

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {

        // ── Purple gradient header ────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(TS_PURPLE_DARK, TS_PURPLE)
                    )
                )
        ) {
            // Back button
            IconButton(
                onClick  = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // Scheme image (right side)
            if (!state.info?.imgsrc.isNullOrBlank()) {
                AsyncImage(
                    model            = IMG_BASE + state.info?.imgsrc,
                    contentDescription = null,
                    contentScale     = ContentScale.Fit,
                    modifier         = Modifier
                        .size(140.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp, top = 20.dp)
                )
            } else {
                Icon(
                    Icons.Filled.TrackChanges,
                    contentDescription = null,
                    tint     = TS_PURPLE_LIGHT.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp)
                )
            }

        }

        // ── Tab row ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0F0F0))
                    .padding(4.dp)
            ) {
                listOf(activeLabel, pastLabel).forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedTab == index) Color(0xFF1A1A1A)
                                else Color.Transparent
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = label,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp,
                            color      = if (selectedTab == index) Color.White else Color.Gray
                        )
                    }
                    if (index == 0) {
                        // Make tab clickable using a Surface overlay via Modifier.clickable
                        // (handled below — we use LaunchedEffect instead)
                    }
                }
            }

            // Clickable overlay on each tab half
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .let { if (selectedTab != 0) it.background(Color.Transparent) else it }
                ) {
                    androidx.compose.material3.Surface(
                        onClick = { selectedTab = 0 },
                        color   = Color.Transparent,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    androidx.compose.material3.Surface(
                        onClick = { selectedTab = 1 },
                        color   = Color.Transparent,
                        modifier = Modifier.fillMaxSize()
                    ) {}
                }
            }
        }

        // ── Tab content ───────────────────────────────────────────────────
        val isLoading = if (selectedTab == 0) state.loadingActive else state.loadingPast
        val schemes   = if (selectedTab == 0) state.activeSchemes else state.pastSchemes

        if (state.loadingInfo) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TS_PURPLE)
            }
        } else if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TS_PURPLE)
            }
        } else if (schemes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = emptyMsg,
                    fontSize  = 15.sp,
                    color     = Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(schemes) { scheme ->
                    SchemeCard(scheme)
                }
            }
        }
    }
}

@Composable
private fun SchemeCard(scheme: SchemeItem) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Scheme name + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    scheme.scheme_name.orEmpty(),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    modifier   = Modifier.weight(1f)
                )
                if (!scheme.status.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = TS_PURPLE.copy(alpha = 0.12f)
                    ) {
                        Text(
                            scheme.status,
                            fontSize  = 11.sp,
                            color     = TS_PURPLE,
                            fontWeight = FontWeight.SemiBold,
                            modifier  = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Progress bar (achieved vs target)
            val target   = scheme.target_amount?.toFloatOrNull() ?: 0f
            val achieved = scheme.achieved_amount?.toFloatOrNull() ?: 0f
            val progress = if (target > 0) (achieved / target).coerceIn(0f, 1f) else 0f

            if (target > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Target: ₹${scheme.target_amount.orEmpty()}",   fontSize = 12.sp, color = Color.Gray)
                    Text("Achieved: ₹${scheme.achieved_amount.orEmpty()}", fontSize = 12.sp, color = TS_PURPLE, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress     = { progress },
                    modifier     = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color        = TS_PURPLE,
                    trackColor   = TS_PURPLE.copy(alpha = 0.15f)
                )
                Spacer(Modifier.height(8.dp))
            }

            // Dates + reward
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (!scheme.start_date.isNullOrBlank()) {
                    Text("${scheme.start_date} → ${scheme.end_date.orEmpty()}", fontSize = 11.sp, color = Color.Gray)
                }
                if (!scheme.reward.isNullOrBlank()) {
                    Text("Reward: ₹${scheme.reward}", fontSize = 12.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
