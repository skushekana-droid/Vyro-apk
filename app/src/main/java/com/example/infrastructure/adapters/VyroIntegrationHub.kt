package com.example.infrastructure.adapters

import com.example.infrastructure.ai.AiProviderType
import com.example.infrastructure.ai.VyroAiEngineImpl
import com.example.infrastructure.auth.FirebaseAuthAdapter
import com.example.infrastructure.auth.VyroAuthService
import com.example.infrastructure.auth.VyroNativeAuthAdapter
import com.example.infrastructure.cdn.CloudflareCdnAdapter
import com.example.infrastructure.cdn.MediaDeliveryService
import com.example.infrastructure.cdn.VyroEdgeNetworkAdapter
import com.example.infrastructure.payment.PaymentAdapter
import com.example.infrastructure.payment.StripePaymentAdapter
import com.example.infrastructure.payment.VyroPayNativeAdapter
import com.example.infrastructure.payment.VyroPaymentEngine
import com.example.infrastructure.storage.StorageService
import com.example.infrastructure.storage.VyroLocalStorageAdapter
import com.example.infrastructure.storage.VyroS3StorageAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object VyroIntegrationHub {
    // Auth Adapter
    private val nativeAuth = VyroNativeAuthAdapter()
    private val firebaseAuth = FirebaseAuthAdapter()
    private val _currentAuthService = MutableStateFlow<VyroAuthService>(nativeAuth)
    val currentAuthService: StateFlow<VyroAuthService> = _currentAuthService.asStateFlow()

    // Storage Adapter
    private val s3Storage = VyroS3StorageAdapter()
    private val localStorage = VyroLocalStorageAdapter()
    private val _currentStorageService = MutableStateFlow<StorageService>(s3Storage)
    val currentStorageService: StateFlow<StorageService> = _currentStorageService.asStateFlow()

    // AI Engine
    val aiEngine = VyroAiEngineImpl()

    // Payment Engine
    private val vyroPay = VyroPayNativeAdapter()
    private val stripePay = StripePaymentAdapter()
    private val _currentPaymentAdapter = MutableStateFlow<PaymentAdapter>(vyroPay)
    val currentPaymentAdapter: StateFlow<PaymentAdapter> = _currentPaymentAdapter.asStateFlow()
    val paymentEngine = VyroPaymentEngine(vyroPay)

    // CDN & Media Delivery
    private val vyroEdge = VyroEdgeNetworkAdapter()
    private val cloudflareCdn = CloudflareCdnAdapter()
    private val _currentCdnService = MutableStateFlow<MediaDeliveryService>(vyroEdge)
    val currentCdnService: StateFlow<MediaDeliveryService> = _currentCdnService.asStateFlow()

    // Switcher Functions
    fun setUseNativeAuth(useNative: Boolean) {
        _currentAuthService.value = if (useNative) nativeAuth else firebaseAuth
    }

    fun setUseS3Storage(useS3: Boolean) {
        _currentStorageService.value = if (useS3) s3Storage else localStorage
    }

    fun setAiProvider(provider: AiProviderType) {
        aiEngine.switchProvider(provider)
    }

    fun setUseNativePayment(useNative: Boolean) {
        _currentPaymentAdapter.value = if (useNative) vyroPay else stripePay
    }

    fun setUseVyroEdgeCdn(useVyroEdge: Boolean) {
        _currentCdnService.value = if (useVyroEdge) vyroEdge else cloudflareCdn
    }
}
