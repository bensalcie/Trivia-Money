package com.bensalcie.app.triviamoney.services

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val okHttp =OkHttpClient.Builder().addInterceptor(logging)
    //retrofit builder
    private val builder =Retrofit.Builder().baseUrl("https://payherokenya.com/u/androidpayment/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttp.build())
    //create retrofit Instance
    private val retrofit = builder.build()
    //we will use this class to create an anonymous inner class function that
    //implements Country service Interface
    fun <T> buildService (serviceType :Class<T>):T{
        return retrofit.create(serviceType)
    }
}