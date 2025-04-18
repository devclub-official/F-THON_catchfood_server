package flab.kr.catchfood.store.application.dto

data class StoreListResponseDto(
    val id: Long,
    val storeName: String,
    val category: String,
    val representativeMenu: RepresentativeMenuDto,
    val distanceInMinutesByWalk: Int
)

data class RepresentativeMenuDto(
    val name: String,
    val imageUrl: String?
)
