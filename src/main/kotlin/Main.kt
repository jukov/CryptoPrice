import data.TickerRepositoryImpl
import ui.MainScreen
import ui.TickerViewModel

fun main(args: Array<String>) {
    MainScreen(
        TickerViewModel(
            TickerRepositoryImpl()
        )
    )
}