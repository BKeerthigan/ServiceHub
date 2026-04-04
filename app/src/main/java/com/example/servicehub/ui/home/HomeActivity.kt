package com.example.servicehub.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.servicehub.data.model.AdItem
import com.example.servicehub.data.model.TypeItem
import com.example.servicehub.ui.food.FoodActivity
import com.example.servicehub.viewmodel.HomeViewModel

private const val BASE_IMAGE_URL = "https://jmsn.in//images//appimage//"
private val HOME_BG = Color(0xFFF7F7F7)
private val SELECTED_YELLOW = Color(0xFFFFC107)

private fun fullImageUrl(img: String?): String? {
    if (img.isNullOrBlank()) return null
    if (img.startsWith("http", true)) return img
    return BASE_IMAGE_URL + img.trim().removePrefix("/")
}

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contactName = intent.getStringExtra("contact_name")
        val address = intent.getStringExtra("address")

        setContent {
            val vm: HomeViewModel = viewModel()
            HomeScreen(contactName = contactName, address = address, vm = vm)
        }
    }
}

@Composable
fun HomeScreen(
    contactName: String?,
    address: String?,
    vm: HomeViewModel
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) { vm.loadHome() }

    fun openFood(typeId: String) {
        ctx.startActivity(
            Intent(ctx, FoodActivity::class.java).apply {
                putExtra("type_id", typeId) // "1" = FOOD, "2" = FMCG
            }
        )
    }

    Scaffold(
        containerColor = HOME_BG,
        bottomBar = {
            HomeBottomBar(
                onHome = { /* stay */ },
                onFood = { openFood("1") },
                onFmcg = { openFood("2") } // later you can switch to FmcgActivity
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(HOME_BG)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeaderSection(contactName, address) }

            item {
                TypeTilesSection(
                    types = state.types,
                    onFoodClick = { openFood("1") },
                    onFmcgClick = { openFood("2") }
                )
            }

            item { AdsSection(state.ads) }

            if (state.loading) item { Text("Loading...") }
            state.error?.let { err -> item { Text("Error: $err") } }
        }
    }
}

@Composable
private fun HeaderSection(contactName: String?, address: String?) {
    val welcome =
        if (!contactName.isNullOrBlank()) "Welcome ${contactName.trim()}"
        else "Welcome Guest"

    Column {
        Text(welcome, style = MaterialTheme.typography.titleLarge)
        if (!address.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(address.trim(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TypeTilesSection(
    types: List<TypeItem>,
    onFoodClick: () -> Unit,
    onFmcgClick: () -> Unit
) {
    if (types.isEmpty()) return

    val first = types.getOrNull(0)
    val second = types.getOrNull(1)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        first?.let {
            TypeCard(
                title = it.name.orEmpty(),
                subtitle = it.descripition,
                imageUrl = fullImageUrl(it.imgsrc),
                modifier = Modifier.weight(1f),
                onClick = {
                    // API gives FOOD first usually
                    if (it.name.equals("FOOD", true)) onFoodClick() else onFmcgClick()
                }
            )
        }
        second?.let {
            TypeCard(
                title = it.name.orEmpty(),
                subtitle = it.descripition,
                imageUrl = fullImageUrl(it.imgsrc),
                modifier = Modifier.weight(1f),
                onClick = {
                    if (it.name.equals("FOOD", true)) onFoodClick() else onFmcgClick()
                }
            )
        }
    }
}

@Composable
private fun TypeCard(
    title: String,
    subtitle: String?,
    imageUrl: String?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val t = title.trim()
    val s = subtitle?.trim().orEmpty()

    val displayTitle =
        if (t.equals("FMCG", ignoreCase = true)) "FMCG\nDIRECT"
        else t

    val bgBrush = if (t.equals("FOOD", ignoreCase = true)) {
        Brush.verticalGradient(listOf(Color(0xFFFFC35A), Color(0xFFF5A623)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF2E6BC8), Color(0xFF1F4FA3)))
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(210.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                if (s.isNotBlank() && !s.equals(t, ignoreCase = true)) {
                    Text(
                        text = s,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 52.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Go", tint = Color.White)
            }

            AsyncImage(
                model = imageUrl,
                contentDescription = t,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
private fun AdsSection(ads: List<AdItem>) {
    if (ads.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { ads.size })

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) { page ->
            val ad = ads[page]
            Card(shape = MaterialTheme.shapes.large) {
                AsyncImage(
                    model = fullImageUrl(ad.imgsrc),
                    contentDescription = ad.name,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (ads.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(ads.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    onHome: () -> Unit,
    onFood: () -> Unit,
    onFmcg: () -> Unit,
) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }

    NavigationBar(
        containerColor = Color(0xFFEFE7F3),
        contentColor = Color.Black
    ) {
        HomeNavItem("Home", Icons.Filled.Home, selectedIndex == 0) {
            selectedIndex = 0
            onHome()
        }
        HomeNavItem("Food", Icons.Filled.Store, selectedIndex == 1) {
            selectedIndex = 1
            onFood()
        }
        HomeNavItem("FMCG", Icons.Filled.ShoppingCart, selectedIndex == 2) {
            selectedIndex = 2
            onFmcg()
        }
        HomeNavItem("Account", Icons.Filled.AccountCircle, selectedIndex == 3) {
            selectedIndex = 3
            // later open AccountActivity
        }
    }
}

@Composable
private fun RowScope.HomeNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(3.dp)
                        .background(if (selected) SELECTED_YELLOW else Color.Transparent)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Icon(imageVector = icon, contentDescription = label)
            }
        },
        label = { Text(label) },
        alwaysShowLabel = true
    )
}
