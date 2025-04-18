package flab.kr.catchfood.party.poll.application

import flab.kr.catchfood.openai.service.ChatGPTService
import flab.kr.catchfood.party.core.application.PartyService
import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.poll.application.dto.PreferenceRequestDto
import flab.kr.catchfood.party.poll.domain.*
import flab.kr.catchfood.store.application.dto.RepresentativeMenuDto
import flab.kr.catchfood.store.domain.StoreRepository
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
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val chatGPTService: ChatGPTService
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

        // Check if all party members have registered preferences and add recommended stores if they have
        checkAndAddRecommendedStores(poll)
    }

    private fun checkAndAddRecommendedStores(poll: MealPoll) {
        // Get all party members
        val partyMembers = partyService.getPartyMembers(poll.party)

        // Get all users who have registered preferences in this poll
        val preferences = preferenceRepository.findByPoll(poll)
        val usersWithPreferences = preferences.map { it.user.id }.distinct()

        // Check if all party members have registered preferences
        val allMembersRegisteredPreferences = partyMembers.all { member -> usersWithPreferences.contains(member.id) }

        // If all members have registered preferences, use ChatGPT to recommend stores
        if (allMembersRegisteredPreferences) {
            // Check if there are already recommended stores for this poll
            val existingRecommendedStores = recommendStoreRepository.findByPoll(poll)
            if (existingRecommendedStores.isEmpty()) {
                // Get all stores
                val allStores = storeRepository.findAll()

                // Use ChatGPT to recommend stores
                val recommendedStoreIds = chatGPTService.recommendStores(
                    stores = allStores,
                    partyMembers = partyMembers,
                    preferences = preferences
                )

                // Add recommended stores
                val addedStores = mutableListOf<Long>()
                recommendedStoreIds.forEach { storeId ->
                    val store = storeRepository.findById(storeId).orElse(null)
                    if (store != null) {
                        val recommendStore = RecommendStore(
                            poll = poll,
                            store = store
                        )
                        recommendStoreRepository.save(recommendStore)
                        addedStores.add(storeId)
                    }
                }

                // If fewer than 3 stores were recommended, add random stores to reach minimum of 3
                if (addedStores.size < 3) {
                    // Log the issue
                    println("ChatGPT recommended fewer than 3 stores (${addedStores.size}). Adding random stores to reach minimum of 3.")

                    // Get stores that weren't recommended
                    val remainingStores = allStores.filter { store -> store.id !in addedStores }

                    // Shuffle the remaining stores and select enough to reach minimum of 3
                    val additionalStores = remainingStores.shuffled().take(3 - addedStores.size)

                    // Add the additional stores
                    additionalStores.forEach { store ->
                        val recommendStore = RecommendStore(
                            poll = poll,
                            store = store
                        )
                        recommendStoreRepository.save(recommendStore)
                    }
                }
            }
        }
    }

    @Transactional
    fun voteForRecommendedStore(partyId: Long, pollId: Long, recommendedStoreId: Long, userName: String) {
        val party = partyService.getParty(partyId)
        val poll = mealPollRepository.findById(pollId)
            .orElseThrow { IllegalArgumentException("Poll with id $pollId not found") }

        if (poll.party.id != party.id) {
            throw IllegalArgumentException("Poll with id $pollId does not belong to party with id $partyId")
        }

        val recommendStore = recommendStoreRepository.findByStoreId(recommendedStoreId)
            ?: throw IllegalArgumentException("Recommended store with id $recommendedStoreId not found")

        if (recommendStore.poll.id != poll.id) {
            throw IllegalArgumentException("Recommended store with id $recommendedStoreId does not belong to poll with id $pollId")
        }

        val user = userRepository.findByName(userName)
            ?: throw IllegalArgumentException("User with name $userName not found")

        // Check if the user has already voted for this store in this poll
        val existingVotes = voteRepository.findByPollAndStore(poll, recommendStore)
        val userAlreadyVoted = existingVotes.any { it.user.id == user.id }

        if (userAlreadyVoted) {
            throw IllegalStateException("User has already voted for this store in this poll")
        }

        val vote = Vote(
            poll = poll,
            store = recommendStore,
            user = user
        )

        voteRepository.save(vote)

        // Check if all party members have voted
        checkAndUpdatePollStatus(poll)
    }

    private fun checkAndUpdatePollStatus(poll: MealPoll) {
        // Get all party members
        val partyMembers = partyService.getPartyMembers(poll.party)

        // Get all users who have voted in this poll
        val allVotes = voteRepository.findByPoll(poll)
        val votedUsers = allVotes.map { it.user.id }.distinct()

        // Check if all party members have voted
        val allMembersVoted = partyMembers.all { member -> votedUsers.contains(member.id) }

        // If all members have voted, update the poll status to DONE
        if (allMembersVoted && poll.status != MealPollStatus.DONE) {
            poll.status = MealPollStatus.DONE
            mealPollRepository.save(poll)
        }
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
