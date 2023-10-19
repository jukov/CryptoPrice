package info.jukov.domain

import info.jukov.domain.model.UserInstrument

interface SettingsRepository {

    suspend fun getUserInstruments(): List<UserInstrument>?

    suspend fun setUserInstruments(tickers: List<UserInstrument>)
}