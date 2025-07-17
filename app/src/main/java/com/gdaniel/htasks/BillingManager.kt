package com.gdaniel.htasks

import android.app.Activity
import android.content.Context

object BillingManager {
    // Product IDs
    private const val PRODUCT_PROMPTS = "com.htasks.prompts30"

    // Stub: In a real app, use Google Play BillingClient
    fun purchasePrompts(activity: Activity, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // TODO: Implement real billing flow
        // For now, simulate success
        onSuccess()
    }

    fun isProductPurchased(context: Context, productId: String): Boolean {
        // TODO: Check real purchase state
        return false
    }
} 