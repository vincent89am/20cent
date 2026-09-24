package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.ui.SalesSaaSViewModel
import com.example.ui.components.ConversationStatusBadge
import com.example.ui.components.CrmStageBadge
import com.example.ui.components.LeadScoreBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val leads by viewModel.leads.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val selectedConvId by viewModel.selectedConversationId.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val copilotDraft by viewModel.copilotDraft.collectAsStateWithLifecycle()

    var showProfileSheet by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }
    var filterTab by remember { mutableStateOf("ALL") } // ALL, AI, COPILOT, HUMAN

    val selectedConv = conversations.firstOrNull { it.id == selectedConvId } ?: conversations.firstOrNull()
    val selectedContact = contacts.firstOrNull { it.id == selectedConv?.contactId }
    val selectedLead = leads.firstOrNull { it.contactId == selectedContact?.id }

    val filteredConversations = when (filterTab) {
        "AI" -> conversations.filter { it.status == "AI_ACTIVE" }
        "COPILOT" -> conversations.filter { it.status == "COPILOT" }
        "HUMAN" -> conversations.filter { it.status == "HUMAN_REQUIRED" }
        else -> conversations
    }

    // Two-pane or switchable view
    var activePane by remember { mutableStateOf(if (selectedConvId != null) "CHAT" else "LIST") }

    Column(modifier = modifier.fillMaxSize().testTag("inbox_screen")) {
        // Filter tabs bar
        ScrollableTabRow(
            selectedTabIndex = when (filterTab) {
                "AI" -> 1
                "COPILOT" -> 2
                "HUMAN" -> 3
                else -> 0
            },
            edgePadding = 12.dp,
            containerColor = WhatsAppGreenDark,
            contentColor = Color.White
        ) {
            val tabs = listOf("Tous" to "ALL", "IA Active" to "AI", "Copilote" to "COPILOT", "Humain Requis ⚠️" to "HUMAN")
            tabs.forEach { (label, code) ->
                Tab(
                    selected = filterTab == code,
                    onClick = { filterTab = code },
                    text = {
                        Text(
                            text = label,
                            color = if (filterTab == code) Color.White else Color.White.copy(alpha = 0.7f),
                            fontWeight = if (filterTab == code) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Main content
        Row(modifier = Modifier.fillMaxSize()) {
            // Left list pane (conversations)
            Box(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        val contact = contacts.firstOrNull { it.id == conv.contactId }
                        val isSelected = conv.id == selectedConv?.id
                        ConversationItemRow(
                            conv = conv,
                            contactName = contact?.name ?: "Client WhatsApp",
                            contactPhone = contact?.phoneNumber ?: "",
                            isSelected = isSelected,
                            onClick = {
                                viewModel.selectConversation(conv.id)
                                activePane = "CHAT"
                            }
                        )
                        HorizontalDivider(color = Slate200.copy(alpha = 0.4f))
                    }
                }
            }

            // Vertical divider
            VerticalDivider(color = Slate200)

            // Center & Right pane (Chat Thread & Copilot)
            Column(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight()
                    .background(Color(0xFFEFEAE2)) // Classic WhatsApp light chat background
            ) {
                if (selectedConv != null && selectedContact != null) {
                    // Chat header
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showProfileSheet = true }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(WhatsAppGreenDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedContact.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = selectedContact.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ConversationStatusBadge(selectedConv.status)
                                        if (selectedLead != null) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Score: ${selectedLead.score}/100",
                                                fontSize = 10.sp,
                                                color = Slate600
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = { showProfileSheet = true },
                                modifier = Modifier.testTag("inbox_btn_profile")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profil Client",
                                    tint = WhatsAppGreenDark
                                )
                            }
                        }
                    }

                    // Messages Stream
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

                    // Copilot Suggestion Box (when available)
                    if (copilotDraft.isNotBlank()) {
                        Surface(
                            color = AiVioletLight,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("copilot_suggestion_box")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = AiViolet,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Proposition IA Copilote",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = AiVioletDark
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(
                                            onClick = { viewModel.regenerateCopilotDraft(selectedConv.id) },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Régénérer", fontSize = 11.sp, color = AiViolet)
                                        }
                                    }
                                }

                                Text(
                                    text = copilotDraft,
                                    fontSize = 12.sp,
                                    color = Slate900,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )

                                // Copilot Tone Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AssistChip(
                                        onClick = { viewModel.applyCopilotTone("PROFESSIONAL") },
                                        label = { Text("Pro", fontSize = 10.sp) }
                                    )
                                    AssistChip(
                                        onClick = { viewModel.applyCopilotTone("WARM") },
                                        label = { Text("Chaleureux", fontSize = 10.sp) }
                                    )
                                    AssistChip(
                                        onClick = { viewModel.applyCopilotTone("SHORT") },
                                        label = { Text("Court", fontSize = 10.sp) }
                                    )
                                    AssistChip(
                                        onClick = { viewModel.applyCopilotTone("EXPANDED") },
                                        label = { Text("Détaillé", fontSize = 10.sp) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.sendAgentReply(selectedConv.id, copilotDraft)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AiViolet),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("copilot_btn_accept")
                                    ) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Valider & Envoyer", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Input Bar (Manual agent reply)
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInputText,
                                onValueChange = { chatInputText = it },
                                placeholder = { Text("Répondre au client...", fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("inbox_chat_input"),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Slate200,
                                    focusedBorderColor = WhatsAppGreenDark
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            FilledIconButton(
                                onClick = {
                                    if (chatInputText.isNotBlank()) {
                                        viewModel.sendAgentReply(selectedConv.id, chatInputText)
                                        chatInputText = ""
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = WhatsAppGreenDark),
                                modifier = Modifier.testTag("inbox_btn_send")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Envoyer",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sélectionnez une conversation pour afficher l'historique", color = Slate500, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Client Profile Bottom Sheet / Modal
    if (showProfileSheet && selectedContact != null) {
        ModalBottomSheet(
            onDismissRequest = { showProfileSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .testTag("client_profile_sheet")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = selectedContact.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = selectedContact.phoneNumber, fontSize = 13.sp, color = Slate600)
                        if (selectedContact.email.isNotBlank()) {
                            Text(text = selectedContact.email, fontSize = 12.sp, color = Slate500)
                        }
                    }
                    if (selectedLead != null) {
                        LeadScoreBadge(score = selectedLead.score)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Informations Prospect (CRM)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedLead != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Étape du pipeline :", fontSize = 12.sp, color = Slate600)
                        CrmStageBadge(stage = selectedLead.stage)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Budget estimé :", fontSize = 12.sp, color = Slate600)
                        Text(text = "%.0f EUR".format(selectedLead.estimatedBudget), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Besoin :", fontSize = 12.sp, color = Slate600)
                        Text(text = selectedLead.needDescription.ifEmpty { "Non spécifié" }, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Actions Rapides", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val p = products.firstOrNull()
                            if (p != null && selectedConv != null) {
                                viewModel.createOrder(selectedContact.id, selectedConv.id, p, "STRIPE")
                                showProfileSheet = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Créer Commande", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.bookAppointment(
                                contactId = selectedContact.id,
                                title = "RDV Commercial Téléphonique",
                                clientName = selectedContact.name,
                                timeOffsetHours = 24,
                                notes = "Prise de contact suite à conversation WhatsApp"
                            )
                            showProfileSheet = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prendre RDV", fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ConversationItemRow(
    conv: ConversationEntity,
    contactName: String,
    contactPhone: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(conv.lastMessageTime))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) WhatsAppGreenLight.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag("conv_item_${conv.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (conv.status == "HUMAN_REQUIRED") RedDanger else WhatsAppGreenDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contactName.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = contactName,
                    fontWeight = if (conv.unreadCount > 0) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = Slate500
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = conv.lastMessage.ifEmpty { "Nouvelle conversation" },
                fontSize = 11.sp,
                color = if (conv.unreadCount > 0) Slate900 else Slate500,
                maxLines = 1,
                fontWeight = if (conv.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConversationStatusBadge(status = conv.status)

                if (conv.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${conv.unreadCount}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    msg: MessageEntity,
    currency: String
) {
    val isClient = msg.sender == "CLIENT"
    val isSystem = msg.sender == "SYSTEM"
    val isAi = msg.sender == "AI"

    if (isSystem) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = msg.content,
                    fontSize = 11.sp,
                    color = Color(0xFF92400E),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        return
    }

    val alignment = if (isClient) Alignment.CenterStart else Alignment.CenterEnd
    val bubbleColor = when {
        isClient -> Color.White
        isAi -> Color(0xFFE7FCE3) // Light WhatsApp green
        else -> Color(0xFFD9FDD3)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isClient) 2.dp else 12.dp,
                bottomEnd = if (isClient) 12.dp else 2.dp
            ),
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .testTag("msg_bubble_${msg.id}")
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (isAi) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = WhatsAppGreenDark,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Assistant IA (Autonome)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WhatsAppGreenDark
                        )
                    }
                } else if (!isClient) {
                    Text(
                        text = "Commercial (Humain)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1)
                    )
                }

                Text(
                    text = msg.content,
                    fontSize = 13.sp,
                    color = Slate900,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    Text(
                        text = timeFormat.format(Date(msg.timestamp)),
                        fontSize = 9.sp,
                        color = Slate500
                    )
                    if (!isClient) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = Color(0xFF34B7F1), // Double blue tick
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
