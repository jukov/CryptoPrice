
package ui.model

data class InstrumentPickerItem(
    val name: String,
    val symbol: String,
) {
    override fun toString(): String = name
}