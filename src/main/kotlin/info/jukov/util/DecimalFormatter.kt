package info.jukov.util

import java.math.BigDecimal
import java.text.DecimalFormat

interface DecimalFormatter {
    fun format(decimal: BigDecimal, precision: Int): String

    fun formatAdjustPrecision(value: BigDecimal, maxPrecision: Int = Int.MAX_VALUE): String

    fun calcValuePrecision(value: BigDecimal, maxPrecision: Int = Int.MAX_VALUE): Int
}

class DecimalFormatterImpl: DecimalFormatter {

    private val decimalFormat = DecimalFormat()
    override fun format(decimal: BigDecimal, precision: Int): String {
        return synchronized(this) {
            decimalFormat
                .apply {
                    minimumFractionDigits = 0
                    maximumFractionDigits = precision
                }
                .format(decimal)
        }
    }

    override fun formatAdjustPrecision(
        value: BigDecimal,
        maxPrecision: Int,
    ): String = format(value, calcValuePrecision(value, maxPrecision))

    override fun calcValuePrecision(value: BigDecimal, maxPrecision: Int): Int {
        val digits = integerDigits(value)
        val zeros = decimalFirstZeros(value)
        return minOf(
            maxPrecision,
            precisionByMask(digits, zeros)
        )
    }

    private fun integerDigits(n: BigDecimal?): Int {
        if (n == null) return 0
        return when {
            n.signum() == 0 -> 1
            else -> Integer.max(0, n.precision() - n.scale())
        }
    }

    private fun decimalFirstZeros(n: BigDecimal?): Int {
        if (n == null || n > BigDecimal.ONE || n < -BigDecimal.ONE) return 0

        return n.scale() - n.precision()
    }

    /*
                0.0...0 ####
                #.### ###
               ##.### #
              ###.###
            # ###.##
           ## ###.#
          ### ###
     */
    private fun precisionByMask(digits: Int, zeros: Int) = when (digits) {
        0 -> zeros + 4
        1 -> 6
        2 -> 4
        3 -> 3
        4 -> 2
        5 -> 1
        else -> 0
    }
}