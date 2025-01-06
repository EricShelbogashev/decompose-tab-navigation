package ru.erxxshll6y.app.component.tab;

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.erxxshll6y.lib.component.SlotComponent

/**
 * Properties
 */
val slotShape = RoundedCornerShape(8.dp)

/**
 * Computations
 */
val slotStyle = Modifier

@Composable
fun SlotContent(component: SlotComponent?, modifier: Modifier = Modifier) {
    if (component == null) {
        EmptySlotImpl(modifier = modifier)
    } else {
        SlotImpl(
            component = component,
            modifier = modifier
        )
    }
}

@Composable
private fun SlotImpl(component: SlotComponent, modifier: Modifier) {
    val state = component.model.subscribeAsState()

    Card(modifier = modifier.then(slotStyle), shape = slotShape) {
        Column {
            Text("${state.value.counter}")
            Text(state.value.body)
        }
    }
}

@Composable
private fun EmptySlotImpl(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("Empty")
    }
}