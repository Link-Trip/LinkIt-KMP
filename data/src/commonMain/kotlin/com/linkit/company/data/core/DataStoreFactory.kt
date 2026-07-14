package com.linkit.company.data.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

const val DATA_STORE_FILE_NAME = "linkit.preferences_pb"

/**
 * 플랫폼별 파일 경로만 받아 DataStore 인스턴스를 생성한다.
 *
 * DataStore는 동일 파일에 두 개 이상의 인스턴스가 생기면 런타임 예외가 발생하므로,
 * 이 함수를 제공하는 @Provides에는 반드시 @SingleIn(DataScope::class)을 지정할 것.
 */
fun createLinkItDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
    )
