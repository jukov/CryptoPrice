package ui.model

sealed class UiEvent {

    object MaxLimitReached: UiEvent()

    class TickerAlreadyAdded(val tickerName: String): UiEvent()
}