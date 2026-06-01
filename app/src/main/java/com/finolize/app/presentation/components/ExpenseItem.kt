package com.finolize.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finolize.app.core.utils.toFormattedDate
import com.finolize.app.domain.model.CategoryList

@Composable
fun ExpenseItem(
    categoryName: String,
    amount: String,
    timestamp: Long,
    description: String,
    modifier: Modifier = Modifier
) {
    val category = CategoryList.getCategoryByName(categoryName)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка категории
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(category.icon, contentDescription = null, tint = category.color)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // 1. Категория
                Text(
                    text = category.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                // 2. Описание (показываем, только если оно не пустое)
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        color = Color.Gray.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        maxLines = 1, // Ограничение в одну строку
                        overflow = TextOverflow.Ellipsis // Три точки в конце
                    )
                }

                // 3. Дата
                Text(
                    text = timestamp.toFormattedDate(),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // Сумма
            Text(
                text = "-$amount",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}