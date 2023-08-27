package domain

import domain.model.UserInstrument

interface SettingsRepository {

    suspend fun getUserInstruments(): List<UserInstrument>?

    suspend fun setUserInstruments(tickers: List<UserInstrument>)
}