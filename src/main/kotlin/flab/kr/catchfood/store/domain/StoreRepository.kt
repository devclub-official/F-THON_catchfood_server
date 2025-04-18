package flab.kr.catchfood.store.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface StoreRepository : JpaRepository<Store, Long> {
    @Query("SELECT DISTINCT s FROM Store s LEFT JOIN s.menus m WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    fun findByNameOrMenuNameContainingIgnoreCase(keyword: String): List<Store>
}
