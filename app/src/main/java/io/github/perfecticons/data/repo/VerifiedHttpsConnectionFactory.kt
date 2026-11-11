package io.github.perfecticons.data.repo

import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.TransportHttp
import org.eclipse.jgit.transport.http.HttpConnection
import org.eclipse.jgit.transport.http.HttpConnectionFactory2
import org.eclipse.jgit.transport.http.JDKHttpConnectionFactory
import java.net.Proxy
import java.net.URL

/** Uses platform TLS validation and rejects JGit's optional trust-all mode and HTTP redirects. */
internal object VerifiedHttpsConnectionFactory : HttpConnectionFactory2 {
    private val delegate = JDKHttpConnectionFactory()

    override fun create(url: URL): HttpConnection {
        require(url.protocol == "https") { "Icon downloads require HTTPS" }
        return delegate.create(url)
    }

    override fun create(url: URL, proxy: Proxy?): HttpConnection {
        require(url.protocol == "https") { "Icon downloads require HTTPS" }
        return delegate.create(url, proxy)
    }

    override fun newSession(): HttpConnectionFactory2.GitSession {
        val session = delegate.newSession()
        return object : HttpConnectionFactory2.GitSession {
            override fun configure(connection: HttpConnection, sslVerify: Boolean): HttpConnection {
                require(sslVerify) { "TLS certificate verification cannot be disabled" }
                require(connection.url.protocol == "https") { "Icon downloads require HTTPS" }
                return session.configure(connection, true)
            }

            override fun close() = session.close()
        }
    }

    fun configure(transport: Transport) {
        require(transport is TransportHttp && transport.uri.scheme == "https") {
            "Icon repository requires an HTTPS transport"
        }
        transport.setHttpConnectionFactory(this)
    }
}
