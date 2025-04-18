package flab.kr.catchfood.store.ui

import flab.kr.catchfood.common.ui.dto.ApiResponse
import flab.kr.catchfood.store.application.StoreService
import flab.kr.catchfood.store.application.dto.StoreDetailResponseDto
import flab.kr.catchfood.store.application.dto.StoreListResponseDto
import org.springframework.web.bind.annotation.*

@RestController
class StoreController(private val storeService: StoreService) {

    @GetMapping("/stores")
    fun getStores(@RequestParam(required = false) keyword: String?): ApiResponse<List<StoreListResponseDto>> {
        val stores = if (keyword.isNullOrBlank()) {
            storeService.getAllStores()
        } else {
            storeService.searchStores(keyword)
        }
        return ApiResponse.success(stores)
    }

    @GetMapping("/stores/{id}")
    fun getStoreById(@PathVariable id: Long): ApiResponse<StoreDetailResponseDto> {
        val store = storeService.getStoreById(id)
        return ApiResponse.success(store)
    }
}
