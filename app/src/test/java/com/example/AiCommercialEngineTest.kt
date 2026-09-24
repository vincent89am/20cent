package com.example

import com.example.data.local.AiSettingsEntity
import com.example.data.local.ContactEntity
import com.example.data.local.LeadEntity
import com.example.data.local.ProductEntity
import com.example.service.ai.AiCommercialEngine
import com.example.service.automation.AutomationEngine
import com.example.service.payment.PaymentGatewayService
import com.example.service.payment.PaymentProviderType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AiCommercialEngineTest {

    private lateinit var aiEngine: AiCommercialEngine
    private lateinit var sampleContact: ContactEntity
    private lateinit var sampleProducts: List<ProductEntity>
    private lateinit var sampleAiSettings: AiSettingsEntity

    @Before
    fun setUp() {
        aiEngine = AiCommercialEngine()
        sampleContact = ContactEntity(
            id = "test_c1",
            organizationId = "org_1",
            name = "Jean Dupont",
            phoneNumber = "+33 6 12 34 56 78"
        )
        sampleProducts = listOf(
            ProductEntity(
                id = "p1",
                organizationId = "org_1",
                category = "Smartphones",
                name = "UltraPhone Nova X",
                description = "Capteur 108MP OLED 120Hz",
                price = 599.0,
                promoPrice = 549.0,
                stock = 10,
                sku = "NOVA-X"
            ),
            ProductEntity(
                id = "p2",
                organizationId = "org_1",
                category = "Audio",
                name = "Casque SoundPro Max",
                description = "Réduction de bruit active",
                price = 189.0,
                promoPrice = 149.0,
                stock = 15,
                sku = "AUDIO-SP"
            )
        )
        sampleAiSettings = AiSettingsEntity(
            organizationId = "org_1",
            assistantName = "Aria",
            personality = "Experte et chaleureuse",
            tone = "Professionnel",
            language = "fr",
            formality = "Vous",
            mission = "Vendre et conseiller",
            isCopilotMode = false,
            fallbackMessage = "Je transmets votre demande à un conseiller."
        )
    }

    @Test
    fun testHumanEscalationTrigger() {
        val result = aiEngine.analyzeAndGenerateResponse(
            incomingMessage = "C'est inadmissible, je veux parler à un responsable humain tout de suite !",
            contact = sampleContact,
            currentLead = null,
            products = sampleProducts,
            knowledgeDocs = emptyList(),
            aiSettings = sampleAiSettings
        )

        assertTrue(result.isHumanHandoffRequired)
        assertEquals("HUMAN_ESCALATION", result.intentDetected)
    }

    @Test
    fun testProductMatchAndPricing() {
        val result = aiEngine.analyzeAndGenerateResponse(
            incomingMessage = "Combien coûte le Casque SoundPro Max ?",
            contact = sampleContact,
            currentLead = null,
            products = sampleProducts,
            knowledgeDocs = emptyList(),
            aiSettings = sampleAiSettings
        )

        assertEquals("PRODUCT_INQUIRY", result.intentDetected)
        assertNotNull(result.matchedProduct)
        assertEquals("Casque SoundPro Max", result.matchedProduct?.name)
        assertTrue(result.replyText.contains("149"))
    }

    @Test
    fun testPurchaseIntentGeneratesPaymentLink() {
        val result = aiEngine.analyzeAndGenerateResponse(
            incomingMessage = "Parfait, je veux commander et payer maintenant par carte !",
            contact = sampleContact,
            currentLead = null,
            products = sampleProducts,
            knowledgeDocs = emptyList(),
            aiSettings = sampleAiSettings
        )

        assertEquals("PURCHASE_INTENT", result.intentDetected)
        assertTrue(result.replyText.contains("pay.nexstore.com"))
        assertNotNull(result.updatedLeadScore)
        assertTrue((result.updatedLeadScore ?: 0) >= 80)
    }

    @Test
    fun testAppointmentIntentDetected() {
        val result = aiEngine.analyzeAndGenerateResponse(
            incomingMessage = "Pouvons-nous fixer un rendez-vous pour une démonstration ?",
            contact = sampleContact,
            currentLead = null,
            products = sampleProducts,
            knowledgeDocs = emptyList(),
            aiSettings = sampleAiSettings
        )

        assertEquals("APPOINTMENT_REQUEST", result.intentDetected)
        assertTrue(result.replyText.contains("rendez-vous"))
    }

    @Test
    fun testToneTransformation() {
        val text = "Le produit est garanti 2 ans avec retour gratuit sous 30 jours."
        val warm = aiEngine.transformTone(text, "WARM")
        val pro = aiEngine.transformTone(text, "PROFESSIONAL")

        assertTrue(warm.contains("Coucou") || warm.contains("plaisir") || warm.contains("dispo"))
        assertTrue(pro.contains("Bonjour") || pro.contains("Cordialement"))
    }

    @Test
    fun testPaymentGatewayService() {
        val service = PaymentGatewayService()
        val result = service.generatePaymentLink(
            provider = PaymentProviderType.STRIPE,
            orderId = "ord_test_99",
            amount = 549.0,
            currency = "EUR",
            customerPhone = "+33612345678"
        )

        assertNotNull(result.paymentUrl)
        assertTrue(result.paymentUrl.contains("stripe.com"))
        assertEquals("PENDING", result.status)
    }
}
