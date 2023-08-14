import java.math.BigDecimal

val SLLeft: String = "West"
val SLRight: String = "East"
val SLTop: String = "North"
val SLBottom: String = "South"

fun Double?.toBigDecimalOrNull(): BigDecimal? {
    this ?: return null
    return try {
        this.toBigDecimal()
    } catch (_: Throwable) {
        null
    }
}