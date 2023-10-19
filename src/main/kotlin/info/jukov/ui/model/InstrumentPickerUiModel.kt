
package info.jukov.ui.model

data class InstrumentPickerUiModel(
    val name: String,
    val baseAsset: String,
    val quoteAsset: String,
    val symbol: String,
    val precision: Int
) {
    override fun toString(): String = name
}