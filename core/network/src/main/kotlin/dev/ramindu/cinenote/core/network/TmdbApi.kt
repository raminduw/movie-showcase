package dev.ramindu.cinenote.core.network

import dev.ramindu.cinenote.core.network.dto.MovieDetailDto
import dev.ramindu.cinenote.core.network.dto.MovieDto
import dev.ramindu.cinenote.core.network.dto.PagedResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** TMDB v3 endpoints used by the app. Docs: https://developer.themoviedb.org/reference */
interface TmdbApi {

    @GET("trending/movie/week")
    suspend fun trendingMovies(@Query("page") page: Int): PagedResponseDto<MovieDto>

    /** [list] is one of `popular`, `top_rated`, `now_playing`, `upcoming`. */
    @GET("movie/{list}")
    suspend fun movieList(
        @Path("list") list: String,
        @Query("page") page: Int,
    ): PagedResponseDto<MovieDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("include_adult") includeAdult: Boolean = false,
    ): PagedResponseDto<MovieDto>

    /** One call returns details, cast, trailers and similar movies. */
    @GET("movie/{id}")
    suspend fun movieDetail(
        @Path("id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar",
    ): MovieDetailDto

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
    }
}
