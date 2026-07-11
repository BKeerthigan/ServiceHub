package com.example.servicehub.ui.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.servicehub.ui.home.HomeActivity

private val GREEN_DARK  = Color(0xFF1B5E20)
private val GREEN_MID   = Color(0xFF2E7D32)
private val GREEN_LIGHT = Color(0xFF388E3C)

class OrderConfirmedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val salesOrderId     = intent.getStringExtra("sales_order_id") ?: ""
        val salesOrderNumber = intent.getStringExtra("sales_order_number") ?: ""
        val deliveryDate     = intent.getStringExtra("delivery_date") ?: ""

        // System back press → go to Home, clear cart/payment stack
        onBackPressedDispatcher.addCallback(this) {
            startActivity(
                Intent(this@OrderConfirmedActivity, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }

        setContent {
            OrderConfirmedScreen(
                salesOrderId     = salesOrderId,
                salesOrderNumber = salesOrderNumber,
                deliveryDate     = deliveryDate,
                onViewDetails    = {
                    startActivity(
                        Intent(this, OrderDetailsActivity::class.java)
                            .putExtra("sales_order_id", salesOrderId)
                    )
                },
                onContinueShopping = {
                    startActivity(
                        Intent(this, HomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    finish()
                }
            )
        }
    }
}

@Composable
private fun OrderConfirmedScreen(
    salesOrderId: String,
    salesOrderNumber: String,
    deliveryDate: String,
    onViewDetails: () -> Unit,
    onContinueShopping: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(GREEN_DARK, GREEN_MID, GREEN_LIGHT))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Checkmark circle
            Surface(
                shape  = androidx.compose.foundation.shape.CircleShape,
                color  = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✓", fontSize = 52.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text       = "Order Confirmed!",
                fontSize   = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            if (deliveryDate.isNotBlank()) {
                Text(
                    text      = "Delivery by $deliveryDate",
                    fontSize  = 16.sp,
                    color     = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
            }

            if (salesOrderNumber.isNotBlank()) {
                Text(
                    text      = "Order ID  $salesOrderNumber",
                    fontSize  = 14.sp,
                    color     = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(56.dp))

            // View Order Details
            OutlinedButton(
                onClick  = onViewDetails,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border   = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f))
            ) {
                Text("View Order Details", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Continue Shopping
            Button(
                onClick  = onContinueShopping,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = GREEN_MID
                )
            ) {
                Text("Continue Shopping", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
