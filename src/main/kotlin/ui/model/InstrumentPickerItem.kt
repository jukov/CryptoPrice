
package ui.model

data class InstrumentPickerItem(
    val name: String,
    val baseCurrency: String,
    val quoteCurrency: String,
    val symbol: String,
    val precision: Int
) {
    override fun toString(): String = name
}