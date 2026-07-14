package com.linkit.company

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.linkit.company.core.common.AppGraph
import com.linkit.company.data.DataScope
import com.linkit.company.data.core.DATA_STORE_FILE_NAME
import com.linkit.company.data.core.DeviceIdProvider
import com.linkit.company.data.core.createLinkItDataStore
import com.linkit.company.data.core.defaultJson
import com.linkit.company.data.core.defaultKtorConfig
import com.linkit.company.data.datasource.auth.AuthLocalDataSource
import com.linkit.company.data.datasource.auth.AuthLocalDataSourceImpl
import com.linkit.company.data.datasource.auth.AuthRemoteDataSource
import com.linkit.company.data.datasource.auth.AuthRemoteDataSourceImpl
import com.linkit.company.data.repository.AuthRepositoryImpl
import com.linkit.company.domain.repository.AuthRepository
import androidx.lifecycle.ViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import io.ktor.client.HttpClient
import kotlin.reflect.KClass
import io.ktor.client.engine.darwin.Darwin
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIDevice

/**
 * The iOS dependency graph cannot currently be resolved by the compiler plugin.
 * Therefore, we need to define the iOS dependency graph manually.
 * For more details, see: https://github.com/ZacSweers/metro/issues/460
 *
 * 컴파일러 플러그인 이슈로 인해 iOS에선 수동 주입 필요
 * see: https://github.com/DroidKaigi/conference-app-2025/blob/07b46e6585ea6bdafe8a52142d1dd456fddda387/app-shared/src/iosMain/kotlin/io/github/droidkaigi/confsched/IosAppGraph.kt#L93-L97
 *
 * 동기화 필요 파일 목록:
 * @see com.linkit.company.data.DataGraph
 * @see com.linkit.company.data.repository.RepositoryGraph
 * @see com.linkit.company.data.datasource.DataSourceGraph
 * @see com.linkit.company.AndroidDataGraph
 */
@DependencyGraph(
    scope = AppScope::class,
    additionalScopes = [DataScope::class],
    // isExtendable = true
)
interface IosAppGraph : AppGraph {

    @Binds
    val AuthLocalDataSourceImpl.bind: AuthLocalDataSource

    @Binds
    val AuthRemoteDataSourceImpl.bind: AuthRemoteDataSource

    @Binds
    val AuthRepositoryImpl.bind: AuthRepository

    @Provides
    fun provideJson(): Json = defaultJson()

    @OptIn(ExperimentalUuidApi::class)
    @Provides
    fun provideDeviceIdProvider(): DeviceIdProvider {
        return DeviceIdProvider {
            UIDevice.currentDevice.identifierForVendor?.UUIDString
                ?: Uuid.random().toString()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    @SingleIn(DataScope::class)
    @Provides
    fun provideDataStore(): DataStore<Preferences> {
        return createLinkItDataStore {
            val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            requireNotNull(documentDirectory).path + "/$DATA_STORE_FILE_NAME"
        }
    }

    @Provides
    fun provideBaseUrl(): String = "https://linktrip.cloud/api/"

    @Provides
    fun provideHttpClient(
        json: Json,
        authLocalDataSource: AuthLocalDataSource,
    ): HttpClient {
        return HttpClient(Darwin) {
            defaultKtorConfig(json) { authLocalDataSource.getAccessToken() }
        }
    }

    @Provides
    fun provideMetroViewModelFactory(
        viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>,
        assistedFactoryProviders: Map<KClass<out ViewModel>, Provider<ViewModelAssistedFactory>>,
        manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, Provider<ManualViewModelAssistedFactory>>,
    ): MetroViewModelFactory = object : MetroViewModelFactory() {
        override val viewModelProviders = viewModelProviders
        override val assistedFactoryProviders = assistedFactoryProviders
        override val manualAssistedFactoryProviders = manualAssistedFactoryProviders
    }

    @Provides
    fun provideKtorfit(
        httpClient: HttpClient,
        baseUrl: String,
    ): Ktorfit {
        return Ktorfit.Builder()
            .baseUrl(baseUrl)
            .httpClient(httpClient)
            .build()
    }

    @DependencyGraph.Factory
    fun interface Factory {
        fun createIosAppGraph(
        ): IosAppGraph
    }
}

fun createIosAppGraph(): IosAppGraph {
    return createGraphFactory<IosAppGraph.Factory>().createIosAppGraph()
}
