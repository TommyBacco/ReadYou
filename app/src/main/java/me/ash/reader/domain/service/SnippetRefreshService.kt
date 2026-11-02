package me.ash.reader.domain.service

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.infrastructure.di.IODispatcher
import me.ash.reader.infrastructure.html.Readability
import me.ash.reader.ui.ext.dataStore

class SnippetRefreshService @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val articleDao: ArticleDao,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun refreshSnippetWithCaptions() = withContext(ioDispatcher) {
        val key = booleanPreferencesKey("snippetWithCaptionsRefreshed")
        if (context.dataStore.data.first()[key] == true) return@withContext
        var afterId = ""
        var updated = 0
        while (true) {
            val candidates =
                articleDao.queryCaptionCandidates(afterId = afterId, limit = PAGE_SIZE)
            if (candidates.isEmpty()) break
            candidates.forEach { article ->
                val snippet =
                    Readability.parseToText(article.rawDescription, article.link).take(280)
                if (snippet != article.shortDescription) {
                    articleDao.update(article.copy(shortDescription = snippet))
                    updated++
                }
            }
            afterId = candidates.last().id
        }
        context.dataStore.edit { preferences -> preferences[key] = true }
        Log.i("RLog", "refreshSnippetWithCaptions: $updated snippets updated")
    }

    companion object {
        private const val PAGE_SIZE = 500
    }
}
