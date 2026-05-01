package com.example.yuntushaomiaojia.data.conversion

import java.math.BigInteger
import java.util.Locale

class ConversionRepository {

    fun convertCurrencyText(amountText: String, fromCurrency: String, toCurrency: String): String {
        val amount = amountText.trim().toDouble()
        val fromRate = currencyRate(fromCurrency)
        val toRate = currencyRate(toCurrency)
        val result = amount * fromRate / toRate
        return String.format(Locale.CHINA, "%.2f %s = %.2f %s", amount, fromCurrency, result, toCurrency)
    }

    fun convertBaseText(numberText: String, selectedBase: Int): String {
        val value = BigInteger(numberText.trim(), selectedBase)
        return "二进制：${value.toString(2)}" +
            "\n八进制：${value.toString(8)}" +
            "\n十进制：${value.toString(10)}" +
            "\n十六进制：${value.toString(16).uppercase(Locale.ROOT)}"
    }

    private fun currencyRate(currency: String): Double {
        return when {
            currency.startsWith("USD") -> 7.25
            currency.startsWith("EUR") -> 7.85
            currency.startsWith("JPY") -> 0.050
            currency.startsWith("KRW") -> 0.0053
            currency.startsWith("GBP") -> 9.12
            else -> 1.0
        }
    }
}
