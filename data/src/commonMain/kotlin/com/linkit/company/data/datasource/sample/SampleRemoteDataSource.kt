package com.linkit.company.data.datasource.sample

interface SampleRemoteDataSource {
    suspend fun getSample(): String
}
