package flab.kr.catchfood.party.poll.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoteRepository : JpaRepository<Vote, Long> {
    fun findByPoll(poll: MealPoll): List<Vote>
    fun findByPollAndStore(poll: MealPoll, store: RecommendStore): List<Vote>
}
