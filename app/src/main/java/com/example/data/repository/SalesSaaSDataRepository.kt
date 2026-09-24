package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SalesSaaSDataRepository(private val dao: AppDao) {

    // Organization
    val organization: Flow<OrganizationEntity?> = dao.getOrganization()
    suspend fun getOrganizationById(id: String) = dao.getOrganizationById(id)
    suspend fun saveOrganization(org: OrganizationEntity) = dao.insertOrganization(org)
    suspend fun updateOrganization(org: OrganizationEntity) = dao.updateOrganization(org)

    // Contacts & Conversations
    val contacts: Flow<List<ContactEntity>> = dao.getAllContacts()
    val conversations: Flow<List<ConversationEntity>> = dao.getAllConversations()
    fun getMessagesForConversation(convId: String): Flow<List<MessageEntity>> = dao.getMessagesForConversation(convId)
    val allMessages: Flow<List<MessageEntity>> = dao.getAllMessages()

    suspend fun getContact(id: String): ContactEntity? = dao.getContactById(id)
    suspend fun getConversation(id: String): ConversationEntity? = dao.getConversationById(id)
    fun getConversationFlow(id: String): Flow<ConversationEntity?> = dao.getConversationFlow(id)

    suspend fun saveContact(contact: ContactEntity) = dao.insertContact(contact)
    suspend fun saveConversation(conv: ConversationEntity) = dao.insertConversation(conv)
    suspend fun updateConversation(conv: ConversationEntity) = dao.updateConversation(conv)
    suspend fun saveMessage(msg: MessageEntity) = dao.insertMessage(msg)
    suspend fun updateMessage(msg: MessageEntity) = dao.updateMessage(msg)

    // Leads / CRM
    val leads: Flow<List<LeadEntity>> = dao.getAllLeads()
    suspend fun getLeadByContactId(contactId: String) = dao.getLeadByContactId(contactId)
    suspend fun saveLead(lead: LeadEntity) = dao.insertLead(lead)
    suspend fun updateLead(lead: LeadEntity) = dao.updateLead(lead)

    // Products
    val products: Flow<List<ProductEntity>> = dao.getAllProducts()
    val availableProducts: Flow<List<ProductEntity>> = dao.getAvailableProducts()
    suspend fun getProduct(id: String) = dao.getProductById(id)
    suspend fun saveProduct(product: ProductEntity) = dao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = dao.updateProduct(product)
    suspend fun deleteProduct(product: ProductEntity) = dao.deleteProduct(product)

    // Orders
    val orders: Flow<List<OrderEntity>> = dao.getAllOrders()
    suspend fun saveOrder(order: OrderEntity) = dao.insertOrder(order)
    suspend fun updateOrder(order: OrderEntity) = dao.updateOrder(order)

    // Appointments
    val appointments: Flow<List<AppointmentEntity>> = dao.getAllAppointments()
    suspend fun saveAppointment(appointment: AppointmentEntity) = dao.insertAppointment(appointment)
    suspend fun updateAppointment(appointment: AppointmentEntity) = dao.updateAppointment(appointment)

    // Workflows
    val workflows: Flow<List<AutomationWorkflowEntity>> = dao.getAllWorkflows()
    suspend fun saveWorkflow(wf: AutomationWorkflowEntity) = dao.insertWorkflow(wf)
    suspend fun updateWorkflow(wf: AutomationWorkflowEntity) = dao.updateWorkflow(wf)

    // Knowledge base
    val knowledgeDocs: Flow<List<KnowledgeDocumentEntity>> = dao.getAllKnowledgeDocs()
    suspend fun saveKnowledgeDoc(doc: KnowledgeDocumentEntity) = dao.insertKnowledgeDoc(doc)
    suspend fun deleteKnowledgeDoc(doc: KnowledgeDocumentEntity) = dao.deleteKnowledgeDoc(doc)

    // AI Settings
    val aiSettings: Flow<AiSettingsEntity?> = dao.getAiSettings()
    suspend fun getAiSettingsSnapshot() = dao.getAiSettingsSnapshot()
    suspend fun saveAiSettings(settings: AiSettingsEntity) = dao.insertAiSettings(settings)

    // Team members
    val teamMembers: Flow<List<TeamMemberEntity>> = dao.getAllTeamMembers()
    suspend fun saveTeamMember(member: TeamMemberEntity) = dao.insertTeamMember(member)

    // Prepopulate rich default data for testing and demonstrations
    suspend fun prepopulateDefaultDataIfEmpty() {
        val orgSnapshot = dao.getOrganizationById("org_default")
        if (orgSnapshot != null) return

        val org = OrganizationEntity(
            id = "org_default",
            name = "NexStore Commercial & Tech",
            industry = "Commerce & Électronique",
            country = "France / Afrique Francophone",
            currency = "EUR",
            commercialGoal = "Augmenter les ventes WhatsApp et qualifier les leads 24/7",
            isAiActive = true,
            plan = "PRO",
            whatsappNumber = "+33 6 88 12 34 56",
            whatsappPhoneId = "WA-BUS-90214",
            isConnected = true
        )
        dao.insertOrganization(org)

        val ai = AiSettingsEntity(
            organizationId = "org_default",
            assistantName = "Aria Commerciale",
            personality = "Consultante de vente proactive, empathique, persuasive et experte",
            tone = "Professionnel et chaleureux",
            language = "fr",
            formality = "Vous",
            mission = "Présenter le catalogue, vérifier les stocks, négocier selon les règles de remise et guider le client jusqu'au paiement ou prise de rendez-vous.",
            isCopilotMode = false,
            fallbackMessage = "Je transmets votre demande à notre conseiller dédié qui vous recontacte dans les 10 minutes.",
            qualificationQuestions = "Quel modèle ou produit précis recherchez-vous ?;Quelle est votre fourchette de budget ?;À quelle adresse ou ville souhaitez-vous la livraison ?"
        )
        dao.insertAiSettings(ai)

        // Products
        val p1 = ProductEntity(
            id = "prod_1",
            organizationId = "org_default",
            category = "Smartphones",
            name = "UltraPhone Nova X 256GB",
            description = "Écran OLED 120Hz, Triple capteur 108MP, autonomie 2 jours, charge ultra-rapide 67W.",
            price = 599.0,
            promoPrice = 549.0,
            stock = 14,
            sku = "NOVA-X-256",
            features = "Couleur Noir Sidéral, Garantie 2 ans, Écouteurs sans fil offerts",
            isAvailable = true
        )
        val p2 = ProductEntity(
            id = "prod_2",
            organizationId = "org_default",
            category = "Audio",
            name = "Casque Sans-Fil SoundPro Max",
            description = "Réduction de bruit active hybride, son haute-définition Spatial, 40h d'autonomie.",
            price = 189.0,
            promoPrice = 149.0,
            stock = 25,
            sku = "AUDIO-SP-MAX",
            features = "Connexion multi-point, Bluetooth 5.3, Étui rigide inclus",
            isAvailable = true
        )
        val p3 = ProductEntity(
            id = "prod_3",
            organizationId = "org_default",
            category = "Accessoires",
            name = "Station de Charge 3-en-1 MagSafe",
            description = "Charge rapide sans fil pour téléphone, montre connectée et écouteurs simultanément.",
            price = 79.0,
            promoPrice = null,
            stock = 32,
            sku = "CHG-MAG-31",
            features = "Aluminium anodisé, Voyant LED discret, Adaptateur 30W inclus",
            isAvailable = true
        )
        val p4 = ProductEntity(
            id = "prod_4",
            organizationId = "org_default",
            category = "Smartphones",
            name = "ProTab Vision 11 pouces",
            description = "Tablette haute performance avec stylet actif inclus, idéale pour professionnels et créatifs.",
            price = 429.0,
            promoPrice = 399.0,
            stock = 8,
            sku = "TAB-VIS-11",
            features = "Clavier magnétique en option, 8 Go RAM, 128 Go stockage",
            isAvailable = true
        )
        dao.insertProduct(p1)
        dao.insertProduct(p2)
        dao.insertProduct(p3)
        dao.insertProduct(p4)

        // Contacts
        val c1 = ContactEntity(
            id = "c_1",
            organizationId = "org_default",
            name = "Thomas Mercier",
            phoneNumber = "+33 6 42 11 90 44",
            email = "thomas.mercier@outlook.fr",
            company = "Mercier Consulting",
            location = "Lyon, France",
            tags = "Chaud, UltraPhone, Prêt à commander"
        )
        val c2 = ContactEntity(
            id = "c_2",
            organizationId = "org_default",
            name = "Fatou Traoré",
            phoneNumber = "+221 77 654 32 10",
            email = "fatou.traore@business.sn",
            company = "Studio Design Dakar",
            location = "Dakar, Sénégal",
            tags = "Qualifié, Casque Audio, Devis demandé"
        )
        val c3 = ContactEntity(
            id = "c_3",
            organizationId = "org_default",
            name = "Lucas Dubois",
            phoneNumber = "+33 7 81 23 45 67",
            email = "lucas.dubois@gmail.com",
            company = "",
            location = "Marseille, France",
            tags = "Intervention Requise, Réclamation livraison"
        )
        val c4 = ContactEntity(
            id = "c_4",
            organizationId = "org_default",
            name = "Amina Benali",
            phoneNumber = "+33 6 99 88 77 66",
            email = "amina.benali@agency.com",
            company = "Atlas Com",
            location = "Paris, France",
            tags = "Nouveau, Intéressé tablette"
        )
        dao.insertContact(c1)
        dao.insertContact(c2)
        dao.insertContact(c3)
        dao.insertContact(c4)

        // Conversations
        val now = System.currentTimeMillis()
        val conv1 = ConversationEntity(
            id = "conv_1",
            organizationId = "org_default",
            contactId = "c_1",
            status = "AI_ACTIVE",
            unreadCount = 0,
            priority = "URGENT",
            lastMessage = "Parfait, je valide l'UltraPhone Nova X à 549€. Vous avez un lien pour payer ?",
            lastMessageTime = now - 1000 * 60 * 12,
            language = "fr"
        )
        val conv2 = ConversationEntity(
            id = "conv_2",
            organizationId = "org_default",
            contactId = "c_2",
            status = "COPILOT",
            unreadCount = 1,
            priority = "HIGH",
            lastMessage = "Est-ce qu'une remise est possible si nous commandons 3 casques pour notre studio ?",
            lastMessageTime = now - 1000 * 60 * 45,
            language = "fr"
        )
        val conv3 = ConversationEntity(
            id = "conv_3",
            organizationId = "org_default",
            contactId = "c_3",
            status = "HUMAN_REQUIRED",
            unreadCount = 2,
            priority = "URGENT",
            lastMessage = "Je souhaite parler immédiatement à un responsable commercial pour ma commande !",
            lastMessageTime = now - 1000 * 60 * 90,
            language = "fr"
        )
        val conv4 = ConversationEntity(
            id = "conv_4",
            organizationId = "org_default",
            contactId = "c_4",
            status = "OPEN",
            unreadCount = 0,
            priority = "NORMAL",
            lastMessage = "Bonjour ! La tablette ProTab est-elle fournie avec le stylet inclus ?",
            lastMessageTime = now - 1000 * 60 * 180,
            language = "fr"
        )
        dao.insertConversation(conv1)
        dao.insertConversation(conv2)
        dao.insertConversation(conv3)
        dao.insertConversation(conv4)

        // Messages for conv_1
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_1",
                sender = "CLIENT",
                content = "Bonjour, vous avez des promotions sur vos téléphones avec un bon appareil photo ?",
                timestamp = now - 1000 * 60 * 60,
                status = "READ",
                intentDetected = "PRICE_INQUIRY"
            )
        )
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_1",
                sender = "AI",
                content = "Bonjour Thomas 👋 Ravi de vous aider ! Notre modèle vedette est l'UltraPhone Nova X 256GB : il intègre un triple capteur 108MP d'une netteté exceptionnelle, actuellement en promo à 549€ au lieu de 599€ (avec écouteurs offerts). Est-ce le type de modèle que vous recherchez ?",
                timestamp = now - 1000 * 60 * 58,
                status = "READ",
                aiConfidence = 0.98f,
                productId = "prod_1"
            )
        )
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_1",
                sender = "CLIENT",
                content = "Parfait, je valide l'UltraPhone Nova X à 549€. Vous avez un lien pour payer ?",
                timestamp = now - 1000 * 60 * 12,
                status = "READ",
                intentDetected = "PURCHASE_INTENT"
            )
        )
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_1",
                sender = "AI",
                content = "Excellente décision Thomas ! 🚀 Voici votre lien de paiement sécurisé (Carte bancaire ou Mobile Money) pour valider votre commande de l'UltraPhone Nova X (549,00 €) : https://pay.nexstore.com/order/NX-9821. Dès confirmation, votre colis sera expédié sous 24h avec numéro de suivi.",
                timestamp = now - 1000 * 60 * 11,
                status = "SENT",
                aiConfidence = 0.99f
            )
        )

        // Messages for conv_2
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_2",
                sender = "CLIENT",
                content = "Est-ce qu'une remise est possible si nous commandons 3 casques SoundPro Max pour notre studio ?",
                timestamp = now - 1000 * 60 * 45,
                status = "DELIVERED",
                intentDetected = "DISCOUNT_REQUEST",
                suggestedReply = "Bonjour Fatou 👋 Absolument ! Pour un pack studio de 3 casques SoundPro Max, nous vous accordons 10% supplémentaire soit 134,10€ par unité (total 402,30€ au lieu de 447€) avec livraison express offerte. Souhaitez-vous que je génère le devis pro ?"
            )
        )

        // Messages for conv_3
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_3",
                sender = "CLIENT",
                content = "Je souhaite parler immédiatement à un responsable commercial pour ma commande !",
                timestamp = now - 1000 * 60 * 90,
                status = "DELIVERED",
                intentDetected = "HUMAN_ESCALATION"
            )
        )
        dao.insertMessage(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = "conv_3",
                sender = "SYSTEM",
                content = "⚠️ [Alerte IA] Détection de demande d'assistance humaine directe. La conversation a été basculée en mode 'Intervention humaine requise' et assignée au commercial de garde.",
                timestamp = now - 1000 * 60 * 89,
                status = "DELIVERED"
            )
        )

        // Leads
        dao.insertLead(
            LeadEntity(
                id = "lead_1",
                organizationId = "org_default",
                contactId = "c_1",
                stage = "NEGOTIATION",
                score = 92,
                estimatedBudget = 550.0,
                needDescription = "Smartphone haut de gamme avec appareil photo pro 108MP",
                urgency = "Immédiate",
                purchaseIntent = "Prêt à commander",
                notes = "Client très réactif, lien de paiement envoyé. Relance automatique dans 4h si non payé.",
                updatedAt = now - 1000 * 60 * 10
            )
        )
        dao.insertLead(
            LeadEntity(
                id = "lead_2",
                organizationId = "org_default",
                contactId = "c_2",
                stage = "PROPOSAL",
                score = 78,
                estimatedBudget = 450.0,
                needDescription = "Achat de 3 casques SoundPro Max pour studio créatif",
                urgency = "Haute",
                purchaseIntent = "Intéressé",
                notes = "En attente validation de la proposition de remise 10% en mode Copilote.",
                updatedAt = now - 1000 * 60 * 40
            )
        )
        dao.insertLead(
            LeadEntity(
                id = "lead_3",
                organizationId = "org_default",
                contactId = "c_4",
                stage = "QUALIFIED",
                score = 65,
                estimatedBudget = 400.0,
                needDescription = "Tablette graphique ProTab Vision pour graphisme",
                urgency = "Moyenne",
                purchaseIntent = "Intéressé",
                notes = "A demandé confirmation stylet inclus.",
                updatedAt = now - 1000 * 60 * 170
            )
        )

        // Orders
        dao.insertOrder(
            OrderEntity(
                id = "ord_101",
                organizationId = "org_default",
                contactId = "c_1",
                conversationId = "conv_1",
                orderNumber = "CMD-2026-0891",
                totalAmount = 549.0,
                status = "CONFIRMED",
                paymentMethod = "STRIPE",
                paymentStatus = "PAID",
                shippingAddress = "14 Rue de la République, 69002 Lyon",
                itemsSummary = "1x UltraPhone Nova X 256GB (Noir Sidéral)",
                createdAt = now - 1000 * 60 * 60 * 3
            )
        )
        dao.insertOrder(
            OrderEntity(
                id = "ord_102",
                organizationId = "org_default",
                contactId = "c_2",
                conversationId = "conv_2",
                orderNumber = "CMD-2026-0890",
                totalAmount = 149.0,
                status = "PREPARING",
                paymentMethod = "MOBILE_MONEY",
                paymentStatus = "PAID",
                shippingAddress = "Plateau, Dakar",
                itemsSummary = "1x Casque Sans-Fil SoundPro Max",
                createdAt = now - 1000 * 60 * 60 * 18
            )
        )

        // Appointments
        dao.insertAppointment(
            AppointmentEntity(
                id = "apt_1",
                organizationId = "org_default",
                contactId = "c_2",
                title = "Démo Studio Audio & Offre B2B",
                clientName = "Fatou Traoré (Studio Design Dakar)",
                scheduledTime = now + 1000 * 60 * 60 * 24, // Tomorrow
                durationMinutes = 30,
                status = "CONFIRMED",
                notes = "Présentation vidéo Zoom des configurations multi-postes"
            )
        )
        dao.insertAppointment(
            AppointmentEntity(
                id = "apt_2",
                organizationId = "org_default",
                contactId = "c_4",
                title = "Conseil Choix Tablette ProTab",
                clientName = "Amina Benali",
                scheduledTime = now + 1000 * 60 * 60 * 48,
                durationMinutes = 20,
                status = "PENDING",
                notes = "Valider compatibilité suite logicielle créative"
            )
        )

        // Workflows
        dao.insertWorkflow(
            AutomationWorkflowEntity(
                id = "wf_1",
                organizationId = "org_default",
                name = "Relance Prospect 'Je vais réfléchir' (24h)",
                triggerType = "NO_REPLY_24H",
                actionType = "AI_FOLLOW_UP",
                delayHours = 24,
                isActive = true
            )
        )
        dao.insertWorkflow(
            AutomationWorkflowEntity(
                id = "wf_2",
                organizationId = "org_default",
                name = "Envoi fiche produit sur demande de prix",
                triggerType = "PRICE_REQUEST",
                actionType = "SEND_TEMPLATE",
                delayHours = 0,
                isActive = true
            )
        )
        dao.insertWorkflow(
            AutomationWorkflowEntity(
                id = "wf_3",
                organizationId = "org_default",
                name = "Alerte Commerciale si Lead Score > 80",
                triggerType = "QUALIFIED_LEAD",
                actionType = "NOTIFY_HUMAN",
                delayHours = 0,
                isActive = true
            )
        )

        // Knowledge Documents
        dao.insertKnowledgeDoc(
            KnowledgeDocumentEntity(
                id = "doc_1",
                organizationId = "org_default",
                title = "Politique de Livraison & Délais",
                content = "Livraison express en France métropolitaine sous 24-48h (Chronopost/Colissimo). Livraison internationale et Afrique de l'Ouest sous 3-5 jours ouvrés via DHL Express. Frais de port offerts dès 100€ d'achat.",
                type = "POLICY",
                tags = "Livraison, Frais, Délais"
            )
        )
        dao.insertKnowledgeDoc(
            KnowledgeDocumentEntity(
                id = "doc_2",
                organizationId = "org_default",
                title = "Garantie et Retours",
                content = "Tous nos appareils électroniques bénéficient d'une garantie constructeur de 2 ans pièces et main d'œuvre. Retour gratuit sous 30 jours si le produit n'est pas ouvert.",
                type = "POLICY",
                tags = "Garantie, SAV, Retours"
            )
        )
        dao.insertKnowledgeDoc(
            KnowledgeDocumentEntity(
                id = "doc_3",
                organizationId = "org_default",
                title = "Modes de Paiement Acceptés",
                content = "Nous acceptons les paiements sécurisés par Carte Bancaire (Visa, Mastercard via Stripe), Mobile Money (Wave, Orange Money, MTN MoMo), et le paiement à la livraison pour les commandes locales.",
                type = "FAQ",
                tags = "Paiement, Stripe, Mobile Money"
            )
        )

        // Team members
        dao.insertTeamMember(
            TeamMemberEntity(
                id = "tm_1",
                organizationId = "org_default",
                name = "Alexandre V. (Vous)",
                email = "alex@nexstore.com",
                role = "OWNER",
                activeConversations = 3
            )
        )
        dao.insertTeamMember(
            TeamMemberEntity(
                id = "tm_2",
                organizationId = "org_default",
                name = "Sarah K.",
                email = "sarah.k@nexstore.com",
                role = "COMMERCIAL",
                activeConversations = 8
            )
        )
        dao.insertTeamMember(
            TeamMemberEntity(
                id = "tm_3",
                organizationId = "org_default",
                name = "David M.",
                email = "david.m@nexstore.com",
                role = "SUPPORT",
                activeConversations = 2
            )
        )
    }
}
