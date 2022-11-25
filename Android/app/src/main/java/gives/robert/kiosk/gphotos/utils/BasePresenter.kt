package gives.robert.kiosk.gphotos.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


interface Presenter<Event, State, Effect> {
    val stateFlow: StateFlow<State>
    val effectFlow: SharedFlow<Effect>
    fun processEvent(event: Event)
}

abstract class BasePresenter<Event, State, Effect> : Presenter<Event, State, Effect> {
    private val eventFlowScope = CoroutineScope(Dispatchers.IO)
    private val launchScope = CoroutineScope(Dispatchers.IO)

    private val eventFlow = MutableSharedFlow<Event>()

    abstract val baseState: State
    override val stateFlow = MutableStateFlow(baseState)
    override val effectFlow = MutableSharedFlow<Effect>(0)

    init {
        eventFlow.onEach {
            handleEvent(it)
        }.produceIn(eventFlowScope)
    }

    override fun processEvent(event: Event) {
        launchScope.launch {
            eventFlow.emit(event)
        }
    }

    protected abstract suspend fun handleEvent(event: Event)
}
