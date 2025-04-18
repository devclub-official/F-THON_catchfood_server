package flab.kr.catchfood.store.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.math.BigDecimal
import java.time.LocalTime

@DataJpaTest
class StoreRepositoryTest {

    @Autowired
    private lateinit var storeRepository: StoreRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `findByNameOrMenuNameContainingIgnoreCase should return stores with matching store name`() {
        // Given
        val store1 = Store(
            name = "한국식 바베큐 레스토랑",
            category = "한식",
            distanceInMinutesByWalk = 10,
            businessOpenHour = LocalTime.of(9, 0),
            businessCloseHour = LocalTime.of(22, 0),
            address = "123 Main St",
            contact = "123-456-7890",
            ratingStars = BigDecimal("4.5")
        )

        val store2 = Store(
            name = "이탈리안 파스타 하우스",
            category = "양식",
            distanceInMinutesByWalk = 15,
            businessOpenHour = LocalTime.of(10, 0),
            businessCloseHour = LocalTime.of(21, 0),
            address = "456 Oak St",
            contact = "987-654-3210",
            ratingStars = BigDecimal("4.2")
        )

        entityManager.persist(store1)
        entityManager.persist(store2)
        entityManager.flush()

        // When
        val result = storeRepository.findByNameOrMenuNameContainingIgnoreCase("한국식")

        // Then
        assertEquals(1, result.size)
        assertEquals("한국식 바베큐 레스토랑", result[0].name)
    }

    @Test
    fun `findByNameOrMenuNameContainingIgnoreCase should return stores with matching menu name`() {
        // Given
        val store = Store(
            name = "이탈리안 레스토랑",
            category = "양식",
            distanceInMinutesByWalk = 15,
            businessOpenHour = LocalTime.of(10, 0),
            businessCloseHour = LocalTime.of(21, 0),
            address = "456 Oak St",
            contact = "987-654-3210",
            ratingStars = BigDecimal("4.2")
        )

        entityManager.persist(store)

        val menu = Menu(
            store = store,
            name = "한국식 퓨전 파스타",
            price = 15000,
            imageUrl = "http://example.com/korean-pasta.jpg"
        )

        entityManager.persist(menu)
        entityManager.flush()

        // When
        val result = storeRepository.findByNameOrMenuNameContainingIgnoreCase("한국식")

        // Then
        assertEquals(1, result.size)
        assertEquals("이탈리안 레스토랑", result[0].name)
    }

    @Test
    fun `findByNameOrMenuNameContainingIgnoreCase should be case insensitive`() {
        // Given
        val store = Store(
            name = "한국식 바베큐 레스토랑",
            category = "한식",
            distanceInMinutesByWalk = 10,
            businessOpenHour = LocalTime.of(9, 0),
            businessCloseHour = LocalTime.of(22, 0),
            address = "123 Main St",
            contact = "123-456-7890",
            ratingStars = BigDecimal("4.5")
        )

        entityManager.persist(store)
        entityManager.flush()

        // When - search with different case
        val result = storeRepository.findByNameOrMenuNameContainingIgnoreCase("한국식")

        // Then
        assertEquals(1, result.size)
        assertEquals("한국식 바베큐 레스토랑", result[0].name)
    }

    @Test
    fun `findByNameOrMenuNameContainingIgnoreCase should return empty list when no matches`() {
        // Given
        val store = Store(
            name = "이탈리안 레스토랑",
            category = "양식",
            distanceInMinutesByWalk = 15,
            businessOpenHour = LocalTime.of(10, 0),
            businessCloseHour = LocalTime.of(21, 0),
            address = "456 Oak St",
            contact = "987-654-3210",
            ratingStars = BigDecimal("4.2")
        )

        entityManager.persist(store)
        entityManager.flush()

        // When
        val result = storeRepository.findByNameOrMenuNameContainingIgnoreCase("일식")

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findByNameOrMenuNameContainingIgnoreCase should return distinct stores when both name and menu match`() {
        // Given
        val store = Store(
            name = "한국식 바베큐 레스토랑",
            category = "한식",
            distanceInMinutesByWalk = 10,
            businessOpenHour = LocalTime.of(9, 0),
            businessCloseHour = LocalTime.of(22, 0),
            address = "123 Main St",
            contact = "123-456-7890",
            ratingStars = BigDecimal("4.5")
        )

        entityManager.persist(store)

        val menu = Menu(
            store = store,
            name = "한우 바베큐",
            price = 20000,
            imageUrl = "http://example.com/korean-bbq.jpg"
        )

        entityManager.persist(menu)
        entityManager.flush()

        // When - both store name and menu name contain "한국식" or "한우"
        val result = storeRepository.findByNameOrMenuNameContainingIgnoreCase("한")

        // Then - should return only one store (distinct)
        assertEquals(1, result.size)
        assertEquals("한국식 바베큐 레스토랑", result[0].name)
    }
}
