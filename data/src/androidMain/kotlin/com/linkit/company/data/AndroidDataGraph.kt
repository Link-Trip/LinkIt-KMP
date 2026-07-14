package com.linkit.company.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.linkit.company.data.core.DATA_STORE_FILE_NAME
import com.linkit.company.data.core.createLinkItDataStore
import com.linkit.company.data.core.defaultKtorConfig
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json

@ContributesTo(DataScope::class)
interface AndroidDataGraph {
    @Provides
    fun provideHttpClient(json: Json): HttpClient {
        return HttpClient(engineFactory = OkHttp) {
            defaultKtorConfig(json)
        }
    }

    @SingleIn(DataScope::class)
    @Provides
    fun provideDataStore(context: Context): DataStore<Preferences> {
        return createLinkItDataStore {
            context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
        }
    }
}
