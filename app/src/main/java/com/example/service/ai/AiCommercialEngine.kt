package com.example.service.ai

import com.example.data.local.AiSettingsEntity
import com.example.data.local.ContactEntity
import com.example.data.local.KnowledgeDocumentEntity
import com.example.data.local.LeadEntity
import com.example.data.local.ProductEntity
import java.util.Locale

data class AiDecisionResult(
    val replyText: String,
    val intentDetected: String,
    val confidence: Float,
    val matchedProduct: ProductEntity? = null,
    val isHumanHandoffRequired: Boolean = false,
    val updatedLeadScore: Int? = null,
    val updatedLeadStage: String? = null,
    val suggestedFollowUpHours: Int? = null,
    val qualificationExtracted: QualificationData? = null
)

data class QualificationData(
    val budget: Double? = null,
    val urgency: String? = null,
    val need: String? = null,
    val intent: String? = null
)

class AiCommercialEngine {

    fun analyzeAndGenerateResponse(
        incomingMessage: String,
        contact: ContactEntity,
        currentLead: LeadEntity?,
        products: List<ProductEntity>,
        knowledgeDocs: List<KnowledgeDocumentEntity>,
        aiSettings: AiSettingsEntity?,
        currency: String = "EUR"
    ): AiDecisionResult {
        val lower = incomingMessage.lowercase(Locale.ROOT).trim()

        // 1. Check for immediate human handoff triggers
        val humanTriggers = listOf(
            "humain", "conseiller", "responsable", "parler à quelqu'un", "agent",
            "plainte", "arnaque", "vol", "avocat", "remboursement", "scandale",
            "inadmissible", "rembourser", "nul", "incompétent"
        )
        if (humanTriggers.any { lower.contains(it) }) {
            return AiDecisionResult(
                replyText = "Je comprends tout à fait votre demande. Je transmets immédiatement notre conversation à un conseiller senior qui prend le relais dans un instant.",
                intentDetected = "HUMAN_ESCALATION",
                confidence = 0.99f,
                isHumanHandoffRequired = true,
                updatedLeadStage = "NEGOTIATION",
                updatedLeadScore = (currentLead?.score ?: 50).coerceAtLeast(60)
            )
        }

        // 2. Extract potential budget/urgency for qualification
        val extractedBudget = extractBudget(lower)
        val extractedUrgency = when {
            lower.contains("urgent") || lower.contains("aujourd'hui") || lower.contains("maintenant") || lower.contains("tout de suite") -> "Immédiate"
            lower.contains("cette semaine") || lower.contains("demain") -> "Haute"
            lower.contains("mois") || lower.contains("réfléchir") -> "Faible"
            else -> null
        }

        // 3. Check for Purchase Intent / Payment link request
        val paymentTriggers = listOf("payer", "lien", "commander", "acheter", "valider", "carte", "mobile money", "facture", "rib")
        if (paymentTriggers.any { lower.contains(it) }) {
            val matched = findBestMatchingProduct(lower, products)
            val productName = matched?.name ?: "votre sélection"
            val price = matched?.promoPrice ?: matched?.price ?: 149.0
            val priceStr = formatPrice(price, currency)
            val score = ((currentLead?.score ?: 60) + 25).coerceAtMost(100)

            val reply = "Superbe choix ! 🎉 Voici votre lien de commande sécurisé pour $productName ($priceStr) : https://pay.nexstore.com/checkout?ref=WA_${contact.id.takeLast(4)}. Dès validation, votre commande est prise en charge pour expédition express."
            return AiDecisionResult(
                replyText = reply,
                intentDetected = "PURCHASE_INTENT",
                confidence = 0.98f,
                matchedProduct = matched,
                updatedLeadScore = score,
                updatedLeadStage = "NEGOTIATION",
                qualificationExtracted = QualificationData(
                    budget = extractedBudget ?: price,
                    urgency = extractedUrgency ?: "Immédiate",
                    intent = "Prêt à commander"
                )
            )
        }

        // 4. Check for Appointment Request
        val appointmentTriggers = listOf("rendez-vous", "rdv", "démo", "appel", "rappeler", "créneau", "calendrier", "visio")
        if (appointmentTriggers.any { lower.contains(it) }) {
            val reply = "Avec plaisir ! 📅 Nous pouvons fixer un rendez-vous téléphonique ou en visio. Nos prochains créneaux disponibles sont : demain à 10h00 ou jeudi à 15h30. Quel créneau vous convient le mieux ?"
            val score = ((currentLead?.score ?: 50) + 15).coerceAtMost(95)
            return AiDecisionResult(
                replyText = reply,
                intentDetected = "APPOINTMENT_REQUEST",
                confidence = 0.95f,
                updatedLeadScore = score,
                updatedLeadStage = "PROPOSAL"
            )
        }

        // 5. Check for "Je vais réfléchir" / Hesitation -> Follow-up trigger
        if (lower.contains("réfléchir") || lower.contains("hésite") || lower.contains("plus tard") || lower.contains("reviens vers vous")) {
            val reply = "Je comprends tout à fait, prenez le temps nécessaire ! 😊 Je reste disponible à tout moment sur ce numéro si vous avez la moindre question sur nos caractéristiques ou facilités de paiement. Excellente journée !"
            return AiDecisionResult(
                replyText = reply,
                intentDetected = "HESITATION",
                confidence = 0.94f,
                suggestedFollowUpHours = 24,
                updatedLeadStage = "CONTACTED",
                updatedLeadScore = currentLead?.score ?: 45
            )
        }

        // 6. Check for Delivery / Shipping / Policies in Knowledge Base
        val deliveryTriggers = listOf("livraison", "livrer", "frais de port", "délai", "combien de temps", "retard", "ville", "pays", "dakar", "paris", "abidjan", "lyon")
        if (deliveryTriggers.any { lower.contains(it) }) {
            val policyDoc = knowledgeDocs.firstOrNull { it.type == "POLICY" || it.tags.contains("Livraison") }
            val info = policyDoc?.content ?: "Nous assurons la livraison sous 24h à 48h ouvrées. La livraison est offerte à partir de 100€ d'achat."
            return AiDecisionResult(
                replyText = "Bonjour ${contact.name.split(" ").firstOrNull() ?: ""} ! 📦 Concernant notre logistique : $info. Dans quelle ville ou zone souhaitez-vous être livré(e) ?",
                intentDetected = "DELIVERY_INFO",
                confidence = 0.96f
            )
        }

        // 7. Product recommendation by budget / feature
        if (extractedBudget != null || lower.contains("recommande") || lower.contains("conseil") || lower.contains("cherche") || lower.contains("budget")) {
            val budget = extractedBudget ?: 500.0
            val affordableProducts = products.filter { (it.promoPrice ?: it.price) <= budget * 1.15 }
            val bestPick = affordableProducts.maxByOrNull { it.promoPrice ?: it.price } ?: products.firstOrNull()
            if (bestPick != null) {
                val priceStr = formatPrice(bestPick.promoPrice ?: bestPick.price, currency)
                val reply = "Pour votre budget (environ ${formatPrice(budget, currency)}), je vous conseille chaleureusement : **${bestPick.name}** à $priceStr.\n\n✨ Points forts : ${bestPick.features.ifEmpty { bestPick.description }}\n\nSouhaitez-vous que je vous réserve un exemplaire en stock (${bestPick.stock} restants) ?"
                val score = ((currentLead?.score ?: 40) + 20).coerceAtMost(90)
                return AiDecisionResult(
                    replyText = reply,
                    intentDetected = "RECOMMENDATION",
                    confidence = 0.97f,
                    matchedProduct = bestPick,
                    updatedLeadScore = score,
                    updatedLeadStage = "QUALIFIED",
                    qualificationExtracted = QualificationData(
                        budget = budget,
                        need = bestPick.name,
                        urgency = extractedUrgency ?: "Moyenne",
                        intent = "Intéressé"
                    )
                )
            }
        }

        // 8. Specific Product Match (Price / Availability / Features)
        val matchedProduct = findBestMatchingProduct(lower, products)
        if (matchedProduct != null) {
            val priceStr = formatPrice(matchedProduct.promoPrice ?: matchedProduct.price, currency)
            val promoBadge = if (matchedProduct.promoPrice != null) " (en promotion au lieu de ${formatPrice(matchedProduct.price, currency)})" else ""
            val stockText = if (matchedProduct.stock > 0) "Actuellement en stock (${matchedProduct.stock} unités prêtes à expédier)" else "Actuellement en rupture temporaire"

            val reply = "Bonjour ! Concernant **${matchedProduct.name}** :\n\n💰 Prix : $priceStr$promoBadge\n📦 Disponibilité : $stockText\nℹ️ Caractéristiques : ${matchedProduct.description}\n\nSouhaitez-vous passer commande ou recevoir le récapitulatif ?"
            val score = ((currentLead?.score ?: 40) + 15).coerceAtMost(85)
            return AiDecisionResult(
                replyText = reply,
                intentDetected = "PRODUCT_INQUIRY",
                confidence = 0.96f,
                matchedProduct = matchedProduct,
                updatedLeadScore = score,
                updatedLeadStage = "QUALIFIED",
                qualificationExtracted = QualificationData(
                    need = matchedProduct.name,
                    budget = matchedProduct.promoPrice ?: matchedProduct.price
                )
            )
        }

        // 9. Discount Request / Negotiation
        if (lower.contains("remise") || lower.contains("réduction") || lower.contains("rabais") || lower.contains("promo") || lower.contains("moins cher")) {
            val reply = "Nous proposons actuellement des tarifs préférentiels pour les commandes groupées ou les premiers achats ! Pourriez-vous m'indiquer la quantité souhaitée ou le modèle qui vous intéresse afin que je vérifie notre meilleure offre commerciale ?"
            return AiDecisionResult(
                replyText = reply,
                intentDetected = "DISCOUNT_REQUEST",
                confidence = 0.93f,
                updatedLeadStage = "NEGOTIATION"
            )
        }

        // 10. General / Greeting / Fallback
        val assistantName = aiSettings?.assistantName ?: "Aria"
        val greeting = if (lower.contains("bonjour") || lower.contains("salut") || lower.contains("hello") || lower.contains("hi")) {
            "Bonjour ${contact.name.split(" ").firstOrNull() ?: ""} ! 👋 Je suis $assistantName, votre conseiller commercial dédié."
        } else {
            "Merci pour votre message !"
        }

        val availableCatalog = products.take(3).joinToString(", ") { "${it.name} (${formatPrice(it.promoPrice ?: it.price, currency)})" }
        val reply = "$greeting\n\nComment puis-je vous aider aujourd'hui ? Nos meilleures références du moment sont : $availableCatalog.\n\nRecherchez-vous un modèle en particulier ou une recommandation selon votre budget ?"

        return AiDecisionResult(
            replyText = reply,
            intentDetected = "GENERAL_INQUIRY",
            confidence = 0.90f,
            updatedLeadScore = currentLead?.score ?: 40,
            updatedLeadStage = "CONTACTED"
        )
    }

