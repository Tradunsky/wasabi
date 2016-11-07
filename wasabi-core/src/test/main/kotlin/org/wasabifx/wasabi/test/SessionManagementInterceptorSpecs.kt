 package org.wasabifx.wasabi.test

import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.cookie.ServerCookieEncoder
import org.junit.Test as spec
 import kotlin.test.assertEquals
import org.junit.Ignore
import org.wasabifx.wasabi.interceptors.enableSessionSupport
import kotlin.test.assertTrue

 class SessionManagementInterceptorSpecs: TestServerContext() {

    class CustomSession(val name: String) {

    }
    @spec fun should_have_same_session_between_multiple_requests() {

        TestServer.appServer.enableSessionSupport()
        TestServer.appServer.get("/test_session", {

        })

        // Make sure session id stays consistent between requests.
        var response = get("http://localhost:${TestServer.definedPort}/test_session", hashMapOf())
        var session_id = ""
        response.headers.iterator().forEach {
            if (it.name == "Set-Cookie") {
                session_id = it.value
            }
        }

        // Set session cookie as you would expect the client to do...
        val response2 = get("http://localhost:${TestServer.definedPort}/test_session", hashMapOf(), hashMapOf(Pair(session_id.split("=").first(), session_id.split("=").last())))

        // Check it comes back and matches original.
        var session_id_2 = ""
        response2.headers.iterator().forEach {
            if (it.name == "Set-Cookie") {
                session_id_2 = it.value
            }
        }
        assertEquals(session_id, session_id_2)
    }

     @spec fun cookie_should_have_root_path() {
         TestServer.appServer.enableSessionSupport()
         TestServer.appServer.get("/test-session", {
             response.send("Test", "text/plain")
         })

         // Make sure session id stays consistent between requests.
         val response = get("http://localhost:${TestServer.definedPort}/test-session", hashMapOf())

         assertTrue(response.headers.filter { it.name == "Set-Cookie" }.count() > 0)

         response.headers.forEach {
             if (it.name == "Set-Cookie") {
                 val parts = it.value.split(";")
                 assertTrue(parts.filter { it.trim() == "Path=/" }.count() > 0)
             }
         }
     }

}
