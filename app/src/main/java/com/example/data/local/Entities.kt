package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val industry: String,
    val country: String,
    val currency: String,
    val commercialGoal: String,
    val isAiActive: Boolean = true,
    val plan: String = "PRO", // STARTER, PRO, BUSINESS
    val whatsappNumber: String = "+33 6 12 34 56 78",
    val whatsappPhoneId: String = "104928374928174",
    val isConnected: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val company: String = "",
    val location: String = "Paris, France",
    val tags: String = "Prospect, Intéressé", // Comma-separated
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val contactId: String,
    val status: String = "AI_ACTIVE", // OPEN, AI_ACTIVE, COPILOT, HUMAN_REQUIRED, CLOSED
    val unreadCount: Int = 0,
    val priority: String = "NORMAL", // NORMAL, HIGH, URGENT
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val language: String = "fr",
    val assignedToUserId: String = "agent_1"
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val sender: String, // CLIENT, AI, AGENT, SYSTEM
    val content: String,
    val messageType: String = "TEXT", // TEXT, PRODUCT, ORDER, APPOINTMENT
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED", // SENT, DELIVERED, READ
    val aiConfidence: Float? = null,
    val intentDetected: String? = null,
    val suggestedReply: String? = null,
    val productId: String? = null
)

@Entity(tableName = "leads")
data class LeadEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val contactId: String,
    val stage: String = "NEW", // NEW, CONTACTED, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST
    val score: Int = 50, // 0..100 (0-30 Froid, 31-70 Intéressé, 71-100 Chaud)
    val estimatedBudget: Double = 0.0,
    val needDescription: String = "",
    val urgency: String = "Moyenne", // Faible, Moyenne, Haute, Immédiate
    val purchaseIntent: String = "Intéressé", // Curieux, Intéressé, Prêt à commander
    val assignedToUserId: String = "agent_1",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val category: String,
    val name: String,
    val description: String,
    val price: Double,
    val promoPrice: Double? = null,
    val stock: Int = 10,
    val sku: String,
    val imageUrl: String = "",
    val features: String = "",
    val isAvailable: Boolean = true
)

@Entity(tableName = "product_categories")
data class ProductCategoryEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val name: String
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val contactId: String,
    val conversationId: String,
    val orderNumber: String,
    val totalAmount: Double,
    val status: String = "NEW", // NEW, CONFIRMED, PREPARING, SHIPPED, DELIVERED, CANCELLED
    val paymentMethod: String = "STRIPE", // STRIPE, MOBILE_MONEY, LINK, COD
    val paymentStatus: String = "PENDING", // PENDING, PAID
    val shippingAddress: String = "",
    val itemsSummary: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val contactId: String,
    val title: String,
    val clientName: String,
    val scheduledTime: Long,
    val durationMinutes: Int = 30,
    val status: String = "CONFIRMED", // CONFIRMED, PENDING, CANCELLED
    val notes: String = ""
)

@Entity(tableName = "automation_workflows")
data class AutomationWorkflowEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val name: String,
    val triggerType: String, // NEW_LEAD, PRICE_REQUEST, ABANDONED_CART, NO_REPLY_24H, QUALIFIED_LEAD
    val actionType: String, // SEND_TEMPLATE, AI_FOLLOW_UP, ASSIGN_AGENT, NOTIFY_HUMAN
    val delayHours: Int = 0,
    val isActive: Boolean = true
)

@Entity(tableName = "knowledge_documents")
data class KnowledgeDocumentEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val title: String,
    val content: String,
    val type: String = "FAQ", // FAQ, PRODUCT_INFO, POLICY, URL
    val tags: String = "Général"
)

@Entity(tableName = "ai_settings")
data class AiSettingsEntity(
    @PrimaryKey val organizationId: String,
    val assistantName: String = "Nova Commercial",
    val personality: String = "Consultant expert, chaleureux et persuasif",
    val tone: String = "Professionnel et convivial",
    val language: String = "fr",
    val formality: String = "Vous",
    val mission: String = "Guider les prospects vers le bon produit, répondre précisément aux questions de prix et de livraison, et conclure la vente ou réserver un rendez-vous.",
    val isCopilotMode: Boolean = false,
    val fallbackMessage: String = "Je transmets immédiatement votre demande à un de nos spécialistes.",
    val qualificationQuestions: String = "Quel est votre besoin principal ?;Quel budget prévoyez-vous ?;Pour quel délai souhaitez-vous concrétiser ?"
)

@Entity(tableName = "team_members")
data class TeamMemberEntity(
    @PrimaryKey val id: String,
    val organizationId: String,
    val name: String,
    val email: String,
    val role: String = "COMMERCIAL", // OWNER, ADMIN, MANAGER, COMMERCIAL, SUPPORT
    val activeConversations: Int = 0
)
