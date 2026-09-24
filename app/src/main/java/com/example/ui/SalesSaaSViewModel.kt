package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.SalesSaaSDataRepository
import com.example.service.ai.AiCommercialEngine
import com.example.service.automation.AutomationEngine
import com.example.service.payment.PaymentGatewayService
import com.example.service.payment.PaymentProviderType
import com.example.service.whatsapp.WhatsAppPlatformService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppNavDestination {
    DASHBOARD,
    INBOX,
    CRM_PIPELINE,
    CATALOG,
    ORDERS,
    APPOINTMENTS,
    AUTOMATIONS,
    AI_SETTINGS,
    SIMULATOR,
    ANALYTICS,
    ADMIN_SETTINGS,
    LANDING_PAGE,
    ONBOARDING
}

data class DashboardStats(
    val conversationsToday: Int = 18,
    val newLeads: Int = 12,
    val qualifiedLeads: Int = 7,
    val salesCount: Int = 5,
    val conversionRatePercent: Double = 27.8,
    val revenueAttributed: Double = 2840.0,
    val appointmentsBooked: Int = 4,
    val humanInterventionsRequired: Int = 1,
    val avgResponseTimeSeconds: Int = 3,
    val aiRepliesCount: Int = 42,
    val autoFollowUpsSent: Int = 9
)

class SalesSaaSViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = SalesSaaSDataRepository(db.appDao())
    private val aiEngine = AiCommercialEngine()
    private val automationEngine = AutomationEngine()
    private val paymentService = PaymentGatewayService()
    val whatsappService = WhatsAppPlatformService()

    // Navigation
    private val _currentDestination = MutableStateFlow(AppNavDestination.DASHBOARD)
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    // Selected Conversation
    private val _selectedConversationId = MutableStateFlow<String?>("conv_1")
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    // Active Dashboard Period
    private val _dashboardPeriod = MutableStateFlow("7J") // TODAY, 7J, 30J, 90J
    val dashboardPeriod: StateFlow<String> = _dashboardPeriod.asStateFlow()

    // Copilot Draft State
    private val _copilotDraft = MutableStateFlow("")
    val copilotDraft: StateFlow<String> = _copilotDraft.asStateFlow()

    // Toast / Notification Banner
    private val _uiNotice = MutableStateFlow<String?>(null)
    val uiNotice: StateFlow<String?> = _uiNotice.asStateFlow()

    // Onboarding Step (1 to 8)
    private val _onboardingStep = MutableStateFlow(1)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    // Repository Flows
    val organization: StateFlow<OrganizationEntity?> = repository.organization
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<ContactEntity>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leads: StateFlow<List<LeadEntity>> = repository.leads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<AppointmentEntity>> = repository.appointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workflows: StateFlow<List<AutomationWorkflowEntity>> = repository.workflows
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val knowledgeDocs: StateFlow<List<KnowledgeDocumentEntity>> = repository.knowledgeDocs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiSettings: StateFlow<AiSettingsEntity?> = repository.aiSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val teamMembers: StateFlow<List<TeamMemberEntity>> = repository.teamMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMessages: StateFlow<List<MessageEntity>> = _selectedConversationId
        .flatMapLatest { convId ->
            if (convId != null) repository.getMessagesForConversation(convId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.prepopulateDefaultDataIfEmpty()
        }
    }

    fun navigateTo(dest: AppNavDestination) {
        _currentDestination.value = dest
    }

    fun selectConversation(id: String) {
        _selectedConversationId.value = id
        // Reset unread count
        viewModelScope.launch {
            val conv = repository.getConversation(id)
            if (conv != null && conv.unreadCount > 0) {
                repository.updateConversation(conv.copy(unreadCount = 0))
            }
        }
    }

    fun setDashboardPeriod(period: String) {
        _dashboardPeriod.value = period
    }

    fun clearNotice() {
        _uiNotice.value = null
    }

    // Inbound Customer Message (Simulated or Real Webhook)
    fun receiveClientMessage(convId: String, text: String) {
        viewModelScope.launch {
            val conv = repository.getConversation(convId) ?: return@launch
            val contact = repository.getContact(conv.contactId) ?: return@launch
            val currentLead = repository.getLeadByContactId(contact.id)
            val allProds = products.value
            val docs = knowledgeDocs.value
            val settings = aiSettings.value
            val now = System.currentTimeMillis()

            // 1. Save incoming message
            val clientMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                sender = "CLIENT",
                content = text,
                timestamp = now,
                status = "READ"
            )
            repository.saveMessage(clientMsg)

            // Update conversation last message
            repository.updateConversation(
                conv.copy(
                    lastMessage = text,
                    lastMessageTime = now
                )
            )

            // 2. AI Decision Analysis
            val decision = aiEngine.analyzeAndGenerateResponse(
                incomingMessage = text,
                contact = contact,
                currentLead = currentLead,
                products = allProds,
                knowledgeDocs = docs,
                aiSettings = settings,
                currency = organization.value?.currency ?: "EUR"
            )

            // 3. Update Lead if score/stage changed
            if (currentLead != null && (decision.updatedLeadScore != null || decision.updatedLeadStage != null)) {
                val newScore = decision.updatedLeadScore ?: currentLead.score
                val newStage = decision.updatedLeadStage ?: currentLead.stage
                val newBudget = decision.qualificationExtracted?.budget ?: currentLead.estimatedBudget
                val newNeed = decision.qualificationExtracted?.need ?: currentLead.needDescription
                val newUrgency = decision.qualificationExtracted?.urgency ?: currentLead.urgency
                repository.updateLead(
                    currentLead.copy(
                        score = newScore,
                        stage = newStage,
                        estimatedBudget = newBudget,
                        needDescription = newNeed,
                        urgency = newUrgency,
                        updatedAt = now
                    )
                )
            }

            // 4. Check for Human Escalation
            if (decision.isHumanHandoffRequired) {
                repository.updateConversation(
                    conv.copy(
                        status = "HUMAN_REQUIRED",
                        priority = "URGENT",
                        lastMessage = text,
                        lastMessageTime = now
                    )
                )
                repository.saveMessage(
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = convId,
                        sender = "SYSTEM",
                        content = "⚠️ [Alerte Immédiate] Intervention humaine requise détectée par l'IA. La conversation a été transférée au conseiller en direct.",
                        timestamp = now + 500,
                        status = "DELIVERED"
                    )
                )
                _uiNotice.value = "Alerte : Intervention humaine requise sur la conversation de ${contact.name}"
                return@launch
            }

            // 5. Automatic vs Copilot Mode
            val isCopilot = settings?.isCopilotMode == true || conv.status == "COPILOT"

            if (isCopilot) {
                _copilotDraft.value = decision.replyText
                repository.updateConversation(conv.copy(status = "COPILOT"))
                repository.saveMessage(
                    MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = convId,
                        sender = "SYSTEM",
                        content = "💡 [Copilote IA] Suggestion de réponse prête à être validée ou éditée.",
                        timestamp = now + 500,
                        status = "DELIVERED",
                        suggestedReply = decision.replyText,
                        aiConfidence = decision.confidence,
                        intentDetected = decision.intentDetected
                    )
                )
            } else {
                // Auto reply by AI
                val aiMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = convId,
                    sender = "AI",
                    content = decision.replyText,
                    timestamp = now + 1200,
                    status = "DELIVERED",
                    aiConfidence = decision.confidence,
                    intentDetected = decision.intentDetected,
                    productId = decision.matchedProduct?.id
                )
                repository.saveMessage(aiMsg)
                repository.updateConversation(
                    conv.copy(
                        lastMessage = decision.replyText,
                        lastMessageTime = now + 1200
                    )
                )
            }
        }
    }

    // Send Agent Reply (Human or Accepted Copilot)
    fun sendAgentReply(convId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val msg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                sender = "AGENT",
                content = text,
                timestamp = now,
                status = "SENT"
            )
            repository.saveMessage(msg)
            _copilotDraft.value = ""

            val conv = repository.getConversation(convId)
            if (conv != null) {
                repository.updateConversation(
                    conv.copy(
                        lastMessage = text,
                        lastMessageTime = now,
                        status = "OPEN"
                    )
                )
            }
        }
    }

    // Copilot Transformation Actions
    fun applyCopilotTone(style: String) {
        val current = _copilotDraft.value
        if (current.isNotBlank()) {
            _copilotDraft.value = aiEngine.transformTone(current, style)
        }
    }

    fun regenerateCopilotDraft(convId: String) {
        viewModelScope.launch {
            val conv = repository.getConversation(convId) ?: return@launch
            val contact = repository.getContact(conv.contactId) ?: return@launch
            val lead = repository.getLeadByContactId(contact.id)
            val prods = products.value
            val docs = knowledgeDocs.value
            val settings = aiSettings.value
            val decision = aiEngine.analyzeAndGenerateResponse(
                incomingMessage = conv.lastMessage,
                contact = contact,
                currentLead = lead,
                products = prods,
                knowledgeDocs = docs,
                aiSettings = settings,
                currency = organization.value?.currency ?: "EUR"
            )
            _copilotDraft.value = decision.replyText
        }
    }

    // CRM Lead Stage update
    fun updateLeadStage(leadId: String, newStage: String) {
        viewModelScope.launch {
            val lead = leads.value.firstOrNull { it.id == leadId } ?: return@launch
            repository.updateLead(lead.copy(stage = newStage, updatedAt = System.currentTimeMillis()))
            _uiNotice.value = "Lead déplacé vers l'étape : $newStage"
        }
    }

    fun updateLeadScore(leadId: String, newScore: Int) {
        viewModelScope.launch {
            val lead = leads.value.firstOrNull { it.id == leadId } ?: return@launch
            repository.updateLead(lead.copy(score = newScore.coerceIn(0, 100), updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateLeadNotes(leadId: String, notes: String) {
        viewModelScope.launch {
            val lead = leads.value.firstOrNull { it.id == leadId } ?: return@launch
            repository.updateLead(lead.copy(notes = notes, updatedAt = System.currentTimeMillis()))
        }
    }

    // Products Management
    fun addProduct(name: String, category: String, price: Double, promoPrice: Double?, stock: Int, sku: String, desc: String, features: String) {
        viewModelScope.launch {
            val p = ProductEntity(
                id = "prod_${System.currentTimeMillis()}",
                organizationId = "org_default",
                category = category,
                name = name,
                description = desc,
                price = price,
                promoPrice = promoPrice,
                stock = stock,
                sku = sku,
                features = features,
                isAvailable = stock > 0
            )
            repository.saveProduct(p)
            _uiNotice.value = "Produit '$name' ajouté avec succès au catalogue !"
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _uiNotice.value = "Produit supprimé."
        }
    }

    // Order Creation
    fun createOrder(contactId: String, convId: String, product: ProductEntity, paymentMethod: String) {
        viewModelScope.launch {
            val contact = repository.getContact(contactId) ?: return@launch
            val orderNum = "CMD-${(1000..9999).random()}"
            val total = product.promoPrice ?: product.price
            val order = OrderEntity(
                id = "ord_${System.currentTimeMillis()}",
                organizationId = "org_default",
                contactId = contactId,
                conversationId = convId,
                orderNumber = orderNum,
                totalAmount = total,
                status = "CONFIRMED",
                paymentMethod = paymentMethod,
                paymentStatus = if (paymentMethod == "COD") "PENDING" else "PAID",
                shippingAddress = contact.location,
                itemsSummary = "1x ${product.name}"
            )
            repository.saveOrder(order)

            // Send confirmation in chat
            val msg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                sender = "AI",
                content = "🎉 Commande validée ($orderNum) ! Total : ${total} ${organization.value?.currency ?: "EUR"}.\nProduit : ${product.name}.\nMode de règlement : $paymentMethod.",
                timestamp = System.currentTimeMillis(),
                status = "DELIVERED"
            )
            repository.saveMessage(msg)
            _uiNotice.value = "Commande $orderNum créée avec succès !"
        }
    }

    // Appointment Booking
    fun bookAppointment(contactId: String, title: String, clientName: String, timeOffsetHours: Int, notes: String) {
        viewModelScope.launch {
            val appointment = AppointmentEntity(
                id = "apt_${System.currentTimeMillis()}",
                organizationId = "org_default",
                contactId = contactId,
                title = title,
                clientName = clientName,
                scheduledTime = System.currentTimeMillis() + timeOffsetHours * 3600 * 1000L,
                durationMinutes = 30,
                status = "CONFIRMED",
                notes = notes
            )
            repository.saveAppointment(appointment)
            _uiNotice.value = "Rendez-vous commercial programmé pour $clientName !"
        }
    }

    // Workflows
    fun toggleWorkflow(wf: AutomationWorkflowEntity) {
        viewModelScope.launch {
            repository.updateWorkflow(wf.copy(isActive = !wf.isActive))
        }
    }

    fun addWorkflow(name: String, trigger: String, action: String, delayHours: Int) {
        viewModelScope.launch {
            val wf = AutomationWorkflowEntity(
                id = "wf_${System.currentTimeMillis()}",
                organizationId = "org_default",
                name = name,
                triggerType = trigger,
                actionType = action,
                delayHours = delayHours,
                isActive = true
            )
            repository.saveWorkflow(wf)
            _uiNotice.value = "Nouvelle automatisation '$name' activée !"
        }
    }

    // Knowledge Docs
    fun addKnowledgeDoc(title: String, content: String, type: String, tags: String) {
        viewModelScope.launch {
            val doc = KnowledgeDocumentEntity(
                id = "doc_${System.currentTimeMillis()}",
                organizationId = "org_default",
                title = title,
                content = content,
                type = type,
                tags = tags
            )
            repository.saveKnowledgeDoc(doc)
            _uiNotice.value = "Document de connaissance ajouté."
        }
    }

    fun deleteKnowledgeDoc(doc: KnowledgeDocumentEntity) {
        viewModelScope.launch {
            repository.deleteKnowledgeDoc(doc)
            _uiNotice.value = "Document supprimé."
        }
    }

    // AI Settings
    fun updateAiSettings(name: String, tone: String, mission: String, isCopilot: Boolean) {
        viewModelScope.launch {
            val current = aiSettings.value ?: return@launch
            repository.saveAiSettings(
                current.copy(
                    assistantName = name,
                    tone = tone,
                    mission = mission,
                    isCopilotMode = isCopilot
                )
            )
            _uiNotice.value = "Configuration de l'assistant IA mise à jour."
        }
    }

    // Onboarding Wizard Progression
    fun nextOnboardingStep() {
        if (_onboardingStep.value < 8) {
            _onboardingStep.value += 1
        } else {
            _onboardingStep.value = 1
            _currentDestination.value = AppNavDestination.DASHBOARD
            _uiNotice.value = "🎉 Votre assistant commercial IA est prêt et activé !"
        }
    }

    fun prevOnboardingStep() {
        if (_onboardingStep.value > 1) {
            _onboardingStep.value -= 1
        }
    }
}
