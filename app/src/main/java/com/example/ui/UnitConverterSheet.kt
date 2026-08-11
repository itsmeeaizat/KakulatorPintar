package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.DecimalFormat

enum class UnitCategory(val label: String) {
    LENGTH("Panjang"),
    WEIGHT("Berat"),
    TEMPERATURE("Suhu"),
    AREA("Luas"),
    VOLUME("Volume")
}

data class UnitItem(val code: String, val name: String, val factorToBase: Double)

object UnitData {
    val lengthUnits = listOf(
        UnitItem("km", "Kilometer (km)", 1000.0),
        UnitItem("m", "Meter (m)", 1.0),
        UnitItem("cm", "Sentimeter (cm)", 0.01),
        UnitItem("mm", "Milimeter (mm)", 0.001),
        UnitItem("mi", "Mil (mile)", 1609.344),
        UnitItem("yd", "Yard (yd)", 0.9144),
        UnitItem("ft", "Kaki (foot)", 0.3048),
        UnitItem("in", "Inci (inch)", 0.0254)
    )

    val weightUnits = listOf(
        UnitItem("t", "Ton (t)", 1000000.0),
        UnitItem("kg", "Kilogram (kg)", 1000.0),
        UnitItem("hg", "Hektogram / Ons (hg)", 100.0),
        UnitItem("g", "Gram (g)", 1.0),
        UnitItem("mg", "Miligram (mg)", 0.001),
        UnitItem("lb", "Pound (lbs)", 453.59237),
        UnitItem("oz", "Ounce (oz)", 28.349523125)
    )

    val temperatureUnits = listOf(
        UnitItem("C", "Celsius (°C)", 1.0),
        UnitItem("F", "Fahrenheit (°F)", 1.0),
        UnitItem("K", "Kelvin (K)", 1.0),
        UnitItem("R", "Reamur (°R)", 1.0)
    )

    val areaUnits = listOf(
        UnitItem("km2", "Kilometer persegi (km²)", 1000000.0),
        UnitItem("ha", "Hektar (ha)", 10000.0),
        UnitItem("m2", "Meter persegi (m²)", 1.0),
        UnitItem("cm2", "Sentimeter persegi (cm²)", 0.0001),
        UnitItem("ac", "Acre (ac)", 4046.8564224),
        UnitItem("ft2", "Kaki persegi (sq ft)", 0.09290304)
    )

    val volumeUnits = listOf(
        UnitItem("m3", "Meter kubik (m³)", 1000.0),
        UnitItem("l", "Liter (L)", 1.0),
        UnitItem("ml", "Mililiter (mL)", 0.001),
        UnitItem("gal", "Galon (US gal)", 3.78541),
        UnitItem("pt", "Pint (US pt)", 0.473176)
    )

    fun getUnitsForCategory(category: UnitCategory): List<UnitItem> {
        return when (category) {
            UnitCategory.LENGTH -> lengthUnits
            UnitCategory.WEIGHT -> weightUnits
            UnitCategory.TEMPERATURE -> temperatureUnits
            UnitCategory.AREA -> areaUnits
            UnitCategory.VOLUME -> volumeUnits
        }
    }

    fun convert(value: Double, from: UnitItem, to: UnitItem, category: UnitCategory): Double {
        if (category == UnitCategory.TEMPERATURE) {
            return convertTemperature(value, from.code, to.code)
        }
        val baseValue = value * from.factorToBase
        return baseValue / to.factorToBase
    }

