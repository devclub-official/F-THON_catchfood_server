package flab.kr.catchfood.party.poll.application

import flab.kr.catchfood.party.core.application.PartyService
import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.poll.application.dto.PreferenceRequestDto
import flab.kr.catchfood.party.poll.domain.*
import flab.kr.catchfood.store.application.dto.RepresentativeMenuDto
import flab.kr.catchfood.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MealPollService(
    private val mealPollRepository: MealPollRepository,
    private val partyService: PartyService,
    private val preferenceRepository: PreferenceRepository,
    private val recommendStoreRepository: RecommendStoreRepository,
    private val voteRepository: VoteRepository,
    private val userRepository: UserRepository
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

    @Transactional(readOnly = true)
    fun getPollDetails(partyId: Long, pollId: Long, currentUserName: String): PollDetailsDto {
        val party = partyService.getParty(partyId)
        val poll = mealPollRepository.findById(pollId)
            .orElseThrow { IllegalArgumentException("Poll with id $pollId not found") }

        if (poll.party.id != party.id) {
            throw IllegalArgumentException("Poll with id $pollId does not belong to party with id $partyId")
        }

        // Get preferences from the database
        val preferencesEntities = preferenceRepository.findByPoll(poll)
        val preferences = preferencesEntities.associate { it.user.name to it.content }

        // Get recommended stores from the database
        val recommendStores = recommendStoreRepository.findByPoll(poll)
        val currentUser = userRepository.findByName(currentUserName)

        val recommendedStores = recommendStores.map { recommendStore ->
            val store = recommendStore.store
            val votes = voteRepository.findByPollAndStore(poll, recommendStore)
            val votedMembers = votes.map { it.user.name }

            RecommendedStoreDto(
                id = store.id!!,
                storeName = store.name,
                representativeMenu = RepresentativeMenuDto(
                    name = store.menus.firstOrNull()?.name ?: "",
                    imageUrl = store.menus.firstOrNull()?.imageUrl ?: ""
                ),
                category = store.category,
                distanceInMinutesByWalk = store.distanceInMinutesByWalk,
                votedMembers = votedMembers,
                isVotedByMe = currentUser != null && votedMembers.contains(currentUser.name)
            )
        }

        return PollDetailsDto(
            status = poll.status.name,
            preferences = preferences,
            recommendedStores = recommendedStores
        )
    }

    @Transactional
    fun addPreference(partyId: Long, pollId: Long, userName: String, preferenceRequest: PreferenceRequestDto) {
        val party = partyService.getParty(partyId)
        val poll = mealPollRepository.findById(pollId)
            .orElseThrow { IllegalArgumentException("Poll with id $pollId not found") }

        if (poll.party.id != party.id) {
            throw IllegalArgumentException("Poll with id $pollId does not belong to party with id $partyId")
        }

        val user = userRepository.findByName(userName)
            ?: throw IllegalArgumentException("User with name $userName not found")

        val preference = Preference(
            user = user,
            poll = poll,
            content = preferenceRequest.preference
        )

        preferenceRepository.save(preference)
    }
}

data class PartyPollsDto(
    val done: List<Long>,
    val ongoing: Long?
)

data class PollDetailsDto(
    val status: String,
    val preferences: Map<String, String>,
    val recommendedStores: List<RecommendedStoreDto>
)

data class RecommendedStoreDto(
    val id: Long,
    val storeName: String,
    val representativeMenu: RepresentativeMenuDto,
    val category: String,
    val distanceInMinutesByWalk: Int,
    val votedMembers: List<String>,
    val isVotedByMe: Boolean
)
