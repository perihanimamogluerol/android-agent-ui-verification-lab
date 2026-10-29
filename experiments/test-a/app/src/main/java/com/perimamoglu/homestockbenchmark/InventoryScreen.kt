package com.perimamoglu.homestockbenchmark

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.perimamoglu.homestockbenchmark.ui.theme.HomeStockBenchmarkTheme

enum class Category(val label: String) {
    LegumesAndGrains("Legumes & Grains"),
    Vegetables("Vegetables"),
    Fruits("Fruits"),
    Snacks("Snacks"),
    Drinks("Drinks"),
    HouseholdEssentials("Household Essentials"),
}

data class Product(
    val id: Int,
    val name: String,
    val category: Category,
    val quantity: Int,
)

private fun sampleProducts(): List<Product> = listOf(
    Product(1, "Red Lentils", Category.LegumesAndGrains, 3),
    Product(2, "Basmati Rice", Category.LegumesAndGrains, 0),
    Product(3, "Carrots", Category.Vegetables, 6),
    Product(4, "Spinach", Category.Vegetables, 0),
    Product(5, "Apples", Category.Fruits, 8),
    Product(6, "Bananas", Category.Fruits, 5),
    Product(7, "Crackers", Category.Snacks, 2),
    Product(8, "Mixed Nuts", Category.Snacks, 4),
    Product(9, "Orange Juice", Category.Drinks, 1),
    Product(10, "Sparkling Water", Category.Drinks, 12),
    Product(11, "Dish Soap", Category.HouseholdEssentials, 2),
    Product(12, "Paper Towels", Category.HouseholdEssentials, 0),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(modifier: Modifier = Modifier) {
    val products = remember { mutableStateListOf<Product>().apply { addAll(sampleProducts()) } }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { CenterAlignedTopAppBar(title = { Text("Household Inventory") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAddDialog = true }) {
                Text("Add product")
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Category.entries.forEach { category ->
                val inCategory = products.filter { it.category == category }
                item(key = "header-${category.name}") {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(inCategory, key = { it.id }) { product ->
                    ProductRow(
                        product = product,
                        onQuantityChange = { delta ->
                            val index = products.indexOfFirst { it.id == product.id }
                            if (index >= 0) {
                                products[index] = product.copy(
                                    quantity = (product.quantity + delta).coerceAtLeast(0),
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, category, quantity ->
                val nextId = (products.maxOfOrNull { it.id } ?: 0) + 1
                products.add(Product(nextId, name, category, quantity))
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun ProductRow(
    product: Product,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val outOfStock = product.quantity == 0
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (outOfStock) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(product.category.label, style = MaterialTheme.typography.bodySmall)
                if (outOfStock) {
                    SuggestionChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("Out of stock") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            disabledLabelColor = MaterialTheme.colorScheme.error,
                        ),
                    )
                }
            }
            FilledTonalIconButton(
                onClick = { onQuantityChange(-1) },
                enabled = !outOfStock,
            ) { Text("−") }
            Text(
                text = product.quantity.toString(),
                style = MaterialTheme.typography.titleLarge,
            )
            FilledTonalIconButton(onClick = { onQuantityChange(1) }) { Text("+") }
        }
    }
}

@Composable
private fun AddProductDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, category: Category, quantity: Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.LegumesAndGrains) }
    var quantityText by remember { mutableStateOf("1") }
    val quantity = quantityText.toIntOrNull()
    val canAdd = name.isNotBlank() && quantity != null && quantity >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Category.entries.forEach { option ->
                        FilterChip(
                            selected = option == category,
                            onClick = { category = option },
                            label = { Text(option.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input -> quantityText = input.filter { it.isDigit() }.take(6) },
                    label = { Text("Initial quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name.trim(), category, quantity ?: 0) },
                enabled = canAdd,
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Preview(name = "Phone", widthDp = 360, heightDp = 780, showBackground = true)
@Composable
fun InventoryScreenPhonePreview() {
    HomeStockBenchmarkTheme { InventoryScreen() }
}

@Preview(name = "Tablet", widthDp = 840, heightDp = 1180, showBackground = true)
@Composable
fun InventoryScreenTabletPreview() {
    HomeStockBenchmarkTheme { InventoryScreen() }
}
