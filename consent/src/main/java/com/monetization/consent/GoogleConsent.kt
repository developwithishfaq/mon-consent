package com.monetization.consent

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean


class GoogleConsent(
    private val context: Context
) {
    private val isSdkInitialized = AtomicBoolean(false)

    private var consentInformation = UserMessagingPlatform.getConsentInformation(context)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()


    private val debugSettings = ConsentDebugSettings.Builder(context)
        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
        .addTestDeviceHashedId("F35C4591A4D8063274BE764E6A6FDD96").build()

    private val params: ConsentRequestParameters =
        ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()

    private var listener: ConsentListener? = null

    private fun youCanGo() {
        listener?.canGo()
        listener = null
    }

    fun reset() {
        youCanGo()
    }

    private var alreadyRequested = false

    fun showConsent(
        activity: Activity, canGoListener: ConsentListener? = null
    ) {
        if (alreadyRequested) {
            return
        }
        Log.d("cvv", "showConsent canRequestAds:$canRequestAds ")
        listener = canGoListener
        if (canRequestAds) {
            initAdMob()
            youCanGo()
            return
        }
        alreadyRequested = true
        startHandler()
        consentInformation.requestConsentInfoUpdate(activity, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                activity
            ) {
                alreadyRequested = false
                if (it != null) {
                    Log.d("cvv", "showConsent: Error :${it.message} ")
                }
                if (consentInformation.canRequestAds()) {
                    initAdMob()
                }
                youCanGo()
            }
        }, { e ->

            alreadyRequested = false
            if (canRequestAds) {
                initAdMob()
            }
            Log.d("cvv", "showConsent: error ${e.errorCode} $canRequestAds")
            youCanGo()
        })
        if (consentInformation.canRequestAds()) {
            alreadyRequested = false
            initAdMob()
            youCanGo()
        }
    }

    private fun startHandler() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (listener != null) {
//                youCanGo()
            }
        }, 35_000)
    }

    private fun initAdMob() {
        if (isSdkInitialized.getAndSet(true)) {
            return
        }
    }

}