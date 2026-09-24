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
import com.example.data.local.AutomationWorkflowEntity
import com.example.ui.SalesSaaSViewModel
import com.example.ui.theme.*

@Composable
fun AutomationsScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val workflows by viewModel.workflows.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(14.dp).testTag("automations_screen")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Automatisations & Relances WhatsApp", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Moteur de relances intelligentes et actions déclenchées", fontSize = 12.sp, color = Slate600)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nouveau Workflow", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Relance sequence info banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppGreenDark.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Repeat, contentDescription = null, tint = WhatsAppGreenDark, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Séquence de Relance IA Multi-Étapes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WhatsAppGreenDark)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Relance 1 (+24h) : Délicate vérification de disponibilité avec mention du modèle discuté.\n• Relance 2 (+3 jours) : Proposition d'avantage commercial ou réponse aux doutes.\n• Relance 3 (+7 jours) : Ultime relance de clôture avant mise en sommeil.\n\n🛡️ Conforme aux exigences WhatsApp Cloud API pour éviter les signalements spam.",
                    fontSize = 12.sp,
                    color = Slate700,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "Workflows Actifs (${workflows.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(workflows, key = { it.id }) { wf ->
                WorkflowItemCard(wf = wf, onToggle = { viewModel.toggleWorkflow(wf) })
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var trigger by remember { mutableStateOf("NO_REPLY_24H") }
        var action by remember { mutableStateOf("AI_FOLLOW_UP") }
        var delayHours by remember { mutableStateOf("24") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Créer une nouvelle automatisation", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom de la règle") }, modifier = Modifier.fillMaxWidth())

                    Text(text = "Déclencheur (SI...) :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val triggers = listOf("NO_REPLY_24H" to "Client ne répond pas après 24h", "PRICE_REQUEST" to "Demande de prix", "QUALIFIED_LEAD" to "Prospect qualifié score > 80")
                    triggers.forEach { (code, lbl) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = trigger == code, onClick = { trigger = code })
                            Text(text = lbl, fontSize = 12.sp)
                        }
                    }

                    Text(text = "Action (ALORS...) :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val actions = listOf("AI_FOLLOW_UP" to "Envoyer relance personnalisée IA", "SEND_TEMPLATE" to "Envoyer template WhatsApp", "NOTIFY_HUMAN" to "Notifier un commercial")
                    actions.forEach { (code, lbl) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = action == code, onClick = { action = code })
                            Text(text = lbl, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(value = delayHours, onValueChange = { delayHours = it }, label = { Text("Délai d'attente (en heures)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addWorkflow(name, trigger, action, delayHours.toIntOrNull() ?: 24)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark)
                ) {
                    Text("Créer l'automatisation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
fun WorkflowItemCard(wf: AutomationWorkflowEntity, onToggle: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().testTag("workflow_card_${wf.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = wf.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Déclencheur : ${wf.triggerType} · Délai : ${wf.delayHours}h",
                    fontSize = 12.sp,
                    color = Slate600
                )
                Text(
                    text = "Action : ${wf.actionType}",
                    fontSize = 11.sp,
                    color = WhatsAppGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Switch(
                checked = wf.isActive,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(checkedThumbColor = WhatsAppGreenDark, checkedTrackColor = WhatsAppGreenLight)
            )
        }
    }
}
