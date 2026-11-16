package dev.ramindu.cinenote.core.data.mapper

import com.google.common.truth.Truth.assertThat
import dev.ramindu.cinenote.core.testing.TestData
import org.junit.jupiter.api.Test

class FavoriteMappersTest {

    @Test
    fun `domain to entity and back is lossless`() {
        val favorite = TestData.favorite(
            id = 3,
            title = "Arrival",
            note = "Linguistics!",
            addedAt = TestData.NOW,
            noteUpdatedAt = TestData.NOW.plusSeconds(30),
        )

        val roundTripped = favorite.toEntity().toDomain()

        assertThat(roundTripped).isEqualTo(favorite)
    }

    @Test
    fun `entity stores timestamps as epoch millis`() {
        val entity = TestData.favorite(addedAt = TestData.NOW, noteUpdatedAt = null).toEntity()

        assertThat(entity.addedAtEpochMillis).isEqualTo(TestData.NOW.toEpochMilli())
        assertThat(entity.noteUpdatedAtEpochMillis).isNull()
    }
}
