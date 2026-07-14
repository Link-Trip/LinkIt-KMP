package com.linkit.company.data

import com.linkit.company.data.repository.AuthRepositoryImpl
import com.linkit.company.domain.repository.AuthRepository
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@ContributesTo(DataScope::class)
internal interface RepositoryGraph {

    @Binds
    val AuthRepositoryImpl.bind: AuthRepository
}