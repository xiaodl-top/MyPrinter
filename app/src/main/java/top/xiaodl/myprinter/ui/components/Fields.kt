package top.xiaodl.myprinter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.xiaodl.myprinter.ui.HintColor
import top.xiaodl.myprinter.ui.TextPrimary
import top.xiaodl.myprinter.ui.TextSecondary
import top.xiaodl.myprinter.ui.brandGradient
import top.xiaodl.myprinter.ui.myPrinterColors

/**
 * The app's single-line input. It has no Material box: the field is a soft filled surface whose
 * hairline border warms up to the brand colour and lifts a coloured glow while focused — a small
 * detail that makes typing feel connected to the UI.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIconRes: Int? = null,
    trailing: (@Composable () -> Unit)? = null,
    onFocusChange: ((Boolean) -> Unit)? = null,
    onImeAction: (() -> Unit)? = null,
    enabled: Boolean = true,
    minHeight: Dp = 54.dp,
) {
    val colors = myPrinterColors
    val focusManager = LocalFocusManager.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(17.dp)
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.border
            focused -> colors.brandStart.copy(alpha = 0.85f)
            else -> colors.border
        },
        animationSpec = tween(Motion.Normal),
        label = "fieldBorder",
    )
    val glow by animateDpAsState(
        targetValue = if (focused && !colors.isDark) 14.dp else 0.dp,
        animationSpec = tween(Motion.Normal),
        label = "fieldGlow",
    )
    val fill by animateColorAsState(
        targetValue = if (focused) {
            if (colors.isDark) colors.surfaceMuted else Color.White
        } else {
            colors.fieldFill
        },
        animationSpec = tween(Motion.Normal),
        label = "fieldFill",
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        enabled = enabled,
        textStyle = TextStyle(
            fontSize = 15.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        ),
        cursorBrush = SolidColor(colors.brandStart),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) },
            onDone = {
                focusManager.clearFocus()
                onImeAction?.invoke()
            },
        ),
        interactionSource = interaction,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .onFocusChanged { onFocusChange?.invoke(it.isFocused) }
            .shadow(
                elevation = glow,
                shape = shape,
                ambientColor = colors.brandStart.copy(alpha = 0.40f),
                spotColor = colors.brandStart.copy(alpha = 0.35f),
            )
            .clip(shape)
            .background(fill)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 15.dp, vertical = 15.dp),
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIconRes != null) {
                    Icon(
                        painter = painterResource(leadingIconRes),
                        contentDescription = null,
                        tint = if (focused) colors.brandStart else colors.textTertiary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = HintColor,
                            maxLines = 1,
                        )
                    }
                    innerTextField()
                }
                if (trailing != null) {
                    Spacer(Modifier.width(8.dp))
                    trailing()
                }
            }
        },
    )
}

/** A label above a field, so long hints stay readable instead of being squeezed into the box. */
@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.2.sp,
        color = TextSecondary,
    )
}

/**
 * Copies stepper: minus / editable value / plus. The editable middle keeps free typing possible
 * (the view model already sanitises digits and the 3-digit limit).
 */
@Composable
fun QuantityStepper(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 1,
    max: Int = 999,
) {
    val colors = myPrinterColors
    val shape = RoundedCornerShape(17.dp)
    val current = value.toIntOrNull() ?: min

    Row(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(colors.fieldFill)
            .border(1.dp, colors.border, shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(
            iconRes = top.xiaodl.myprinter.R.drawable.ic_minus,
            contentDescription = "减少份数",
            enabled = current > min,
            onClick = {
                val next = (current - 1).coerceAtLeast(min)
                onValueChange(next.toString())
            },
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = TextPrimary,
            ),
            cursorBrush = SolidColor(colors.brandStart),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier
                .width(56.dp)
                .padding(vertical = 12.dp),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text("1", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = HintColor)
                    }
                    inner()
                }
            },
        )
        StepperButton(
            iconRes = top.xiaodl.myprinter.R.drawable.ic_plus,
            contentDescription = "增加份数",
            enabled = current < max,
            onClick = {
                val next = (current + 1).coerceAtMost(max)
                onValueChange(next.toString())
            },
        )
    }
}

@Composable
private fun StepperButton(
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = myPrinterColors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(46.dp)
            .padding(6.dp)
            .pressScale(interaction)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = false, radius = 20.dp),
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = if (enabled) colors.textSecondary else colors.textTertiary.copy(alpha = 0.45f),
            modifier = Modifier.size(16.dp),
        )
    }
}

/**
 * A custom switch: the track fills with the brand gradient as it turns on, the thumb overshoots
 * with a light spring and the whole control scales slightly while pressed.
 */
@Composable
fun GlowSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = myPrinterColors
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val onProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = Motion.settleSpring(),
        label = "switchProgress",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 23.dp else 3.dp,
        animationSpec = Motion.bouncySpring(),
        label = "switchThumb",
    )
    val glow by animateDpAsState(
        targetValue = if (checked && !colors.isDark) 12.dp else 0.dp,
        animationSpec = tween(Motion.Normal),
        label = "switchGlow",
    )
    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .size(width = 50.dp, height = 30.dp)
            .pressScale(interaction, pressedScale = 0.94f)
            .shadow(
                elevation = glow,
                shape = shape,
                ambientColor = colors.brandStart.copy(alpha = 0.45f),
                spotColor = colors.brandEnd.copy(alpha = 0.45f),
            )
            .clip(shape)
            .background(if (colors.isDark) colors.fieldFill else colors.surfaceMuted)
            .border(1.dp, colors.borderStrong.copy(alpha = 0.9f), shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCheckedChange(!checked)
                },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = onProgress }
                .background(brandGradient()),
        )
        Box(
            Modifier
                .offset { IntOffset(thumbOffset.roundToPx(), 0) }
                .size(24.dp)
                .shadow(
                    elevation = if (colors.isDark) 0.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.22f),
                )
                .background(Color.White, CircleShape),
        )
    }
}

/** A full-width setting row: title, optional supporting line, trailing control. */
@Composable
fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    val colors = myPrinterColors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) },
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) TextPrimary else TextPrimary.copy(alpha = 0.5f),
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = colors.textTertiary,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        GlowSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}
