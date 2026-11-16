package dev.ramindu.cinenote.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Qualifier

/** The TMDB credential from local.properties. Provided by :app from BuildConfig. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TmdbToken

/**
 * Authenticates every TMDB request, so the API interface stays clean.
 *
 * TMDB accepts two credentials (https://developer.themoviedb.org/docs/authentication-application):
 *  - "API Read Access Token" (a JWT, `eyJ…`): sent as `Authorization: Bearer <token>` (preferred)
 *  - "API Key" (32 hex chars): sent as the `api_key` query parameter
 * Either one works in local.properties.
 */
class AuthInterceptor @Inject constructor(
    @TmdbToken private val token: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder().header("Accept", "application/json")
        val credential = token.trim()

        if (credential.isReadAccessToken()) {
            builder.header("Authorization", "Bearer $credential")
        } else if (credential.isNotEmpty()) {
            builder.url(original.url.newBuilder().setQueryParameter("api_key", credential).build())
        }
        return chain.proceed(builder.build())
    }

    /** A Read Access Token is a JWT: three dot-separated parts. The v3 API key has no dots. */
    private fun String.isReadAccessToken(): Boolean = count { it == '.' } == 2
}
