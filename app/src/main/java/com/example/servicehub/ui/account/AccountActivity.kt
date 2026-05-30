package com.example.servicehub.ui.account

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.servicehub.ui.expiry.ExpiryActivity
import com.example.servicehub.ui.schemes.TargetSchemeActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.servicehub.data.model.AccountData
import com.example.servicehub.session.UserSession
import com.example.servicehub.ui.delivery.DeliveryActivity
import com.example.servicehub.ui.returns.ReturnActivity
import com.example.servicehub.viewmodel.AccountViewModel
import java.io.File

private val BRAND_RED = Color(0xFFCC0000)
private val PAGE_BG = Color(0xFFF2F2F2)
private val YELLOW_INFO = Color(0xFFFFF8DC)

class AccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: AccountViewModel = viewModel()
            val companyId = UserSession.companyId
            LaunchedEffect(companyId) { vm.load(companyId) }
            AccountScreen(vm = vm)
        }
    }
}

@Composable
fun AccountScreen(vm: AccountViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    Scaffold(
        containerColor = PAGE_BG,
        bottomBar = { AccountBottomBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                AccountHeader(data = state.data)
            }

            item { Spacer(Modifier.height(12.dp)) }

            if (state.loading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BRAND_RED)
                    }
                }
            }

            state.error?.let {
                item {
                    Text(
                        "Error: $it",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            state.data?.let { account ->
                item {
                    Text(
                        "Manage Your Business",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                item {
                    ManageBusinessCard(account)
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    Text(
                        "Need any help?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                item {
                    SupportCard(account)
                }

                item { Spacer(Modifier.height(12.dp)) }

                item {
                    AppInfoCard(ctx)
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun AccountHeader(data: AccountData?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BRAND_RED)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data?.company_name ?: "—",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                val phone = data?.mobile_app?.let { "+91-$it" } ?: ""
                val contact = data?.contact_name ?: ""
                val subtitle = listOf(phone, contact).filter { it.isNotBlank() }.joinToString(" · ")
                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                }
            }

        }
    }
}

@Composable
private fun ManageBusinessCard(account: AccountData) {
    val ctx = LocalContext.current
    val menuItems = listOfNotNull(
        account.deliveries?.let { Triple(it, Icons.Filled.LocalShipping, { ctx.startActivity(Intent(ctx, DeliveryActivity::class.java)) }) },
        account.returns?.let { Triple(it, Icons.Filled.Replay, { ctx.startActivity(Intent(ctx, ReturnActivity::class.java)) }) },
    )
    val soloItems = listOfNotNull(
        account.targetSchemes?.let { Triple(it, Icons.Filled.TrackChanges, { ctx.startActivity(Intent(ctx, TargetSchemeActivity::class.java)) }) },
        account.expirySupport?.let { Triple(it, Icons.Filled.ReportProblem, { ctx.startActivity(Intent(ctx, ExpiryActivity::class.java)) }) },
        account.accountSettings?.let { Triple(it, Icons.Filled.Settings, { ctx.startActivity(Intent(ctx, AccountSettingsActivity::class.java)) }) },
    )

    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (menuItems.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                menuItems.forEachIndexed { index, (label, icon, onClick) ->
                    MenuRow(label = label, icon = icon, onClick = onClick)
                    if (index < menuItems.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        soloItems.forEach { (label, icon, onClick) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                MenuRow(label = label, icon = icon, onClick = onClick)
            }
        }
    }
}

@Composable
private fun MenuRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.DarkGray,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun isSupportAvailable(): Boolean {
    val now = java.util.Calendar.getInstance()
    val day = now.get(java.util.Calendar.DAY_OF_WEEK)
    val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = now.get(java.util.Calendar.MINUTE)
    val totalMinutes = hour * 60 + minute
    val isWeekday = day in java.util.Calendar.MONDAY..java.util.Calendar.SATURDAY
    return isWeekday && totalMinutes >= 9 * 60 && totalMinutes < 20 * 60
}

@Composable
private fun SupportCard(account: AccountData) {
    val ctx = LocalContext.current
    val available = remember { isSupportAvailable() }
    val number = account.helpNumber.orEmpty()

    Card(
        modifier = Modifier.padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.HeadsetMic,
                    contentDescription = "Support",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Support Hotline", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (number.isNotBlank()) {
                        Text(number, fontSize = 13.sp, color = Color.Gray)
                    }
                }
                Button(
                    onClick = {
                        if (available && number.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL,
                                Uri.parse("tel:$number"))
                            ctx.startActivity(intent)
                        }
                    },
                    enabled = available,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BRAND_RED,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFDDDDDD),
                        disabledContentColor = Color(0xFF888888)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(if (available) "Call Now" else "Unavailable", fontSize = 13.sp)
                }
            }

            if (!account.helpMessage.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(YELLOW_INFO)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = account.helpMessage,
                        fontSize = 13.sp,
                        color = Color(0xFF555555)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppInfoCard(ctx: Context) {
    var cacheSizeMb by rememberSaveable { mutableStateOf("0") }
    LaunchedEffect(Unit) {
        val bytes = ctx.cacheDir?.let { folderSize(it) } ?: 0L
        cacheSizeMb = if (bytes < 1024 * 1024) "0" else String.format("%.1f", bytes / (1024.0 * 1024.0))
    }

    val versionName = try {
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "—"
    } catch (e: Exception) { "—" }

    Card(
        modifier = Modifier.padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("App Version", fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(
                    text = "App is up to date!",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = versionName,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp, top = 0.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        ctx.cacheDir?.deleteRecursively()
                        cacheSizeMb = "0"
                    }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Clear Cache", fontSize = 15.sp)
                    Text("$cacheSizeMb MB", fontSize = 12.sp, color = Color.Gray)
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun folderSize(dir: File): Long {
    if (!dir.exists()) return 0L
    return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

@Composable
private fun AccountBottomBar() {
    var selectedIndex by rememberSaveable { mutableStateOf(3) }
    val ctx = LocalContext.current

    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        AccountNavItem(
            label = "HOME",
            icon = Icons.Filled.Home,
            selected = selectedIndex == 0,
            selectedColor = BRAND_RED
        ) { selectedIndex = 0 /* TODO: open HomeActivity */ }

        AccountNavItem(
            label = "CATEGORIES",
            icon = Icons.Filled.GridView,
            selected = selectedIndex == 1,
            selectedColor = BRAND_RED
        ) { selectedIndex = 1 /* TODO: open CategoriesActivity */ }

        AccountNavItem(
            label = "CART",
            icon = Icons.Filled.ShoppingCart,
            selected = selectedIndex == 2,
            selectedColor = BRAND_RED
        ) { selectedIndex = 2 /* TODO: open CartDetailsActivity */ }

        AccountNavItem(
            label = "ACCOUNT",
            icon = Icons.Filled.AccountCircle,
            selected = selectedIndex == 3,
            selectedColor = BRAND_RED
        ) { selectedIndex = 3 }
    }
}

@Composable
private fun RowScope.AccountNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(imageVector = icon, contentDescription = label)
        },
        label = { Text(label, fontSize = 10.sp) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            indicatorColor = Color.Transparent
        )
    )
}
