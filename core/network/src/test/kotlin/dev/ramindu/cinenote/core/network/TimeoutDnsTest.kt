package dev.ramindu.cinenote.core.network

import com.google.common.truth.Truth.assertThat
import okhttp3.Dns
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.InetAddress
import java.net.UnknownHostException

class TimeoutDnsTest {

    private val localhost = listOf(InetAddress.getLoopbackAddress())

    @Test
    fun `returns the delegate's addresses when it answers in time`() {
        val dns = TimeoutDns(timeoutMillis = 1_000, delegate = Dns { localhost })

        assertThat(dns.lookup("api.themoviedb.org")).isEqualTo(localhost)
    }

    @Test
    fun `hanging lookup fails fast with UnknownHostException`() {
        val dns = TimeoutDns(timeoutMillis = 50, delegate = Dns { Thread.sleep(5_000); localhost })

        val start = System.nanoTime()
        val error = assertThrows<UnknownHostException> { dns.lookup("api.themoviedb.org") }

        assertThat(error).hasMessageThat().contains("timed out")
        assertThat((System.nanoTime() - start) / 1_000_000).isLessThan(2_000)
    }

    @Test
    fun `delegate failure is surfaced as UnknownHostException`() {
        val dns = TimeoutDns(timeoutMillis = 1_000, delegate = Dns { throw UnknownHostException("nope") })

        val error = assertThrows<UnknownHostException> { dns.lookup("x") }

        assertThat(error).hasMessageThat().isEqualTo("nope")
    }
}
