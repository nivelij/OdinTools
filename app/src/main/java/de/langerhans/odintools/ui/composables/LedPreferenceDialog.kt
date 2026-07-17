package de.langerhans.odintools.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import de.langerhans.odintools.R
import kotlin.math.roundToInt

// Red, orange, yellow, green, cyan, blue, indigo, purple, pink, white.
private val presetColors = listOf(
    Color(0xFFFF3B30),
    Color(0xFFFF9500),
    Color(0xFFFFCC00),
    Color(0xFF34C759),
    Color(0xFF32ADE6),
    Color(0xFF007AFF),
    Color(0xFF5856D6),
    Color(0xFFAF52DE),
    Color(0xFFFF2D55),
    Color(0xFFFFFFFF),
)

@Composable
fun LedPreferenceDialog(
    initialDifferentColors: Boolean,
    initialColorLeft: Int,
    initialColorRight: Int,
    initialBrightness: Int,
    onCancel: () -> Unit,
    onSave: (differentColors: Boolean, colorLeft: Int, colorRight: Int, brightness: Int) -> Unit,
) {
    var differentColors by remember { mutableStateOf(initialDifferentColors) }
    var editingRight by remember { mutableStateOf(false) }
    var left by remember { mutableStateOf(Color(initialColorLeft)) }
    var right by remember { mutableStateOf(Color(initialColorRight)) }
    var brightness by remember { mutableIntStateOf(initialBrightness) }

    val controller = rememberColorPickerController()

    // Point the wheel at the color of whichever stick is currently being edited. Runs on first
    // composition and whenever the mode or the selected side changes.
    val activeColor = if (differentColors && editingRight) right else left
    LaunchedEffect(differentColors, editingRight) {
        controller.selectByColor(activeColor, fromUser = false)
    }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            DialogButton(text = stringResource(id = R.string.save)) {
                onSave(differentColors, left.toArgb(), right.toArgb(), brightness)
            }
        },
        dismissButton = {
            DialogButton(text = stringResource(id = R.string.cancel), onCancel)
        },
        title = {
            Text(text = stringResource(id = R.string.ledLighting))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                // Same / Different mode
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !differentColors,
                        onClick = {
                            differentColors = false
                            editingRight = false
                            // Collapse to a single color so both sticks match.
                            right = left
                        },
                        label = { Text(stringResource(id = R.string.ledSameColor)) },
                    )
                    FilterChip(
                        selected = differentColors,
                        onClick = { differentColors = true },
                        label = { Text(stringResource(id = R.string.ledDifferentColor)) },
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Which stick is being edited (only relevant in "different" mode)
                if (differentColors) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SideChip(
                            label = stringResource(id = R.string.ledLeft),
                            color = left,
                            selected = !editingRight,
                        ) { editingRight = false }
                        SideChip(
                            label = stringResource(id = R.string.ledRight),
                            color = right,
                            selected = editingRight,
                        ) { editingRight = true }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Swatch(color = left, size = 28)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.ledBothSticks),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    controller = controller,
                    onColorChanged = { envelope ->
                        val color = envelope.color
                        // The picker emits a transparent color before it is laid out; ignore it.
                        if (color.alpha == 0f) return@HsvColorPicker
                        if (differentColors) {
                            if (editingRight) right = color else left = color
                        } else {
                            left = color
                            right = color
                        }
                    },
                )

                Spacer(Modifier.height(12.dp))

                // Preset colors, two rows of five
                presetColors.chunked(5).forEach { rowColors ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowColors.forEach { preset ->
                            Swatch(
                                color = preset,
                                size = 32,
                                modifier = Modifier.clickable {
                                    // Capture directly: selectByColor() alone does not fire
                                    // onColorChanged, so the pick would otherwise be lost.
                                    if (differentColors) {
                                        if (editingRight) right = preset else left = preset
                                    } else {
                                        left = preset
                                        right = preset
                                    }
                                    controller.selectByColor(preset, fromUser = true)
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Brightness
                Text(
                    text = stringResource(id = R.string.ledBrightness),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = brightness.toFloat(),
                        valueRange = 0f..100f,
                        onValueChange = { brightness = it.roundToInt() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                    )
                    Text(
                        text = "$brightness%",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        },
    )
}

@Composable
private fun SideChip(label: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = { Swatch(color = color, size = 18) },
        label = { Text(label) },
    )
}

@Composable
private fun Swatch(color: Color, size: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
    )
}
