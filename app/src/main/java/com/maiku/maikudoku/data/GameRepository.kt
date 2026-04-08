package com.maiku.maikudoku.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.maiku.maikudoku.domain.model.GameSaveState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "game_state")

class GameRepository(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson = Gson()
) {

    fun loadGame(): Flow<GameSaveState?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[GAME_STATE_KEY] ?: return@map null
                runCatching { gson.fromJson(json, GameSaveState::class.java) }.getOrNull()
            }
    }

    suspend fun saveGame(state: GameSaveState) {
        val payload = gson.toJson(state)
        dataStore.edit { preferences ->
            preferences[GAME_STATE_KEY] = payload
        }
    }

    suspend fun clearGame() {
        dataStore.edit { preferences ->
            preferences.remove(GAME_STATE_KEY)
        }
    }

    companion object {
        private val GAME_STATE_KEY = stringPreferencesKey("game_state_json")

        fun from(context: Context): GameRepository {
            return GameRepository(context.applicationContext.gameDataStore)
        }
    }
}

