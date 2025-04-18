package flab.kr.catchfood.party.poll.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PreferenceRepository : JpaRepository<Preference, Long> {
    fun findByPoll(poll: MealPoll): List<Preference>
}
