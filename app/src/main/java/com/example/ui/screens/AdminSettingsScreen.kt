package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.theme.*

@Composable
fun AdminSettingsScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val organization by viewModel.organization.collectAsStateWithLifecycle()
    val teamMembers by viewModel.teamMembers.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: WhatsApp Cloud API, 1: Équipe, 2: Abonnement, 3: Super-Admin

    Column(modifier = modifier.fillMaxSize().padding(14.dp).testTag("admin_settings_screen")) {
        Text(text = "Paramètres & Administration SaaS", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = "Configuration multi-tenant, intégration officielle Meta et facturation", fontSize = 12.sp, color = Slate600)

        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = activeTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = WhatsAppGreenDark
        ) {
            val tabs = listOf("WhatsApp API", "Équipe & Rôles", "Abonnement", "Super-Admin SaaS")
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = { Text(title, fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (activeTab) {
            0 -> WhatsAppApiConfigView(organizationName = organization?.name ?: "NexStore")
            1 -> TeamMembersView(teamMembers = teamMembers)
            2 -> SubscriptionsView(currentPlan = organization?.plan ?: "PRO")
            3 -> SuperAdminSaasView()
        }
    }
}

@Composable
fun WhatsAppApiConfigView(organizationName: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Statut WhatsApp Business Platform", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(color = GreenSuccess.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)) {
                    Text(text = "Opérationnel 🟢", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = "104928374928174",
                onValueChange = {},
                readOnly = true,
                label = { Text("Phone Number ID (Meta Cloud API)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "948271630192847",
                onValueChange = {},
                readOnly = true,
                label = { Text("WhatsApp Business Account ID (WABA)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "https://api.nexstore-saas.com/webhooks/whatsapp",
                onValueChange = {},
                readOnly = true,
                label = { Text("Webhook Callback URL (Meta Developers)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "wh_verify_nexstore_sec_99182",
                onValueChange = {},
                readOnly = true,
                label = { Text("Webhook Verification Token") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tester la connexion Webhook")
            }
        }
    }
}

@Composable
fun TeamMembersView(teamMembers: List<com.example.data.local.TeamMemberEntity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(teamMembers, key = { it.id }) { member ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = member.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = member.email, fontSize = 11.sp, color = Slate500)
                    }

                    Surface(
                        color = when (member.role) {
                            "OWNER" -> Color(0xFFFEF3C7)
                            "ADMIN" -> Color(0xFFE0E7FF)
                            "COMMERCIAL" -> Color(0xFFDCFCE7)
                            else -> Slate100
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = member.role,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (member.role) {
                                "OWNER" -> Color(0xFFB45309)
                                "ADMIN" -> Color(0xFF4338CA)
                                "COMMERCIAL" -> Color(0xFF15803D)
                                else -> Slate700
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionsView(currentPlan: String) {
    val plans = listOf(
        Triple("STARTER", "29€ / mois", listOf("1 numéro WhatsApp", "500 conversations / mois", "Assistant IA standard", "Catalogue produits de base")),
        Triple("PRO", "79€ / mois", listOf("3 numéros WhatsApp", "5 000 conversations / mois", "Mode Copilote & Automatique", "Moteur de relances 24h/3j/7j", "Rendez-vous et CRM")),
        Triple("BUSINESS", "199€ / mois", listOf("Numéros illimités", "Conversations illimitées", "Équipe complète multi-rôles", "API personnalisée & Webhooks", "Support 24/7 dédié"))
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(plans) { (name, price, features) ->
            val isCurrent = currentPlan == name
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(if (isCurrent) 2.dp else 0.dp, WhatsAppGreenDark, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isCurrent) WhatsAppGreenLight.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Plan $name", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = price, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = WhatsAppGreenDark)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    features.forEach { feat ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = feat, fontSize = 12.sp, color = Slate700)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {},
                        enabled = !isCurrent,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isCurrent) "Forfait Actuel" else "Choisir ce forfait")
                    }
                }
            }
        }
    }
}

@Composable
fun SuperAdminSaasView() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "Tableau de Bord Fondateur (Master SaaS)", fontWeight = FontWeight.Bold, fontSize = 15.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ElevatedCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "MRR Global", fontSize = 11.sp, color = Slate600)
                    Text(text = "42 850 €", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = WhatsAppGreenDark)
                    Text(text = "+14.2% ce mois", fontSize = 10.sp, color = GreenSuccess)
                }
            }
            ElevatedCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Entreprises Actives", fontSize = 11.sp, color = Slate600)
                    Text(text = "314", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BlueInfo)
                    Text(text = "98.5% rétention", fontSize = 10.sp, color = Slate600)
                }
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Santé Système & Passerelles WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "• Meta Graph API v21.0 : 100% opérationnel (latence: 180ms)\n• Moteur de qualification IA : 99.98% uptime\n• Passerelles Mobile Money & Stripe : Connectées", fontSize = 11.sp, color = Slate700)
            }
        }
    }
}
