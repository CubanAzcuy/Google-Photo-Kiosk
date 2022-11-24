package gives.robert.kiosk.gphotos.features

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import gives.robert.kiosk.gphotos.features.data.PhotoKioskEffect
import gives.robert.kiosk.gphotos.features.data.PhotoKioskEvents
import gives.robert.kiosk.gphotos.features.data.PhotoKioskState
import gives.robert.kiosk.gphotos.networking.GooglePhotoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class Presenter(localIp: String, val testAlbum: String) {

    private val repo = GooglePhotoRepository(localIp)

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
                processToken(event.googleAccount)
            }
            PhotoKioskEvents.GetPhotos -> {
                buildPhotoList()
            }
        }
    }
    
    private suspend fun processToken(acct: GoogleSignInAccount) {
        val serverAuthCode = acct.serverAuthCode ?: return
        repo.authenticate(serverAuthCode)
        buildPhotoList()
    }

    private suspend fun buildPhotoList() {
        val asdfasdf = repo.fetchPhotos(setOf(testAlbum))

        val asdfffasdf = asdfasdf.associate {
            it.id to Pair("${it.baseUrl}=d", it.mimeType)
        }

        stateFlow.update {
            it.copy(photoUrls = asdfffasdf.values.toList())
        }
    }
}
