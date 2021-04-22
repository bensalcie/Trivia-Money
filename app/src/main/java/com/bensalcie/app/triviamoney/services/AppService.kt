package com.bensalcie.app.triviamoney.services



import com.bensalcie.app.triviamoney.C2BValidateBody
import com.bensalcie.app.triviamoney.C2BValidateResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
interface AppService {
    @POST("validate.php")
    fun validatePayment (@Body c2BValidateBody: C2BValidateBody): Call<C2BValidateResponse>



}