    fun transformTone(
        originalText: String,
        style: String // "SHORT", "EXPANDED", "PROFESSIONAL", "WARM"
    ): String {
        return when (style) {
            "SHORT" -> {
                originalText.lines().firstOrNull { it.isNotBlank() } ?: originalText.take(120) + "..."
            }
            "EXPANDED" -> {
                "$originalText\n\nN'hésitez pas si vous avez la moindre interrogation supplémentaire sur les garanties, modes de livraison express ou modalités de paiement sécurisé. Notre équipe reste à votre écoute pour vous garantir la meilleure satisfaction !"
            }
            "PROFESSIONAL" -> {
                "Bonjour. Faisant suite à votre sollicitation, nous vous informons que : ${originalText.removePrefix("Bonjour !").trim()}\n\nRestant à votre entière disposition pour tout renseignement complémentaire.\nCordialement, Le Service Commercial."
            }
            "WARM" -> {
                "Coucou ! Ravie d'échanger avec vous 😊 $originalText Je vous souhaite une merveilleuse journée et reste dispo avec plaisir !"
            }
            else -> originalText
        }
    }

    private fun findBestMatchingProduct(query: String, products: List<ProductEntity>): ProductEntity? {
        val q = query.lowercase()
        return products.firstOrNull { p ->
            val pName = p.name.lowercase()
            val pSku = p.sku.lowercase()
            val pCat = p.category.lowercase()
            q.contains(pName) || pName.split(" ").any { word -> word.length > 3 && q.contains(word) } || q.contains(pSku) || q.contains(pCat)
        }
    }

    private fun extractBudget(text: String): Double? {
        val regex = Regex("""(\d+[\s\d]*)\s*(€|eur|fcfa|cfa|\$|usd)?""", RegexOption.IGNORE_CASE)
        val match = regex.find(text) ?: return null
        val numStr = match.groupValues[1].replace(" ", "").trim()
        return numStr.toDoubleOrNull()
    }

    private fun formatPrice(amount: Double, currency: String): String {
        return when (currency.uppercase()) {
            "EUR" -> "%.2f €".format(amount)
            "FCFA", "XOF" -> "%,.0f FCFA".format(amount)
            "USD" -> "$%.2f".format(amount)
            else -> "%.2f %s".format(amount, currency)
        }
    }
}
