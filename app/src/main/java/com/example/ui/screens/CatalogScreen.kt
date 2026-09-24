package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.ProductEntity
import com.example.ui.SalesSaaSViewModel
import com.example.ui.theme.*

@Composable
fun CatalogScreen(
    viewModel: SalesSaaSViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val organization by viewModel.organization.collectAsStateWithLifecycle()
    val currency = organization?.currency ?: "EUR"

    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Recommendation Query Sandbox state
    var recommendationQuery by remember { mutableStateOf("") }
    var recommendationResult by remember { mutableStateOf<String?>(null) }

    val categories = listOf("ALL" to "Tous") + products.map { it.category }.distinct().map { it to it }

    val filteredProducts = products.filter {
        (selectedCategory == "ALL" || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("catalog_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Catalogue Commercial & Produits", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "${products.size} articles connectés à l'IA WhatsApp", fontSize = 12.sp, color = Slate600)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("catalog_btn_add")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ajouter", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher un produit, référence, SKU...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate500) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Categories Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { (code, label) ->
                val isSelected = selectedCategory == code
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = code },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WhatsAppGreenDark,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AI Recommendation Tester Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = AiVioletLight.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = AiViolet, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Testeur de recommandation IA WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AiVioletDark)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = recommendationQuery,
                        onValueChange = { recommendationQuery = it },
                        placeholder = { Text("Ex: Casque avec réduction de bruit pour 150€", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val lower = recommendationQuery.lowercase()
                            val match = products.firstOrNull { lower.contains(it.category.lowercase()) || lower.contains("casque") && it.category == "Audio" } ?: products.firstOrNull()
                            if (match != null) {
                                recommendationResult = "✨ Recommandation IA : ${match.name} (${match.promoPrice ?: match.price} $currency) - ${match.features}"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AiViolet),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tester", fontSize = 11.sp)
                    }
                }
                if (recommendationResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = recommendationResult!!, fontSize = 11.sp, color = Slate900, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Products List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredProducts, key = { it.id }) { product ->
                ProductItemCard(
                    product = product,
                    currency = currency,
                    onDelete = { viewModel.deleteProduct(product) }
                )
            }
        }
    }

    // Add Product Modal
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Smartphones") }
        var priceStr by remember { mutableStateOf("") }
        var promoPriceStr by remember { mutableStateOf("") }
        var stockStr by remember { mutableStateOf("10") }
        var sku by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var features by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Ajouter un nouveau produit", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom du produit *") }, singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Catégorie *") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Prix ($currency) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = promoPriceStr,
                            onValueChange = { promoPriceStr = it },
                            label = { Text("Prix Promo ($currency)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            label = { Text("Stock initial") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU / Réf") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description pour l'IA") }, maxLines = 3)
                    OutlinedTextField(value = features, onValueChange = { features = it }, label = { Text("Points forts / Variantes") }, maxLines = 2)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = priceStr.toDoubleOrNull() ?: 0.0
                        val pp = promoPriceStr.toDoubleOrNull()
                        val st = stockStr.toIntOrNull() ?: 1
                        if (name.isNotBlank() && p > 0) {
                            viewModel.addProduct(
                                name = name,
                                category = category,
                                price = p,
                                promoPrice = pp,
                                stock = st,
                                sku = sku.ifEmpty { "REF-${System.currentTimeMillis().toString().takeLast(4)}" },
                                desc = desc,
                                features = features
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark)
                ) {
                    Text("Créer le produit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    currency: String,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${product.category} · Réf: ${product.sku}",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (product.promoPrice != null) {
                        Text(
                            text = "%.0f %s".format(product.promoPrice, currency),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WhatsAppGreenDark
                        )
                        Text(
                            text = "%.0f %s".format(product.price, currency),
                            fontSize = 11.sp,
                            color = Slate500,
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        )
                    } else {
                        Text(
                            text = "%.0f %s".format(product.price, currency),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = product.description,
                fontSize = 12.sp,
                color = Slate700,
                lineHeight = 16.sp
            )

            if (product.features.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "✨ ${product.features}",
                        fontSize = 11.sp,
                        color = Slate800,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (product.stock > 5) GreenSuccess.copy(alpha = 0.15f) else GoldWarning.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Stock : ${product.stock} unités",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.stock > 5) GreenSuccess else GoldWarning,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Supprimer",
                        tint = RedDanger.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
