package ir.mabhas19.core.network

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class PublicApiTest {
    @Test fun `uses discovered path and preserves application error on HTTP success`() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody("""{"ok":false,"error":"unavailable","extra":42}""")
            )
            val response = PublicApiFactory.create(server.url("/").toString()).reference()
            assertFalse(response.ok)
            assertEquals("unavailable", response.error)
            val request = server.takeRequest(2, TimeUnit.SECONDS)
            assertNotNull(request)
            assertEquals("GET", request!!.method)
            assertEquals("/api/reference.php", request.path)
        }
    }
}
