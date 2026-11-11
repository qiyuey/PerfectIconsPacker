package io.github.perfecticons.data.repo

import java.net.Proxy
import java.net.URL
import org.junit.Assert.assertSame
import org.junit.Test
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.TransportHttp
import org.eclipse.jgit.transport.URIish

class VerifiedHttpsConnectionFactoryTest {
    @Test(expected = IllegalArgumentException::class)
    fun `rejects disabling certificate verification`() {
        val connection = VerifiedHttpsConnectionFactory.create(URL("https://example.invalid"))
        val session = VerifiedHttpsConnectionFactory.newSession()
        try {
            session.configure(connection, false)
        } finally {
            session.close()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects plain HTTP`() {
        VerifiedHttpsConnectionFactory.create(URL("http://example.invalid"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects plain HTTP through a proxy`() {
        VerifiedHttpsConnectionFactory.create(URL("http://example.invalid"), Proxy.NO_PROXY)
    }

    @Test
    fun `verified HTTPS retains platform connection defaults`() {
        val connection = VerifiedHttpsConnectionFactory.create(URL("https://example.invalid"))
        val session = VerifiedHttpsConnectionFactory.newSession()
        try {
            assertSame(connection, session.configure(connection, true))
        } finally {
            session.close()
        }
    }
    @Test
    fun `installs guarded sessions on JGit HTTPS transport`() {
        Transport.open(URIish("https://example.invalid/repo.git")).use { transport ->
            VerifiedHttpsConnectionFactory.configure(transport)
            assertSame(VerifiedHttpsConnectionFactory, (transport as TransportHttp).httpConnectionFactory)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects JGit HTTP transport`() {
        Transport.open(URIish("http://example.invalid/repo.git")).use { transport ->
            VerifiedHttpsConnectionFactory.configure(transport)
        }
    }

}
