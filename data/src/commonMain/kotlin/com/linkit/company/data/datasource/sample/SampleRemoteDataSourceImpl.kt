package com.linkit.company.data.datasource.sample

import com.linkit.company.data.api.SampleApi
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.Inject

@Inject
class SampleRemoteDataSourceImpl(
    ktorfit: Ktorfit
) : SampleRemoteDataSource {
    /**
     * Auto compile by compiler plugin
     * see : https://github.com/Foso/Ktorfit/blob/399dfb3f9bac4bc67aaf98b7dfaa65febd2ad6f5/ktorfit-lib-core/src/commonMain/kotlin/de/jensklingenberg/ktorfit/Ktorfit.kt#L90
     */
//    private val api = ktorfit.create<SampleApi>()

    override suspend fun getSample(): String {
//        return api.getSample()
        return ""
    }
}
