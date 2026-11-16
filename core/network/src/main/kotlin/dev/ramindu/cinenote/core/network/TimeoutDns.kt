package dev.ramindu.cinenote.core.network

import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * OkHttp cannot interrupt a blocked DNS lookup, so on a broken network a request (and the UI's
 * loading state) can hang far beyond `callTimeout`. This bounds the lookup and fails fast with
 * [UnknownHostException], which the data layer maps to "no internet".
 */
class TimeoutDns(
    private val timeoutMillis: Long,
    private val delegate: Dns = Dns.SYSTEM,
) : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val task = FutureTask { delegate.lookup(hostname) }
        Thread(task, "dns-$hostname").apply { isDaemon = true }.start()
        return try {
            task.get(timeoutMillis, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            task.cancel(true)
            throw UnknownHostException("DNS lookup for $hostname timed out after ${timeoutMillis}ms")
        } catch (e: ExecutionException) {
            throw e.cause as? UnknownHostException ?: UnknownHostException(hostname).apply { initCause(e.cause) }
        }
    }
}
