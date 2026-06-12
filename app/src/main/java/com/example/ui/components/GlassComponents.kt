package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun GlassInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingAction: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.3f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(icon, contentDescription = null, tint = SoftCyan) },
            trailingIcon = trailingAction,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = GlassBackground,
                unfocusedContainerColor = GlassBackground,
                focusedBorderColor = SoftCyan,
                unfocusedBorderColor = GlassBorder,
                cursorColor = SoftCyan
            )
        )
    }
}