    private fun convertTemperature(value: Double, fromCode: String, toCode: String): Double {
        val celsius = when (fromCode) {
            "C" -> value
            "F" -> (value - 32.0) * 5.0 / 9.0
            "K" -> value - 273.15
            "R" -> value * 5.0 / 4.0
            else -> value
        }
        return when (toCode) {
            "C" -> celsius
            "F" -> celsius * 9.0 / 5.0 + 32.0
            "K" -> celsius + 273.15
            "R" -> celsius * 4.0 / 5.0
            else -> celsius
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterSheet(
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    val availableUnits = remember(selectedCategory) { UnitData.getUnitsForCategory(selectedCategory) }

    var fromUnit by remember(selectedCategory) { mutableStateOf(availableUnits[0]) }
    var toUnit by remember(selectedCategory) { mutableStateOf(availableUnits[1]) }
    var inputValue by remember { mutableStateOf("1") }

    val resultValue = remember(inputValue, fromUnit, toUnit, selectedCategory) {
        val num = inputValue.toDoubleOrNull() ?: 0.0
        val res = UnitData.convert(num, fromUnit, toUnit, selectedCategory)
        val df = DecimalFormat("#,##0.######")
        df.format(res)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val blockSheetSwipe = rememberBlockSheetSwipeNestedScrollConnection()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .blockSheetDragFromContent()
                .nestedScroll(blockSheetSwipe)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PurplePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = OnPurplePrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Konverter Satuan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Tutup", tint = TextDark)
                }
            }

            // Tabs / Category Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitCategory.values().forEach { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategory = category
                            val units = UnitData.getUnitsForCategory(category)
                            fromUnit = units[0]
                            toUnit = units[1]
                        },
                        label = {
                            Text(
                                text = category.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimaryContainer,
                            selectedLabelColor = OnPurplePrimaryContainer,
                            containerColor = KeypadBackground,
                            labelColor = TextDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Input and Result Box
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KeypadBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Input Section
                    Text("Nilai Asal", fontSize = 12.sp, color = TextSubtle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutTextField(
                            value = inputValue,
                            onValueChange = { inputValue = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_unit_value")
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        UnitDropdown(
                            units = availableUnits,
                            selectedUnit = fromUnit,
                            onUnitSelected = { fromUnit = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Swap Button Center
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                val temp = fromUnit
                                fromUnit = toUnit
                                toUnit = temp
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PurplePrimary)
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Tukar Satuan",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Result Section
                    Text("Hasil Konversi", fontSize = 12.sp, color = TextSubtle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = resultValue,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = PurplePrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        UnitDropdown(
                            units = availableUnits,
                            selectedUnit = toUnit,
                            onUnitSelected = { toUnit = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Numpad / Keypad Input Helper
            Text("Keypad Cepat", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            val buttons = listOf(
                listOf("7", "8", "9", "C"),
                listOf("4", "5", "6", "⌫"),
                listOf("1", "2", "3", "00"),
                listOf("0", ".", "-", "=")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                buttons.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { btn ->
                            Button(
                                onClick = {
                                    when (btn) {
                                        "C" -> inputValue = "0"
                                        "⌫" -> {
                                            if (inputValue.length > 1) {
                                                inputValue = inputValue.dropLast(1)
                                            } else {
                                                inputValue = "0"
                                            }
                                        }
                                        "." -> {
                                            if (!inputValue.contains(".")) {
                                                inputValue += "."
                                            }
                                        }
                                        "-" -> {
                                            if (inputValue.startsWith("-")) {
                                                inputValue = inputValue.removePrefix("-")
                                            } else if (inputValue != "0") {
                                                inputValue = "-$inputValue"
                                            }
                                        }
                                        "00" -> {
                                            if (inputValue != "0") {
                                                inputValue += "00"
                                            }
                                        }
                                        "=" -> { /* Automatic realtime */ }
                                        else -> {
                                            if (inputValue == "0") {
                                                inputValue = btn
                                            } else {
                                                inputValue += btn
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (btn in listOf("C", "⌫")) UtilityKeyContainer else KeypadBackground,
                                    contentColor = if (btn in listOf("C", "⌫")) OnUtilityKeyContainer else TextDark
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = btn,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value.ifEmpty { "0" },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextDark
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    units: List<UnitItem>,
    selectedUnit: UnitItem,
    onUnitSelected: (UnitItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.width(130.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PurplePrimaryContainer,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedUnit.code,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnPurplePrimaryContainer
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = unit.name,
                            fontSize = 13.sp,
                            fontWeight = if (unit == selectedUnit) FontWeight.Bold else FontWeight.Normal,
                            color = TextDark
                        )
                    },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}
