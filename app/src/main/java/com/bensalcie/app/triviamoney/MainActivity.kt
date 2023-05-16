package com.bensalcie.app.triviamoney

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import bensalcie.payhero.mpesa.mpesa.model.AccessToken
import bensalcie.payhero.mpesa.mpesa.model.STKPush
import bensalcie.payhero.mpesa.mpesa.model.STKResponse
import bensalcie.payhero.mpesa.mpesa.services.DarajaApiClient
import bensalcie.payhero.mpesa.mpesa.services.Environment
import bensalcie.payhero.mpesa.mpesa.services.Utils
import com.bensalcie.app.triviamoney.services.AppService
import com.bensalcie.app.triviamoney.services.ServiceBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private var mApiClient: DarajaApiClient? = null //Intitialization before on create

    private lateinit var btnPay:MaterialButton
    private lateinit var etPhone:TextInputEditText
    private lateinit var etAmount:TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mApiClient = DarajaApiClient(
            "2CFtEBcX3H87VMiTMpsOAhByMwsm0JeJ",
            "VXJXRHNHVrBtnD87",
            Environment.SANDBOX
        )
        //use Environment.PRODUCTION when on production
        //get consumerkey and secret from https://developer.safaricom.co.ke/user/me/apps
        mApiClient!!.setIsDebug(true) //Set True to enable logging, false to disable.
        getAccessToken()

        btnPay = findViewById(R.id.btnPay)
        etPhone = findViewById(R.id.etPhone)
        etAmount = findViewById(R.id.etAmount)

        btnPay.setOnClickListener {
            val phone = etPhone.text.toString()
            val amount = etAmount.text.toString()

            if (TextUtils.isEmpty(phone)){
                Snackbar.make(it,"Please enter phone Number",Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }


            if (TextUtils.isEmpty(amount)){
                Snackbar.make(it,"Please enter Amount",Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            //process payment
            btnPay.text = "Processing..."
            performSTKPush(amount,phone,it,btnPay)




        }
    }

    private fun getAccessToken() {
        mApiClient!!.setGetAccessToken(true)
        mApiClient!!.mpesaService()!!.getAccessToken().enqueue(object : Callback<AccessToken> {
            override fun onResponse(call: Call<AccessToken?>, response: Response<AccessToken>) {
                if (response.isSuccessful) {
                    mApiClient!!.setAuthToken(response.body()?.accessToken)
                }
            }
            override fun onFailure(call: Call<AccessToken?>, t: Throwable) {}
        })
    }

    private fun performSTKPush(amount: String, phone_number: String, view: View, btnPay: MaterialButton) {
        //Handle progresss here
        //credentials here are test credentials
        val timestamp = Utils.getTimestamp()
        val stkPush = STKPush(
            getString(R.string.app_name),
            amount,
            "174379",
            "http://somedormain.com/payment/etc/c2bcallback.php",
            Utils.sanitizePhoneNumber(phone_number)!!,
            "174379",
            Utils.getPassword(
                "174379",
                "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919", timestamp!!
            )!!,
            Utils.sanitizePhoneNumber(phone_number)!!,
            timestamp,
            "Payment for ${getString(R.string.app_name)}",
            Environment.TransactionType.CustomerPayBillOnline
        )
        mApiClient!!.setGetAccessToken(false)
        mApiClient!!.mpesaService()!!.sendPush(stkPush).enqueue(object : Callback<STKResponse> {
            override fun onResponse(call: Call<STKResponse>, response: Response<STKResponse>) {
                try {
                    if (response.isSuccessful) {

                        //handle response here
                        val res = response.body()
                        val checkerID = res!!.checkoutRequestID
                        Handler(Looper.getMainLooper()).postDelayed({
                            btnPay.text = "Validating Payments..."
                            //Do something after 100ms
                            val pd = ProgressDialog(this@MainActivity)
                            pd.setTitle("Validating Payments Please Wait")
                            pd.setMessage("Please seat back as we validate your payments")
                            pd.setCanceledOnTouchOutside(false)
                            validatePayment(checkerID,view,pd,btnPay)

                        }, 8500)


                        //response contains CheckoutRequestID,CustomerMessage,MerchantRequestID,ResponseCode,ResponseDescription
                    } else {
                        btnPay.text ="Try Again"
                        Snackbar.make(view,response.body().toString(),Snackbar.LENGTH_LONG).show()
                        //Timber.e("Response %s", response.errorBody()!!.string())
                    }
                } catch (e: Exception) {
                    btnPay.text ="Try Again"
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<STKResponse>, t: Throwable) {
                //mProgressDialog!!.dismiss()
                //Timber.e(t)
                btnPay.text ="Try Again"
                Snackbar.make(view,t.message.toString(),Snackbar.LENGTH_LONG).show()

            }
        })
    }

    private fun validatePayment(checkerID: String, view: View, pd: ProgressDialog, btnPay: MaterialButton) {
        pd.show()
        val serviceBuilder  = ServiceBuilder.buildService(AppService::class.java)
        val validateBody = C2BValidateBody(checkerID)
        Log.d("Summary", "Upstream Response:$validateBody")

        val resultsQueue = serviceBuilder.validatePayment(validateBody)
        resultsQueue.enqueue(object : Callback<C2BValidateResponse> {
            override fun onResponse(
                call: Call<C2BValidateResponse>,
                response: Response<C2BValidateResponse>
            ) {
                if (response.isSuccessful) {
                    pd.dismiss()
                    val myres = response.body()

                    if (myres!!.status =="0"){
                        if (myres.payment[0].resultCode == "0") {
                            btnPay.text ="Success!!!"

                            Snackbar.make(
                                    view,
                                    "Payment Successful: ${myres.payment[0].resultDesc}",
                                    Snackbar.LENGTH_LONG
                            ).show()
                            Toast.makeText(applicationContext, "Payment  Succesful", Toast.LENGTH_SHORT).show()

                        } else {
                            btnPay.text ="Try Again"

                            Snackbar.make(
                                    view,
                                    "Payment Failed: ${myres.payment[0].resultDesc}",
                                    Snackbar.LENGTH_LONG
                            ).show()
                            Toast.makeText(applicationContext, "Payment Not Succesful", Toast.LENGTH_SHORT).show()

                        }
                    }else{
                        btnPay.text ="Try Again"
                        Snackbar.make(
                                view,
                                "We could'nt complete your payments",
                                Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<C2BValidateResponse>, t: Throwable) {
                pd.dismiss()
                btnPay.text ="Try Again"

                Log.d("Transactions", "Downstream Response error  :$t")

            }
        })

    }



}
