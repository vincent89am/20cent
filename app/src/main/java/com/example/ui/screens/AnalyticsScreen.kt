package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.SalesSaaSViewModel
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val leads by viewModel.leads.collectAsStateWithLifecycle()
    val organization by viewModel.organization.collectAsStateWithLifecycle()
    val currency = organization?.currency ?: "EUR"

    val totalCA = orders.sumOf { it.totalAmount }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(14.dp).testTag("analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(text = "Rapports & Analytics IA", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Mesure précise du ROI commercial et des performances WhatsApp", fontSize = 12.sp, color = Slate600)
            }
        }

        // Top KPIs
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Chiffre d'Affaires",
                    value = "%.0f %s".format(totalCA, currency),
                    subtitle = "100% attribué WhatsApp",
                    icon = Icons.Default.TrendingUp,
                    color = WhatsAppGreenDark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Temps de Réponse",
                    value = "2.4s",
                    subtitle = "Vs 18m pour un humain",
                    icon = Icons.Default.Speed,
                    color = BlueInfo,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Intent Distribution Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Répartition des Intentions Clients Détectées", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    val intents = listOf(
                        Triple("Demandes de Prix & Promo", 38, WhatsAppGreenDark),
                        Triple("Recommandations Produits", 24, AiViolet),
                        Triple("Livraison & Frais de port", 18, BlueInfo),
                        Triple("Intention d'achat / Paiement", 12, GreenSuccess),
                        Triple("Prise de rendez-vous", 5, GoldWarning),
                        Triple("Escalade humaine requise", 3, RedDanger)
                    )

                    intents.forEach { (name, percent, color) ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(text = "$percent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { percent / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = color,
                                trackColor = Slate100
                            )
                        }
                    }
                }
            }
        }

        // AI Consumption & Quota Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Consommation IA & Quota Forfait (${organization?.plan ?: "PRO"})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Messages IA générés ce mois :", fontSize = 12.sp, color = Slate700)
                        Text(text = "1 482 / 5 000", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 1482f / 5000f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = WhatsAppGreenDark,
                        trackColor = Slate100
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "💡 Vous utilisez actuellement 29.6% de votre quota mensuel. Aucune surcharge à prévoir.",
                        fontSize = 11.sp,
                        color = Slate600
                    )
                }
            }
        }
    }
}
