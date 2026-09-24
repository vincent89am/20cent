package com.example.service.automation

import com.example.data.local.AutomationWorkflowEntity
import com.example.data.local.ContactEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.LeadEntity

data class AutomationExecutionResult(
    val workflowName: String,
    val actionTaken: String,
    val generatedMessage: String?,
    val shouldNotifyAgent: Boolean
)

class AutomationEngine {

    fun evaluateEvent(
        eventType: String, // "NO_REPLY_24H", "PRICE_REQUEST", "QUALIFIED_LEAD", "ABANDONED_CART"
        contact: ContactEntity,
        lead: LeadEntity?,
        conversation: ConversationEntity,
        workflows: List<AutomationWorkflowEntity>
    ): List<AutomationExecutionResult> {
        val results = mutableListOf<AutomationExecutionResult>()
        val matching = workflows.filter { it.isActive && it.triggerType == eventType }

        for (wf in matching) {
            when (wf.actionType) {
                "AI_FOLLOW_UP" -> {
                    val firstName = contact.name.split(" ").firstOrNull() ?: contact.name
                    val need = lead?.needDescription?.ifEmpty { "le modèle qui vous intéressait" } ?: "notre proposition"
                    val msg = "Bonjour $firstName 👋 Je voulais simplement savoir si vous aviez eu le temps de réfléchir pour $need. Je reste à votre entière disposition si vous avez la moindre question !"
                    results.add(
                        AutomationExecutionResult(
                            workflowName = wf.name,
                            actionTaken = "Envoi relance automatique IA (24h)",
                            generatedMessage = msg,
                            shouldNotifyAgent = false
                        )
                    )
                }
                "NOTIFY_HUMAN" -> {
                    results.add(
                        AutomationExecutionResult(
                            workflowName = wf.name,
                            actionTaken = "Notification commerciale urgente : Prospect très chaud (Score: ${lead?.score ?: 80}/100)",
                            generatedMessage = null,
                            shouldNotifyAgent = true
                        )
                    )
                }
                "SEND_TEMPLATE" -> {
                    results.add(
                        AutomationExecutionResult(
                            workflowName = wf.name,
                            actionTaken = "Envoi fiche produit et grille tarifaire",
                            generatedMessage = "Voici la fiche technique complète de nos produits ainsi que les promotions du mois en cours.",
                            shouldNotifyAgent = false
                        )
                    )
                }
            }
        }
        return results
    }
}
