package data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeInfoDto(
//    @SerialName("exchangeFilters") val exchangeFilters: List<Any?>?,
//    @SerialName("rateLimits") val rateLimits: List<RateLimit?>?,
    @SerialName("serverTime") val serverTime: Long?,
    @SerialName("symbols") val symbols: List<Symbol?>?,
    @SerialName("timezone") val timezone: String?
) {
//    @Serializable
//    data class RateLimit(
//        @SerialName("interval") val interval: String?,
//        @SerialName("intervalNum") val intervalNum: Int?,
//        @SerialName("limit") val limit: Int?,
//        @SerialName("rateLimitType") val rateLimitType: String?
//    )

    @Serializable
    data class Symbol(
//        @SerialName("allowTrailingStop") val allowTrailingStop: Boolean?,
//        @SerialName("allowedSelfTradePreventionModes") val allowedSelfTradePreventionModes: List<String?>?,
        @SerialName("baseAsset") val baseAsset: String?,
        @SerialName("baseAssetPrecision") val baseAssetPrecision: Int?,
//        @SerialName("baseCommissionPrecision") val baseCommissionPrecision: Int?,
//        @SerialName("cancelReplaceAllowed") val cancelReplaceAllowed: Boolean?,
//        @SerialName("defaultSelfTradePreventionMode") val defaultSelfTradePreventionMode: String?,
//        @SerialName("filters") val filters: List<Filter?>?,
//        @SerialName("icebergAllowed") val icebergAllowed: Boolean?,
//        @SerialName("isMarginTradingAllowed") val isMarginTradingAllowed: Boolean?,
//        @SerialName("isSpotTradingAllowed") val isSpotTradingAllowed: Boolean?,
//        @SerialName("ocoAllowed") val ocoAllowed: Boolean?,
//        @SerialName("orderTypes") val orderTypes: List<String?>?,
//        @SerialName("permissions") val permissions: List<String?>?,
        @SerialName("quoteAsset") val quoteAsset: String?,
        @SerialName("quoteAssetPrecision") val quoteAssetPrecision: Int?,
//        @SerialName("quoteCommissionPrecision") val quoteCommissionPrecision: Int?,
//        @SerialName("quoteOrderQtyMarketAllowed") val quoteOrderQtyMarketAllowed: Boolean?,
//        @SerialName("quotePrecision") val quotePrecision: Int?,
        @SerialName("status") val status: String?,
        @SerialName("symbol") val symbol: String?
    ) {
//        @Serializable
//        data class Filter(
//            @SerialName("applyMaxToMarket") val applyMaxToMarket: Boolean?,
//            @SerialName("applyMinToMarket") val applyMinToMarket: Boolean?,
//            @SerialName("askMultiplierDown") val askMultiplierDown: String?,
//            @SerialName("askMultiplierUp") val askMultiplierUp: String?,
//            @SerialName("avgPriceMins") val avgPriceMins: Int?,
//            @SerialName("bidMultiplierDown") val bidMultiplierDown: String?,
//            @SerialName("bidMultiplierUp") val bidMultiplierUp: String?,
//            @SerialName("filterType") val filterType: String?,
//            @SerialName("limit") val limit: Int?,
//            @SerialName("maxNotional") val maxNotional: String?,
//            @SerialName("maxNumAlgoOrders") val maxNumAlgoOrders: Int?,
//            @SerialName("maxNumOrders") val maxNumOrders: Int?,
//            @SerialName("maxPosition") val maxPosition: String?,
//            @SerialName("maxPrice") val maxPrice: String?,
//            @SerialName("maxQty") val maxQty: String?,
//            @SerialName("maxTrailingAboveDelta") val maxTrailingAboveDelta: Int?,
//            @SerialName("maxTrailingBelowDelta") val maxTrailingBelowDelta: Int?,
//            @SerialName("minNotional") val minNotional: String?,
//            @SerialName("minPrice") val minPrice: String?,
//            @SerialName("minQty") val minQty: String?,
//            @SerialName("minTrailingAboveDelta") val minTrailingAboveDelta: Int?,
//            @SerialName("minTrailingBelowDelta") val minTrailingBelowDelta: Int?,
//            @SerialName("stepSize") val stepSize: String?,
//            @SerialName("tickSize") val tickSize: String?
//        )
    }
}