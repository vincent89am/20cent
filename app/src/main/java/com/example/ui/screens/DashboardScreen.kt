package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppNavDestination
import com.example.ui.SalesSaaSViewModel
import com.example.ui.components.ConversationStatusBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val organization by viewModel.organization.collectAsStateWithLifecycle()
    val period by viewModel.dashboardPeriod.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val leads by viewModel.leads.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()

    val currency = organization?.currency ?: "EUR"
    val humanReqCount = conversations.count { it.status == "HUMAN_REQUIRED" }
    val totalRevenue = orders.filter { it.status != "CANCELLED" }.sumOf { it.totalAmount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Welcome & Status Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_header_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppGreenDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = organization?.name ?: "Mon Entreprise SaaS",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Assistant Commercial IA · En ligne 24/7",
                                color = WhatsAppGreenLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(WhatsAppGreenLight)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WhatsApp Connecté",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Numéro officiel : ${organization?.whatsappNumber ?: "+33 6 88 12 34 56"} · Plan : ${organization?.plan ?: "PRO"}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Period Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val periods = listOf("Aujourd'hui" to "TODAY", "7 Jours" to "7J", "30 Jours" to "30J", "90 Jours" to "90J")
                periods.forEach { (label, code) ->
                    val isSelected = period == code
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setDashboardPeriod(code) },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WhatsAppGreenDark,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("period_chip_$code")
                    )
                }
            }
        }

        // Human Intervention Alert (if any)
        if (humanReqCount > 0) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppNavDestination.INBOX) }
                        .testTag("human_alert_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RedDanger.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = RedDanger,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Intervention humaine requise ($humanReqCount)",
                                color = RedDanger,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Un client réclame une assistance personnalisée ou une réclamation.",
                                color = Slate700,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Ouvrir",
                            tint = RedDanger
                        )
                    }
                }
            }
        }

        // Executive AI Summary Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = AiVioletLight.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AiViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rapport Hebdomadaire IA Commerciale",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AiVioletDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cette semaine, les interactions WhatsApp ont généré ${orders.size} commandes et ${appointments.size} rendez-vous confirmés. 88% des demandes (prix, disponibilité, livraison) ont été traitées de manière 100% autonome par l'IA. Les modèles les plus consultés sont UltraPhone Nova X et Casque SoundPro Max.",
                        fontSize = 13.sp,
                        color = Slate800,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // Primary Stats Grid (2 columns)
        item {
            Text(
                text = "Indicateurs Clés de Vente",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "CA WhatsApp",
                        value = "%.0f %s".format(totalRevenue, currency),
                        subtitle = "+32% vs période précédente",
                        icon = Icons.Default.AttachMoney,
                        color = WhatsAppGreenDark,
                        trendPercent = "+32%",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Conversations",
                        value = "${conversations.size + 14}",
                        subtitle = "Temps moyen: 3s",
                        icon = Icons.Default.Chat,
                        color = BlueInfo,
                        trendPercent = "+18%",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Prospects Chauds",
                        value = "${leads.count { it.score >= 71 }}",
                        subtitle = "Sur ${leads.size} leads actifs",
                        icon = Icons.Default.LocalFireDepartment,
                        color = GoldWarning,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Taux de Conversion",
                        value = "28.5%",
                        subtitle = "Objectif: 25%",
                        icon = Icons.Default.CheckCircle,
                        color = GreenSuccess,
                        trendPercent = "+4.2%",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Réponses IA",
                        value = "148",
                        subtitle = "Autonomie : 92%",
                        icon = Icons.Default.SmartToy,
                        color = AiViolet,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Rendez-vous",
                        value = "${appointments.size}",
                        subtitle = "Planifiés avec l'IA",
                        icon = Icons.Default.CalendarMonth,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Activity Graph Simulation (Visual CSS/Compose Bar Chart)
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_chart_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Volume des conversations (7 derniers jours)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(text = "Total : 162", fontSize = 12.sp, color = Slate500)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    val days = listOf("Lun" to 14, "Mar" to 22, "Mer" to 28, "Jeu" to 19, "Ven" to 35, "Sam" to 26, "Dim" to 18)
                    val maxVal = 35

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        days.forEach { (day, count) ->
                            val heightFraction = count.toFloat() / maxVal.toFloat()
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "$count",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (count == maxVal) WhatsAppGreenDark else Slate500
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(heightFraction.coerceIn(0.15f, 1f))
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            if (count == maxVal) WhatsAppGreenLight
                                            else WhatsAppGreenDark.copy(alpha = 0.35f)
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Demand Products
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Produits les plus demandés sur WhatsApp",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    products.take(3).forEach { prod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${prod.category} · Stock : ${prod.stock} unités",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                            Text(
                                text = "%.0f %s".format(prod.promoPrice ?: prod.price, currency),
                                fontWeight = FontWeight.Bold,
                                color = WhatsAppGreenDark,
                                fontSize = 13.sp
                            )
                        }
                        if (prod != products.take(3).last()) {
                            HorizontalDivider(color = Slate200.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(AppNavDestination.INBOX) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_btn_inbox"),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Boîte de réception", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppNavDestination.SIMULATOR) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_btn_simulator"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simulateur", fontSize = 12.sp)
                }
            }
        }
    }
}
