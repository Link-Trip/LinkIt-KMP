package com.linkit.company.data.repository

import com.linkit.company.data.datasource.sample.SampleRemoteDataSource
import com.linkit.company.domain.repository.SampleRepository
import dev.zacsweers.metro.Inject

@Inject
class SampleRepositoryImpl(
    private val sampleRemoteDataSource: SampleRemoteDataSource,
) : SampleRepository {
    override suspend fun getSample(): String {
        return sampleRemoteDataSource.getSample()
    }
}
