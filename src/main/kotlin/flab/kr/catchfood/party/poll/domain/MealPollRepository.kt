package flab.kr.catchfood.party.poll.domain

import flab.kr.catchfood.party.core.domain.Party
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MealPollRepository : JpaRepository<MealPoll, Long> {
    fun findByParty(party: Party): List<MealPoll>
}
