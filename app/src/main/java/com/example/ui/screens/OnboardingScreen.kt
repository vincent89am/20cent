package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun OnboardingScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val step by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val organization by viewModel.organization.collectAsStateWithLifecycle()

    var companyName by remember { mutableStateOf(organization?.name ?: "NexStore Commercial") }
    var industry by remember { mutableStateOf(organization?.industry ?: "Boutique & Électronique") }
    var currency by remember { mutableStateOf(organization?.currency ?: "EUR") }
    var goal by remember { mutableStateOf(organization?.commercialGoal ?: "Vendre davantage sur WhatsApp et qualifier 24/7") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("onboarding_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Stepper indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configuration initiale SaaS",
                    fontSize = 13.sp,
                    color = Slate600,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Étape $step / 8",
                    fontSize = 13.sp,
                    color = WhatsAppGreenDark,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { step / 8f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = WhatsAppGreenDark,
                trackColor = Slate200
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content
            when (step) {
                1 -> {
                    Text(text = "Quel est le nom de votre entreprise ?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Ce nom sera mentionné par l'assistant lors des échanges avec vos clients.", fontSize = 13.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Nom de l'entreprise") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                2 -> {
                    Text(text = "Quel est votre secteur d'activité ?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Permet d'adapter le vocabulaire de vente et la qualification des leads.", fontSize = 13.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(16.dp))
                    val sectors = listOf("Boutique de vêtements", "Magasin électronique & High-Tech", "Restaurant & Traiteur", "Pharmacie & Cosmétique", "Agence immobilière / Services B2B", "Freelance & Formateur")
                    sectors.forEach { sec ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = industry == sec, onClick = { industry = sec })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = sec, fontSize = 13.sp)
                        }
                    }
                }
                3 -> {
                    Text(text = "Pays et devise principale", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Choisissez la devise affichée sur vos devis et liens de paiement WhatsApp.", fontSize = 13.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(16.dp))
                    val currencies = listOf("EUR (€) - Union Européenne", "FCFA (XOF / XAF) - Afrique de l'Ouest / Centrale", "USD ($) - International", "MAD (DH) - Maroc")
                    currencies.forEach { cur ->
                        val code = cur.take(3)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            RadioButton(selected = currency == code, onClick = { currency = code })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = cur, fontSize = 13.sp)
                        }
                    }
                }
                4 -> {
                    Text(text = "Quel est votre objectif commercial principal ?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Objectif commercial") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
                5 -> {
                    Text(text = "Connexion WhatsApp Business Platform", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Connexion officielle via Meta Cloud API sécurisée.", fontSize = 13.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = WhatsAppGreenDark.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = WhatsAppGreenDark)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Passerelle WhatsApp connectée (+33 6 88 12 34 56)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                6 -> {
                    Text(text = "Catalogue de vente", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Vos 4 premiers articles ont été pré-configurés avec prix, photos et stocks pour l'IA.", fontSize = 13.sp, color = Slate600)
                }
                7 -> {
                    Text(text = "Configuration de votre assistant IA", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "L'IA 'Aria Commerciale' est prête avec le ton : Professionnel & Chaleureux.", fontSize = 13.sp, color = Slate600)
                }
                8 -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(WhatsAppGreenDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Votre assistant commercial IA est prêt !",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WhatsAppGreenDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Il est désormais opérationnel pour recevoir des messages, qualifier vos prospects et enregistrer des ventes.",
                                fontSize = 13.sp,
                                color = Slate700,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Stepper Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (step > 1) {
                OutlinedButton(onClick = { viewModel.prevOnboardingStep() }) {
                    Text("Précédent")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Button(
                onClick = { viewModel.nextOnboardingStep() },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                modifier = Modifier.testTag("onboarding_btn_next")
            ) {
                Text(if (step == 8) "Accéder au Dashboard 🚀" else "Continuer")
            }
        }
    }
}
