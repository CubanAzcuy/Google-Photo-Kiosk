package gives.robert.kiosk.gphotos.features

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import gives.robert.kiosk.gphotos.features.data.PhotoKioskEffect
import gives.robert.kiosk.gphotos.features.data.PhotoKioskEvents
import gives.robert.kiosk.gphotos.features.data.PhotoKioskState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class Presenter {

    private val eventFlowScope = CoroutineScope(Dispatchers.IO)
    private val launchScope = CoroutineScope(Dispatchers.IO)

    private val eventFlow = MutableSharedFlow<PhotoKioskEvents>()

    val stateFlow = MutableStateFlow(PhotoKioskState())
    val effectFlow = MutableSharedFlow<PhotoKioskEffect>(0)

    init {
        eventFlow.onEach {
            handleEvent(it)
        }.produceIn(eventFlowScope)
    }

    fun processEvent(event: PhotoKioskEvents)  {
        launchScope.launch {
            eventFlow.emit(event)
        }
    }

    private suspend fun handleEvent(event: PhotoKioskEvents) {
        when(event) {
            is PhotoKioskEvents.TokenFetched -> {
                processToken(event.context, event.googleAccount)
            }
        }
    }
    
    private fun processToken(context: Context, acct: GoogleSignInAccount) {
        val serverAuthCode = acct.serverAuthCode ?: return
        val asfasdf = ""
    }
}
