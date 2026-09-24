package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.ui.components.ConversationStatusBadge
import com.example.ui.components.LeadScoreBadge
import com.example.ui.theme.*

@Composable
fun SimulatorScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val leads by viewModel.leads.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val selectedConvId by viewModel.selectedConversationId.collectAsStateWithLifecycle()

    var clientInputText by remember { mutableStateOf("") }
    val activeConv = conversations.firstOrNull { it.id == selectedConvId } ?: conversations.firstOrNull()
    val activeContact = contacts.firstOrNull { it.id == activeConv?.contactId }
    val activeLead = leads.firstOrNull { it.contactId == activeContact?.id }

    val quickSimPrompts = listOf(
        "Combien coûte l'UltraPhone Nova X ?",
        "Je cherche un casque pour moins de 150€",
        "La livraison est-elle offerte ?",
        "Je veux passer commande tout de suite !",
        "Je vais réfléchir...",
        "Je veux parler à un responsable humain !"
    )

    Column(modifier = modifier.fillMaxSize().padding(14.dp).testTag("simulator_screen")) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Simulateur WhatsApp Client", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Testez les réponses de l'IA commerciale comme si vous étiez un prospect",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }

            Surface(
                color = WhatsAppGreenLight.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WhatsAppGreenDark))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Sandbox Live", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WhatsAppGreenDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick prompts suggestions
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(quickSimPrompts) { prompt ->
                SuggestionChip(
                    onClick = { clientInputText = prompt },
                    label = { Text(prompt, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live CRM Inspector Card
        if (activeLead != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Inspecteur Lead CRM : ${activeContact?.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "Étape : ${activeLead.stage} · Urgence : ${activeLead.urgency}", fontSize = 11.sp, color = Slate600)
                    }
                    LeadScoreBadge(score = activeLead.score)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // WhatsApp Phone Frame
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, WhatsAppGreenDark.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECE5DD)) // WhatsApp wallpaper color
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Phone Top App Bar
                Surface(
                    color = WhatsAppGreenDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Assistant Commercial (NexStore)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "En ligne · Réponse instantanée", color = WhatsAppGreenLight, fontSize = 10.sp)
                        }
                    }
                }

                // Chat Messages
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentMessages, key = { it.id }) { msg ->
                        MessageBubble(msg = msg, currency = "EUR")
                    }
                }

                // Client Input Area (Simulate Prospect)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = clientInputText,
                            onValueChange = { clientInputText = it },
                            placeholder = { Text("Écrire en tant que client...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f).testTag("simulator_input_text"),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledIconButton(
                            onClick = {
                                if (clientInputText.isNotBlank() && activeConv != null) {
                                    viewModel.receiveClientMessage(activeConv.id, clientInputText)
                                    clientInputText = ""
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = WhatsAppGreenDark),
                            modifier = Modifier.testTag("simulator_btn_send")
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Envoyer client", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
