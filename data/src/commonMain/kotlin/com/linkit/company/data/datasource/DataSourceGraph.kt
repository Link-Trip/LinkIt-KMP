package com.linkit.company.data.datasource

import com.linkit.company.data.DataScope
import com.linkit.company.data.datasource.auth.AuthLocalDataSource
import com.linkit.company.data.datasource.auth.AuthLocalDataSourceImpl
import com.linkit.company.data.datasource.auth.AuthRemoteDataSource
import com.linkit.company.data.datasource.auth.AuthRemoteDataSourceImpl
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@ContributesTo(DataScope::class)
internal interface DataSourceGraph {

    @Binds
    val AuthLocalDataSourceImpl.bind: AuthLocalDataSource

    @Binds
    val AuthRemoteDataSourceImpl.bind: AuthRemoteDataSource
}
