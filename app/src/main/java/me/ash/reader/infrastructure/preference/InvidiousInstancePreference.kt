package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import me.ash.reader.ui.ext.dataStore

object InvidiousInstancePreference {

    private val key = stringPreferencesKey("invidiousInstanceHost")

    suspend fun get(context: Context): String? =
        context.dataStore.data.first()[key]?.takeIf { it.isNotBlank() }

    suspend fun put(context: Context, host: String) {
        context.dataStore.edit { preferences -> preferences[key] = host }
    }
}
