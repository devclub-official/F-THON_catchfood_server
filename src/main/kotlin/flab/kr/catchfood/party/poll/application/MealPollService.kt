package flab.kr.catchfood.party.poll.application

import flab.kr.catchfood.party.core.application.PartyService
import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.poll.domain.MealPoll
import flab.kr.catchfood.party.poll.domain.MealPollRepository
import flab.kr.catchfood.party.poll.domain.MealPollStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MealPollService(
    private val mealPollRepository: MealPollRepository,
    private val partyService: PartyService
) {

    @Transactional
    fun createPoll(partyId: Long): MealPoll {
        val party = partyService.getParty(partyId)
        val mealPoll = MealPoll(
            party = party,
            status = MealPollStatus.IN_PROGRESS
        )

        val ongoingPoll = mealPollRepository.findByParty(party).firstOrNull { it.status == MealPollStatus.IN_PROGRESS }
        if (ongoingPoll != null) {
            throw IllegalStateException("There is already an ongoing poll for this party.")
        }

        return mealPollRepository.save(mealPoll)
    }

    @Transactional(readOnly = true)
    fun getPartyPolls(partyId: Long): PartyPollsDto {
        val party = partyService.getParty(partyId)
        val mealPolls = mealPollRepository.findByParty(party)
        
        return PartyPollsDto(
            done = mealPolls.filter { it.status == MealPollStatus.DONE }.map { it.id!! },
            ongoing = mealPolls.filter { it.status == MealPollStatus.IN_PROGRESS }.map { it.id!! }.firstOrNull()
        )
    }
}

data class PartyPollsDto(
    val done: List<Long>,
    val ongoing: Long?
)
