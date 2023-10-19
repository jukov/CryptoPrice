package ui.model

sealed class UiEvent {

    object MaxLimitReached: UiEvent()

    class TickerAlreadyAdded(val tickerName: String): UiEvent()

    object InstrumentListError: UiEvent()

    object TickerObserveError: UiEvent()
}