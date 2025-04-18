package flab.kr.catchfood.store.application

import flab.kr.catchfood.store.domain.Menu
import flab.kr.catchfood.store.domain.Store
import flab.kr.catchfood.store.domain.StoreRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class StoreServiceTest {

    @Mock
    private lateinit var storeRepository: StoreRepository

    @InjectMocks
    private lateinit var storeService: StoreService

    @Test
    fun `getAllStores should return all stores from repository`() {
        // Given
        val store1 = createStore(1L, "Store 1")
        val store2 = createStore(2L, "Store 2")
        val stores = listOf(store1, store2)

        `when`(storeRepository.findAll()).thenReturn(stores)

        // When
        val result = storeService.getAllStores()

        // Then
        assertEquals(2, result.size)
        assertEquals("Store 1", result[0].storeName)
        assertEquals("Store 2", result[1].storeName)
        verify(storeRepository).findAll()
    }

    @Test
    fun `searchStores should return stores matching keyword in store name`() {
        // Given
        val store1 = createStore(1L, "Korean BBQ")
        val stores = listOf(store1)

        `when`(storeRepository.findByNameOrMenuNameContainingIgnoreCase("Korean")).thenReturn(stores)

        // When
        val result = storeService.searchStores("Korean")

        // Then
        assertEquals(1, result.size)
        assertEquals("Korean BBQ", result[0].storeName)
        verify(storeRepository).findByNameOrMenuNameContainingIgnoreCase("Korean")
    }

    @Test
    fun `searchStores should return stores matching keyword in menu name`() {
        // Given
        val store1 = createStore(1L, "Italian Restaurant")
        val menu = Menu(
            id = 1L,
            store = store1,
            name = "Korean Pasta",
            price = 15000,
            imageUrl = "http://example.com/korean-pasta.jpg"
        )
        store1.menus.add(menu)

        val stores = listOf(store1)

        `when`(storeRepository.findByNameOrMenuNameContainingIgnoreCase("Korean")).thenReturn(stores)

        // When
        val result = storeService.searchStores("Korean")

        // Then
        assertEquals(1, result.size)
        assertEquals("Italian Restaurant", result[0].storeName)
        assertEquals("Korean Pasta", result[0].representativeMenu.name)
        verify(storeRepository).findByNameOrMenuNameContainingIgnoreCase("Korean")
    }

    @Test
    fun `getStoreById should return store when exists`() {
        // Given
        val store = createStore(1L, "Test Store")
        val menu = Menu(
            id = 1L,
            store = store,
            name = "Test Menu",
            price = 10000,
            imageUrl = "http://example.com/image.jpg"
        )
        store.menus.add(menu)

        `when`(storeRepository.findById(1L)).thenReturn(Optional.of(store))

        // When
        val result = storeService.getStoreById(1L)

        // Then
        assertEquals("Test Store", result.storeName)
        assertEquals("Category", result.category)
        assertEquals(10, result.distanceInMinutesByWalk)
        assertEquals("123-456-7890", result.contact)
        assertEquals("Test Address", result.address)
        assertEquals("09:00", result.businessHours.open)
        assertEquals("18:00", result.businessHours.close)
        assertEquals(1, result.menus.size)
        assertEquals("Test Menu", result.menus[0].menuName)
        assertEquals(10000, result.menus[0].price)
        assertEquals("http://example.com/image.jpg", result.menus[0].imageUrl)
        assertEquals(BigDecimal("4.5"), result.ratingStars)
        verify(storeRepository).findById(1L)
    }

    @Test
    fun `getStoreById should throw exception when store does not exist`() {
        // Given
        `when`(storeRepository.findById(999L)).thenReturn(Optional.empty())

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            storeService.getStoreById(999L)
        }

        assertEquals("Store with id 999 not found", exception.message)
        verify(storeRepository).findById(999L)
    }

    private fun createStore(id: Long, name: String): Store {
        return Store(
            id = id,
            name = name,
            category = "Category",
            distanceInMinutesByWalk = 10,
            businessOpenHour = LocalTime.of(9, 0),
            businessCloseHour = LocalTime.of(18, 0),
            address = "Test Address",
            contact = "123-456-7890",
            ratingStars = BigDecimal("4.5")
        )
    }
}
