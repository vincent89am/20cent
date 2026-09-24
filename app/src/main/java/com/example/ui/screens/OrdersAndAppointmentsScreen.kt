package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppointmentEntity
import com.example.data.local.OrderEntity
import com.example.ui.SalesSaaSViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersAndAppointmentsScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val organization by viewModel.organization.collectAsStateWithLifecycle()
    val currency = organization?.currency ?: "EUR"

    var selectedTab by remember { mutableStateOf(0) } // 0: Commandes, 1: Rendez-vous

    Column(modifier = modifier.fillMaxSize().padding(14.dp).testTag("orders_appointments_screen")) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = WhatsAppGreenDark
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Commandes WhatsApp (${orders.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Rendez-vous (${appointments.size})", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Orders List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderItemCard(order = order, currency = currency)
                }
            }
        } else {
            // Appointments List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(appointments, key = { it.id }) { apt ->
                    AppointmentItemCard(apt = apt)
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(order: OrderEntity, currency: String) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy à HH:mm", Locale.FRANCE)
    val formattedDate = dateFormat.format(Date(order.createdAt))

    val (statusColor, statusLabel) = when (order.status) {
        "CONFIRMED" -> Pair(GreenSuccess, "Confirmée")
        "PREPARING" -> Pair(BlueInfo, "En préparation")
        "SHIPPED" -> Pair(AiViolet, "Expédiée")
        "DELIVERED" -> Pair(WhatsAppGreenDark, "Livrée")
        "CANCELLED" -> Pair(RedDanger, "Annulée")
        else -> Pair(GoldWarning, "Nouvelle")
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = formattedDate, fontSize = 11.sp, color = Slate500)
                }

                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Articles : ${order.itemsSummary}", fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.Medium)
            if (order.shippingAddress.isNotBlank()) {
                Text(text = "Livraison : ${order.shippingAddress}", fontSize = 12.sp, color = Slate600)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = Slate600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${order.paymentMethod} · ${order.paymentStatus}", fontSize = 11.sp, color = Slate600)
                }

                Text(
                    text = "Total : %.2f %s".format(order.totalAmount, currency),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = WhatsAppGreenDark
                )
            }
        }
    }
}

@Composable
fun AppointmentItemCard(apt: AppointmentEntity) {
    val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy à HH:mm", Locale.FRANCE)
    val formattedDate = dateFormat.format(Date(apt.scheduledTime))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().testTag("appointment_card_${apt.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = apt.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(
                    color = if (apt.status == "CONFIRMED") GreenSuccess.copy(alpha = 0.15f) else GoldWarning.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (apt.status == "CONFIRMED") "Confirmé ✅" else "En attente",
                        color = if (apt.status == "CONFIRMED") GreenSuccess else GoldWarning,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Slate600, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = apt.clientName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = Slate600, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = formattedDate, fontSize = 12.sp, color = Slate700)
            }

            if (apt.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Notes : ${apt.notes}", fontSize = 11.sp, color = Slate500)
            }
        }
    }
}
