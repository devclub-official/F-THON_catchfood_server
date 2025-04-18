package flab.kr.catchfood.party.poll.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecommendStoreRepository : JpaRepository<RecommendStore, Long> {
    fun findByPoll(poll: MealPoll): List<RecommendStore>
    fun findByStoreId(storeId: Long): RecommendStore?
    fun findByStoreIdAndPoll(storeId: Long, poll: MealPoll): RecommendStore?
}
