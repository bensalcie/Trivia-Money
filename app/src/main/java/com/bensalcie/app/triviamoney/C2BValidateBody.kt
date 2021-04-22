package com.bensalcie.app.triviamoney


import com.google.gson.annotations.SerializedName

data class C2BValidateBody(
    @SerializedName("CheckoutRequestID")
    val checkoutRequestID: String
)