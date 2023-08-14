import data.TickerRepositoryImpl
import ui.TickerScreen
import ui.TickerViewModel

fun main(args: Array<String>) {
    TickerScreen(
        TickerViewModel(
            TickerRepositoryImpl()
        )
    )
}