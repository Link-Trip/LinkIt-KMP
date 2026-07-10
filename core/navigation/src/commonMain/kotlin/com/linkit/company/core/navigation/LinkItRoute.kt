package com.linkit.company.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val linkItSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(LinkItNavKey.Map::class, LinkItNavKey.Map.serializer())
        subclass(LinkItNavKey.Storage::class, LinkItNavKey.Storage.serializer())
        subclass(LinkItNavKey.StorageSearch::class, LinkItNavKey.StorageSearch.serializer())
        subclass(LinkItNavKey.StorageSearchResult::class, LinkItNavKey.StorageSearchResult.serializer())
        subclass(LinkItNavKey.StorageAddSaved::class, LinkItNavKey.StorageAddSaved.serializer())
        subclass(LinkItNavKey.Explore::class, LinkItNavKey.Explore.serializer())
        subclass(LinkItNavKey.ScheduleEdit::class, LinkItNavKey.ScheduleEdit.serializer())
    }
}

val LinkItSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = linkItSerializersModule
}

interface LinkItNavKey : NavKey {
    @Serializable
    data object Map : LinkItNavKey

    @Serializable
    data object Explore : LinkItNavKey

    @Serializable
    data object Storage : LinkItNavKey

    @Serializable
    data object StorageSearch : LinkItNavKey

    @Serializable
    data object StorageSearchResult : LinkItNavKey

    @Serializable
    data object StorageAddSaved : LinkItNavKey

    @Serializable
    data object ScheduleEdit : LinkItNavKey
}
