package flab.kr.catchfood.store.application

import flab.kr.catchfood.store.application.dto.*
import flab.kr.catchfood.store.domain.Store
import flab.kr.catchfood.store.domain.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
class StoreService(private val storeRepository: StoreRepository) {

    @Transactional(readOnly = true)
    fun getAllStores(): List<StoreListResponseDto> {
        return storeRepository.findAll().map { convertToStoreListDto(it) }
    }

    @Transactional(readOnly = true)
    fun searchStores(keyword: String): List<StoreListResponseDto> {
        return storeRepository.findByNameOrMenuNameContainingIgnoreCase(keyword).map { convertToStoreListDto(it) }
    }

    @Transactional(readOnly = true)
    fun getStoreById(id: Long): StoreDetailResponseDto {
        val store = storeRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Store with id $id not found") }
        return convertToStoreDetailDto(store)
    }

    private fun convertToStoreListDto(store: Store): StoreListResponseDto {
        val representativeMenu = store.menus.firstOrNull()?.let {
            RepresentativeMenuDto(
                name = it.name,
                imageUrl = it.imageUrl
            )
        } ?: RepresentativeMenuDto(name = "", imageUrl = null)

        return StoreListResponseDto(
            id = store.id!!,
            storeName = store.name,
            category = store.category,
            representativeMenu = representativeMenu,
            distanceInMinutesByWalk = store.distanceInMinutesByWalk
        )
    }

    private fun convertToStoreDetailDto(store: Store): StoreDetailResponseDto {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        return StoreDetailResponseDto(
            id = store.id!!,
            storeName = store.name,
            category = store.category,
            distanceInMinutesByWalk = store.distanceInMinutesByWalk,
            contact = store.contact,
            address = store.address,
            businessHours = BusinessHoursDto(
                open = store.businessOpenHour.format(timeFormatter),
                close = store.businessCloseHour.format(timeFormatter)
            ),
            menus = store.menus.map {
                MenuDto(
                    menuName = it.name,
                    imageUrl = it.imageUrl,
                    price = it.price
                )
            },
            ratingStars = store.ratingStars
        )
    }
}
