package ru.erxxshll6y.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import ru.erxxshll6y.app.component.tab.Config
import ru.erxxshll6y.app.component.tab.SlotContent
import ru.erxxshll6y.app.component.tab.TabContent
import ru.erxxshll6y.app.utils.readSerializableContainer
import ru.erxxshll6y.app.utils.runOnUiThread
import ru.erxxshll6y.lib.component.DefaultSlotComponent
import ru.erxxshll6y.lib.component.DefaultTabNavigationComponent
import ru.erxxshll6y.lib.tab.TabNavigationContent
import java.io.File

/**
 * Properties
 */
val spaceBetweenTabsAndContent = 4.dp

/**
 * Computations
 */
val rootStyle = Modifier.fillMaxSize().padding(4.dp)
val spacerStyle = Modifier.height(spaceBetweenTabsAndContent)

private const val SAVED_STATE_FILE_NAME = "saved_state.dat"

fun main() {
    val lifecycle = LifecycleRegistry()
    val stateKeeper = StateKeeperDispatcher(
        savedState = File(SAVED_STATE_FILE_NAME).readSerializableContainer()
    )

    val tabNavigationComponent = runOnUiThread {
        DefaultTabNavigationComponent(
            componentContext = DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper
            ),
            configurations = listOf(Config("readme.txt"), Config("build.gradle.kts")),
            serializer = Config.serializer()
        ) { componentContext, config ->
            DefaultSlotComponent(componentContext, config.filename)
        }
    }

    application {
        val windowState = rememberWindowState()

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "app",
        ) {
            // FIXME: важная деталь, без нее ничего не работает (состояния)
            LifecycleController(
                lifecycleRegistry = lifecycle,
                windowState = windowState,
                windowInfo = LocalWindowInfo.current,
            )

            TabNavigationContent(
                navigationComponent = tabNavigationComponent,
                tabContent = { index, slotComponent, modifier, isSelected, isDragging, onClose ->
                    TabContent(
                        slotComponent = slotComponent,
                        modifier = modifier,
                        isSelected = isSelected,
                        isDragging = isDragging,
                        onClose = onClose,
                        onSelect = { tabNavigationComponent.onSelect(index) }
                    )
                },
                containerContent = { innerTabs, slotComponent ->
                    Column(rootStyle) {
                        innerTabs(Modifier.fillMaxWidth())
                        Spacer(spacerStyle)
                        LazyColumn(Modifier.fillMaxSize()) {
                            item {
                                SlotContent(
                                    component = slotComponent,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}
