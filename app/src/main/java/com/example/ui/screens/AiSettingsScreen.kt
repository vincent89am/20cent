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
import com.example.data.local.KnowledgeDocumentEntity
import com.example.ui.SalesSaaSViewModel
import com.example.ui.theme.*

@Composable
fun AiSettingsScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val aiSettings by viewModel.aiSettings.collectAsStateWithLifecycle()
    val knowledgeDocs by viewModel.knowledgeDocs.collectAsStateWithLifecycle()

    var name by remember(aiSettings) { mutableStateOf(aiSettings?.assistantName ?: "Aria Commerciale") }
    var tone by remember(aiSettings) { mutableStateOf(aiSettings?.tone ?: "Professionnel et chaleureux") }
    var mission by remember(aiSettings) { mutableStateOf(aiSettings?.mission ?: "Guider le prospect vers l'achat et conclure la vente.") }
    var isCopilot by remember(aiSettings) { mutableStateOf(aiSettings?.isCopilotMode ?: false) }

    var showAddDocDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(14.dp).testTag("ai_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(text = "Configuration de l'IA Commerciale", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Personnalité, mission et base de connaissances RAG", fontSize = 12.sp, color = Slate600)
            }
        }

        // Mode switch Card (Auto vs Copilot)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isCopilot) AiVioletLight.copy(alpha = 0.5f) else WhatsAppGreenLight.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCopilot) "Mode Copilote (Validation humaine)" else "Mode 100% Automatique",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isCopilot) AiVioletDark else WhatsAppGreenDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isCopilot) "L'IA prépare des propositions de réponses qu'un commercial valide avant envoi." else "L'IA analyse et répond en direct aux prospects sur WhatsApp.",
                            fontSize = 12.sp,
                            color = Slate700
                        )
                    }

                    Switch(
                        checked = isCopilot,
                        onCheckedChange = {
                            isCopilot = it
                            viewModel.updateAiSettings(name, tone, mission, it)
                        }
                    )
                }
            }
        }

        // Identity Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Identité & Mission", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom de l'assistant") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tone,
                        onValueChange = { tone = it },
                        label = { Text("Ton & Style commercial") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = mission,
                        onValueChange = { mission = it },
                        label = { Text("Mission & Objectif commercial") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Button(
                        onClick = { viewModel.updateAiSettings(name, tone, mission, isCopilot) },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Enregistrer l'identité")
                    }
                }
            }
        }

        // Knowledge Base (RAG)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Base de Connaissances RAG (${knowledgeDocs.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "FAQ, politiques de livraison et conditions commerciales", fontSize = 11.sp, color = Slate600)
                }

                Button(
                    onClick = { showAddDocDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter Doc", fontSize = 11.sp)
                }
            }
        }

        items(knowledgeDocs, key = { it.id }) { doc ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = doc.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        IconButton(onClick = { viewModel.deleteKnowledgeDoc(doc) }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = RedDanger, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = doc.content, fontSize = 12.sp, color = Slate700)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(color = Slate100, shape = RoundedCornerShape(4.dp)) {
                        Text(text = "Tags : ${doc.tags} · Type : ${doc.type}", fontSize = 10.sp, color = Slate600, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }

    if (showAddDocDialog) {
        var docTitle by remember { mutableStateOf("") }
        var docContent by remember { mutableStateOf("") }
        var docTags by remember { mutableStateOf("Général") }
        var docType by remember { mutableStateOf("FAQ") }

        AlertDialog(
            onDismissRequest = { showAddDocDialog = false },
            title = { Text("Ajouter à la base de connaissances", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = docTitle, onValueChange = { docTitle = it }, label = { Text("Titre du document *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = docContent, onValueChange = { docContent = it }, label = { Text("Contenu d'information pour l'IA *") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
                    OutlinedTextField(value = docTags, onValueChange = { docTags = it }, label = { Text("Mots-clés / Tags") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (docTitle.isNotBlank() && docContent.isNotBlank()) {
                            viewModel.addKnowledgeDoc(docTitle, docContent, docType, docTags)
                            showAddDocDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark)
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDocDialog = false }) { Text("Annuler") }
            }
        )
    }
}
