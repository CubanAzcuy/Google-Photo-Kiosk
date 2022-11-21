package gives.robert.kiosk.gphotos.ui.theme

/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.Credentials
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.UserCredentials
import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.PhotosLibrarySettings
import java.io.IOException

/**
 * A factory class that helps initialize a [PhotosLibraryClient] instance.
 */
object PhotosLibraryClientFactory {

    fun createClient(token: GoogleSignInAccount): PhotosLibraryClient? {

        return try {
            val settings = PhotosLibrarySettings.newBuilder()
                .setCredentialsProvider(
                    FixedCredentialsProvider.create(getUserCredentials(token.serverAuthCode!!))
                )
                .build()
            PhotosLibraryClient.initialize(settings!!)

        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getUserCredentials(token: String): Credentials {
        val a = AccessToken(token, null)
        return UserCredentials.newBuilder()
            .setClientId("clientId")
            .setClientSecret("clientSecret")
            .setAccessToken(a)
            .build()
    }
}
