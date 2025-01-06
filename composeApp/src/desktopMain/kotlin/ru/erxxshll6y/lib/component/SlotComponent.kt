package ru.erxxshll6y.lib.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.retainedInstance
import com.arkivanov.essenty.lifecycle.subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.erxxshll6y.lib.component.SlotComponent.Model

interface SlotComponent {
    val model: Value<Model>

    data class Model(
        val counter: Int,
        val title: String,
        val body: String
    )
}

class DefaultSlotComponent(
    componentContext: ComponentContext,
    private val tabTitle: String
) : ComponentContext by componentContext, SlotComponent {
    private companion object {
        private const val KEY_STATE = "STATE"
    }

    private val handler = retainedInstance {
        Handler(
            initialState = stateKeeper.consume(key = KEY_STATE, strategy = State.serializer())
                ?: State(title = "(created) $tabTitle"),
            tabTitle = tabTitle
        )
    }

    override val model: Value<Model> = handler.state.map { it.toModel() }

    init {
        lifecycle.subscribe(
            onStart = handler::resume,
            onStop = handler::pause,
        )

        stateKeeper.register(key = KEY_STATE, strategy = State.serializer()) { handler.state.value }
    }

    private fun State.toModel(): Model =
        Model(
            counter = count,
            title = title,
            body = body
        )

    @Serializable
    private data class State(
        val count: Int = 0,
        val title: String,
        val body: String = title
    )

    private class Handler(initialState: State, private val tabTitle: String) : InstanceKeeper.Instance {
        val state: MutableValue<State> = MutableValue(initialState)
        private var job: Job? = null

        fun resume() {
            state.update { it.copy(title = "(active) $tabTitle") }
            job?.cancel()

            job = CoroutineScope(Dispatchers.Default).launch {
                flow {
                    while (true) {
                        emit(Unit)
                        delay(250L)
                    }
                }
                    .collect {
                        state.update { it.copy(count = it.count + 1, body = it.title.repeat(it.count)) }
                    }
            }
        }

        fun pause() {
            state.update { it.copy(title = "(paused) $tabTitle") }
            job?.cancel()
            job = null
        }

        override fun onDestroy() {
            pause()
        }
    }
}