package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavDestination
import com.example.ui.SalesSaaSViewModel
import com.example.ui.theme.*

@Composable
fun LandingPageScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp).testTag("landing_page_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppGreenDark)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp)) {
                        Text(
                            text = "🚀 SAAS COMMERCIAL WHATSAPP + IA",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Votre assistant commercial IA travaille sur WhatsApp 24h/24.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Répondez automatiquement à vos clients, qualifiez vos prospects, relancez-les et transformez davantage de conversations WhatsApp en ventes.",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.navigateTo(AppNavDestination.INBOX) },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenLight, contentColor = Slate900),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Essai Gratuit", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(AppNavDestination.SIMULATOR) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Voir la Démo")
                        }
                    }
                }
            }
        }

        // 3 Key Value Props
        item {
            Text(text = "Pourquoi choisir notre plateforme ?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        val benefits = listOf(
            Triple(Icons.Default.Bolt, "Zéro Temps d'Attente", "Vos clients obtiennent des réponses en moins de 3 secondes, 24h/24 et 7j/7 même la nuit."),
            Triple(Icons.Default.FilterAlt, "Qualification Automatique", "L'IA pose les questions clés (budget, besoin, urgence) et attribue un score prospect de 0 à 100."),
            Triple(Icons.Default.Repeat, "Relances Intelligentes", "Ne perdez plus aucune vente. Relances douces programmées à 24h, 3j et 7j sans spam.")
        )

        items(benefits.size) { i ->
            val (icon, title, desc) = benefits[i]
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(WhatsAppGreenDark.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = WhatsAppGreenDark)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = desc, fontSize = 13.sp, color = Slate600, lineHeight = 18.sp)
                    }
                }
            }
        }

        // CTA Final
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = AiVioletLight.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Prêt à booster vos ventes WhatsApp ?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AiVioletDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Configurez votre assistant commercial en 5 minutes.", fontSize = 13.sp, color = Slate700)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.navigateTo(AppNavDestination.ONBOARDING) },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Démarrer l'Onboarding")
                    }
                }
            }
        }
    }
}
