package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    trendPercent: String? = null
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stat_card_${title.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (trendPercent != null) {
                    Surface(
                        color = GreenSuccess.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = GreenSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trendPercent,
                                color = GreenSuccess,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Slate500
            )
        }
    }
}

@Composable
fun LeadScoreBadge(score: Int) {
    val (bg, fg, label) = when {
        score >= 71 -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "Chaud 🔥")
        score >= 31 -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "Intéressé ⚡")
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), "Froid ❄️")
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("lead_score_badge_$score")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$score/100 · $label",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = fg
            )
        }
    }
}

@Composable
fun ConversationStatusBadge(status: String) {
    val (bg, fg, label) = when (status) {
        "AI_ACTIVE" -> Triple(WhatsAppGreenLight.copy(alpha = 0.2f), WhatsAppGreenDark, "IA Active")
        "COPILOT" -> Triple(AiVioletLight, AiViolet, "Copilote")
        "HUMAN_REQUIRED" -> Triple(RedDanger.copy(alpha = 0.15f), RedDanger, "Humain requis ⚠️")
        "CLOSED" -> Triple(Slate200, Slate600, "Fermée")
        else -> Triple(BlueInfo.copy(alpha = 0.15f), BlueInfo, "Ouverte")
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CrmStageBadge(stage: String) {
    val (color, text) = when (stage) {
        "NEW" -> Pair(BlueInfo, "Nouveau")
        "CONTACTED" -> Pair(Slate500, "Contacté")
        "QUALIFIED" -> Pair(WhatsAppGreenPrimary, "Qualifié")
        "PROPOSAL" -> Pair(AiViolet, "Proposition")
        "NEGOTIATION" -> Pair(GoldWarning, "Négociation")
        "WON" -> Pair(GreenSuccess, "Gagné 🎉")
        "LOST" -> Pair(RedDanger, "Perdu")
        else -> Pair(Slate500, stage)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
