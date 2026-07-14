package com.linkit.company.data

import android.content.Context
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.linkit.company.data.core.DATA_STORE_FILE_NAME
import com.linkit.company.data.core.DeviceIdProvider
import com.linkit.company.data.core.createLinkItDataStore
import com.linkit.company.data.core.defaultKtorConfig
import com.linkit.company.data.datasource.auth.AuthLocalDataSource
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.json.Json

@ContributesTo(DataScope::class)
interface AndroidDataGraph {
    @Provides
    fun provideHttpClient(
        json: Json,
        authLocalDataSource: AuthLocalDataSource,
    ): HttpClient {
        return HttpClient(engineFactory = OkHttp) {
            defaultKtorConfig(json) { authLocalDataSource.getAccessToken() }
        }
    }

    @SingleIn(DataScope::class)
    @Provides
    fun provideDataStore(context: Context): DataStore<Preferences> {
        return createLinkItDataStore {
            context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Provides
    fun provideDeviceIdProvider(context: Context): DeviceIdProvider {
        return DeviceIdProvider {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: Uuid.random().toString()
        }
    }
}
