package app.example.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import app.example.di.ApplicationContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Interface that monitors network connectivity status.
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}

/**
 * Production implementation of [NetworkMonitor] using [ConnectivityManager.NetworkCallback].
 *
 * Demonstrates real-time reactive network status monitoring using Kotlin [Flow] and
 * [ConnectivityManager].
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class NetworkMonitorImpl
    constructor(
        @ApplicationContext context: Context,
    ) : NetworkMonitor {
        private val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        override val isOnline: Flow<Boolean> =
            callbackFlow {
                val callback =
                    object : ConnectivityManager.NetworkCallback() {
                        private val networks = mutableSetOf<Network>()

                        override fun onAvailable(network: Network) {
                            networks.add(network)
                            trySend(networks.isNotEmpty())
                        }

                        override fun onLost(network: Network) {
                            networks.remove(network)
                            trySend(networks.isNotEmpty())
                        }
                    }

                val request =
                    NetworkRequest
                        .Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build()

                connectivityManager.registerNetworkCallback(request, callback)

                val activeNetwork = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                val initialOnline =
                    capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                trySend(initialOnline)

                awaitClose {
                    connectivityManager.unregisterNetworkCallback(callback)
                }
            }.distinctUntilChanged()
    }
