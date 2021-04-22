package com.bensalcie.app.triviamoney

import com.google.gson.annotations.SerializedName


data class C2BValidateResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("payment")
    val payment: List<Payment>,
    @SerializedName("status")
    val status: String
) {
    data class Payment(
        @SerializedName("CheckoutRequestID")
        val checkoutRequestID: String,
        @SerializedName("MerchantRequestID")
        val merchantRequestID: String,
        @SerializedName("ResultCode")
        val resultCode: String,
        @SerializedName("ResultDesc")
        val resultDesc: String
    )
}