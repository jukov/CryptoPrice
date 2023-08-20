package util

import java.math.BigDecimal

fun Double?.toBigDecimalOrNull(): BigDecimal? {
    this ?: return null
    return try {
        this.toBigDecimal()
    } catch (_: Throwable) {
        null
    }
}

inline fun <T> Iterable<T>.editIf(condition: (T) -> Boolean, transform: (T) -> T): List<T> =
    map {
        if (condition(it)) {
            transform(it)
        } else {
            it
        }
    }