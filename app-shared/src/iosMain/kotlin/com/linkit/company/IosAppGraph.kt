package com.linkit.company

import com.linkit.company.core.common.AppGraph
import com.linkit.company.data.DataScope
import com.linkit.company.data.core.defaultJson
import com.linkit.company.data.core.defaultKtorConfig
import androidx.lifecycle.ViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import io.ktor.client.HttpClient
import kotlin.reflect.KClass
import io.ktor.client.engine.darwin.Darwin
import kotlinx.serialization.json.Json

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

    @Provides
    fun provideJson(): Json = defaultJson()

    @Provides
    fun provideBaseUrl(): String = ""

    @Provides
    fun provideHttpClient(json: Json): HttpClient {
        return HttpClient(Darwin) {
            defaultKtorConfig(json)
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
