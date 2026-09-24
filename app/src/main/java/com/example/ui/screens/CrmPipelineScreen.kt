package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.local.ContactEntity
import com.example.data.local.LeadEntity
import com.example.ui.SalesSaaSViewModel
import com.example.ui.components.CrmStageBadge
import com.example.ui.components.LeadScoreBadge
import com.example.ui.theme.*

@Composable
fun CrmPipelineScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val leads by viewModel.leads.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val organization by viewModel.organization.collectAsStateWithLifecycle()

    var isKanbanView by remember { mutableStateOf(true) }
    var selectedLeadForEdit by remember { mutableStateOf<LeadEntity?>(null) }
    var editNotesText by remember { mutableStateOf("") }

    val stages = listOf(
        "NEW" to "Nouveau",
        "CONTACTED" to "Contacté",
        "QUALIFIED" to "Qualifié",
        "PROPOSAL" to "Proposition",
        "NEGOTIATION" to "Négociation",
        "WON" to "Gagné 🎉",
        "LOST" to "Perdu"
    )

    Column(modifier = modifier.fillMaxSize().padding(14.dp).testTag("crm_pipeline_screen")) {
        // Header & View Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "CRM & Qualification Prospects", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${leads.size} opportunités actives · Suivi automatique par IA",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(2.dp)) {
                    IconButton(
                        onClick = { isKanbanView = true },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewKanban,
                            contentDescription = "Vue Kanban",
                            tint = if (isKanbanView) WhatsAppGreenDark else Slate500
                        )
                    }
                    IconButton(
                        onClick = { isKanbanView = false },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Vue Liste",
                            tint = if (!isKanbanView) WhatsAppGreenDark else Slate500
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isKanbanView) {
            // Horizontal scrollable Kanban Board
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                stages.forEach { (stageKey, stageLabel) ->
                    val leadsInStage = leads.filter { it.stage == stageKey }
                    KanbanColumn(
                        stageKey = stageKey,
                        stageLabel = stageLabel,
                        leads = leadsInStage,
                        contacts = contacts,
                        currency = organization?.currency ?: "EUR",
                        onMoveLead = { lead, nextStage ->
                            viewModel.updateLeadStage(lead.id, nextStage)
                        },
                        onEditLead = { lead ->
                            selectedLeadForEdit = lead
                            editNotesText = lead.notes
                        }
                    )
                }
            }
        } else {
            // List View
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leads, key = { it.id }) { lead ->
                    val contact = contacts.firstOrNull { it.id == lead.contactId }
                    LeadListCard(
                        lead = lead,
                        contact = contact,
                        currency = organization?.currency ?: "EUR",
                        onEdit = {
                            selectedLeadForEdit = lead
                            editNotesText = lead.notes
                        }
                    )
                }
            }
        }
    }

    // Lead Edit Dialog (Notes & Score)
    if (selectedLeadForEdit != null) {
        val lead = selectedLeadForEdit!!
        val contact = contacts.firstOrNull { it.id == lead.contactId }
        AlertDialog(
            onDismissRequest = { selectedLeadForEdit = null },
            title = {
                Text(text = "Détails Prospect : ${contact?.name ?: ""}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Score IA : ${lead.score}/100", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = lead.score.toFloat(),
                        onValueChange = { viewModel.updateLeadScore(lead.id, it.toInt()) },
                        valueRange = 0f..100f
                    )

                    Text(text = "Changer d'étape :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        stages.forEach { (key, lbl) ->
                            FilterChip(
                                selected = lead.stage == key,
                                onClick = { viewModel.updateLeadStage(lead.id, key) },
                                label = { Text(lbl, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editNotesText,
                        onValueChange = { editNotesText = it },
                        label = { Text("Notes commerciales") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateLeadNotes(lead.id, editNotesText)
                        selectedLeadForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark)
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLeadForEdit = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun KanbanColumn(
    stageKey: String,
    stageLabel: String,
    leads: List<LeadEntity>,
    contacts: List<ContactEntity>,
    currency: String,
    onMoveLead: (LeadEntity, String) -> Unit,
    onEditLead: (LeadEntity) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stageLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = WhatsAppGreenDark.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "${leads.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppGreenDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leads, key = { it.id }) { lead ->
                    val contact = contacts.firstOrNull { it.id == lead.contactId }
                    KanbanCard(
                        lead = lead,
                        contact = contact,
                        currency = currency,
                        onMove = { nextStage -> onMoveLead(lead, nextStage) },
                        onEdit = { onEditLead(lead) }
                    )
                }
            }
        }
    }
}

@Composable
fun KanbanCard(
    lead: LeadEntity,
    contact: ContactEntity?,
    currency: String,
    onMove: (String) -> Unit,
    onEdit: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("kanban_card_${lead.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact?.name ?: "Prospect",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                LeadScoreBadge(score = lead.score)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lead.needDescription.ifEmpty { "Besoin en cours de qualification" },
                fontSize = 11.sp,
                color = Slate700,
                maxLines = 2
            )

            if (lead.estimatedBudget > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Budget: %.0f %s".format(lead.estimatedBudget, currency),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WhatsAppGreenDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Urgence: ${lead.urgency}",
                    fontSize = 10.sp,
                    color = Slate500
                )

                Row {
                    IconButton(
                        onClick = {
                            val next = when (lead.stage) {
                                "NEW" -> "CONTACTED"
                                "CONTACTED" -> "QUALIFIED"
                                "QUALIFIED" -> "PROPOSAL"
                                "PROPOSAL" -> "NEGOTIATION"
                                "NEGOTIATION" -> "WON"
                                else -> "WON"
                            }
                            onMove(next)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Avancer",
                            tint = WhatsAppGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeadListCard(
    lead: LeadEntity,
    contact: ContactEntity?,
    currency: String,
    onEdit: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact?.name ?: "Prospect",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CrmStageBadge(stage = lead.stage)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lead.needDescription.ifEmpty { "Pas de détail" },
                    fontSize = 12.sp,
                    color = Slate600
                )
                Text(
                    text = "${contact?.phoneNumber ?: ""} · Budget : %.0f %s".format(lead.estimatedBudget, currency),
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            LeadScoreBadge(score = lead.score)
        }
    }
}
