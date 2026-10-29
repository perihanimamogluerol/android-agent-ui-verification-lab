package com.perimamoglu.homestockbenchmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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

private val sampleProducts = listOf(
    Product(1, "Red Lentils", Category.LegumesAndGrains, 3),
    Product(2, "Basmati Rice", Category.LegumesAndGrains, 0),
    Product(3, "Carrots", Category.Vegetables, 6),
    Product(4, "Spinach", Category.Vegetables, 0),
    Product(5, "Apples", Category.Fruits, 8),
    Product(6, "Bananas", Category.Fruits, 4),
    Product(7, "Crackers", Category.Snacks, 2),
    Product(8, "Dark Chocolate", Category.Snacks, 0),
    Product(9, "Orange Juice", Category.Drinks, 2),
    Product(10, "Sparkling Water", Category.Drinks, 12),
    Product(11, "Dish Soap", Category.HouseholdEssentials, 1),
    Product(12, "Paper Towels", Category.HouseholdEssentials, 0),
)

@Composable
fun InventoryScreen() {
    val products = remember { sampleProducts.toMutableStateList() }
    var nextId by remember { mutableStateOf(sampleProducts.size + 1) }

    InventoryContent(
        products = products,
        onQuantityChange = { id, delta ->
            val index = products.indexOfFirst { it.id == id }
            if (index >= 0) {
                val p = products[index]
                products[index] = p.copy(quantity = (p.quantity + delta).coerceAtLeast(0))
            }
        },
        onAddProduct = { name, category, quantity ->
            products.add(Product(nextId++, name, category, quantity))
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryContent(
    products: List<Product>,
    onQuantityChange: (id: Int, delta: Int) -> Unit,
    onAddProduct: (name: String, category: Category, quantity: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { CenterAlignedTopAppBar(title = { Text("Household Inventory") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+ Add product")
            }
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 88.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Category.entries.forEach { category ->
                val inCategory = products.filter { it.category == category }
                item(key = "header-${category.name}", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                if (inCategory.isEmpty()) {
                    item(key = "empty-${category.name}", span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "No products yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(inCategory, key = { it.id }) { product ->
                    ProductCard(product, onQuantityChange)
                }
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, quantity ->
                onAddProduct(name, category, quantity)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onQuantityChange: (id: Int, delta: Int) -> Unit,
) {
    val outOfStock = product.quantity == 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (outOfStock) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
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
                onClick = { onQuantityChange(product.id, -1) },
                enabled = !outOfStock,
            ) { Text("−", style = MaterialTheme.typography.titleLarge) }
            Text(
                text = product.quantity.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            FilledTonalIconButton(onClick = { onQuantityChange(product.id, 1) }) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: Category, quantity: Int) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.LegumesAndGrains) }
    var quantityText by remember { mutableStateOf("1") }
    var menuOpen by remember { mutableStateOf(false) }
    val quantity = quantityText.toIntOrNull()
    val valid = name.isNotBlank() && quantity != null && quantity >= 0

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
                Column {
                    OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(category.label)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        Category.entries.forEach {
                            DropdownMenuItem(
                                text = { Text(it.label) },
                                onClick = {
                                    category = it
                                    menuOpen = false
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { text -> quantityText = text.filter(Char::isDigit).take(4) },
                    label = { Text("Initial quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onConfirm(name.trim(), category, quantity!!) },
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Preview(name = "Phone", widthDp = 411, heightDp = 891, showBackground = true)
@Composable
fun InventoryPhonePreview() {
    HomeStockBenchmarkTheme {
        InventoryContent(sampleProducts, { _, _ -> }, { _, _, _ -> })
    }
}

@Preview(name = "Tablet", widthDp = 1280, heightDp = 800, showBackground = true)
@Composable
fun InventoryTabletPreview() {
    HomeStockBenchmarkTheme {
        InventoryContent(sampleProducts, { _, _ -> }, { _, _, _ -> })
    }
}
