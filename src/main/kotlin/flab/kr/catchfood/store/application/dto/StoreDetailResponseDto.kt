package flab.kr.catchfood.store.application.dto

import java.math.BigDecimal

data class StoreDetailResponseDto(
    val id: Long,
    val storeName: String,
    val category: String,
    val distanceInMinutesByWalk: Int,
    val contact: String,
    val address: String,
    val businessHours: BusinessHoursDto,
    val menus: List<MenuDto>,
    val ratingStars: BigDecimal
)

data class BusinessHoursDto(
    val open: String,
    val close: String
)

data class MenuDto(
    val menuName: String,
    val imageUrl: String?,
    val price: Int
)
