package com.example.service.whatsapp

data class WhatsAppCloudConfig(
    val phoneNumberId: String = "104928374928174",
    val wabaId: String = "948271630192847",
    val permanentToken: String = "EAAG...configured",
    val webhookVerifyToken: String = "whatsapp_saas_verify_secret_2026",
    val webhookCallbackUrl: String = "https://api.commercial-saas.com/v1/whatsapp/webhook",
    val isWebhookActive: Boolean = true
)

data class WhatsAppTemplate(
    val id: String,
    val name: String,
    val category: String, // WELCOME, ORDER_CONFIRMATION, FOLLOW_UP, APPOINTMENT, PROMOTION
    val language: String = "fr",
    val headerText: String = "",
    val bodyText: String,
    val footerText: String = "Répondez STOP pour vous désabonner",
    val buttonText: String? = null
)

class WhatsAppPlatformService {

    val availableTemplates = listOf(
        WhatsAppTemplate(
            id = "tpl_welcome",
            name = "bienvenue_commerciale",
            category = "WELCOME",
            bodyText = "Bonjour {{1}} 👋 Merci de contacter {{2}} ! Comment pouvons-nous vous accompagner aujourd'hui ?",
            buttonText = "Découvrir le catalogue"
        ),
        WhatsAppTemplate(
            id = "tpl_followup_24h",
            name = "relance_douce_24h",
            category = "FOLLOW_UP",
            bodyText = "Bonjour {{1}} 👋 Je voulais simplement savoir si vous aviez eu le temps de réfléchir au modèle {{2}}. Je reste disponible si vous avez une question !",
            buttonText = "Voir les détails"
        ),
        WhatsAppTemplate(
            id = "tpl_order_confirmed",
            name = "confirmation_commande",
            category = "ORDER_CONFIRMATION",
            bodyText = "Merci {{1}} ! 🎉 Votre commande n° {{2}} d'un montant de {{3}} a bien été confirmée. Préparation en cours.",
            buttonText = "Suivre mon colis"
        ),
        WhatsAppTemplate(
            id = "tpl_appointment",
            name = "rappel_rendez_vous",
            category = "APPOINTMENT",
            bodyText = "Bonjour {{1}}, nous vous confirmons votre rendez-vous commercial le {{2}} à {{3}} avec votre conseiller.",
            buttonText = "Confirmer ma présence"
        )
    )

    fun buildRenderedTemplate(template: WhatsAppTemplate, params: List<String>): String {
        var text = template.bodyText
        params.forEachIndexed { index, value ->
            text = text.replace("{{${index + 1}}}", value)
        }
        return text
    }
}
