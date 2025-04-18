package flab.kr.catchfood.party.poll.application

import flab.kr.catchfood.party.core.application.PartyService
import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.poll.domain.*
import flab.kr.catchfood.store.application.dto.RepresentativeMenuDto
import flab.kr.catchfood.store.domain.Menu
import flab.kr.catchfood.store.domain.Store
import flab.kr.catchfood.user.domain.User
import flab.kr.catchfood.user.domain.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MealPollServiceTest {

    @Mock
    private lateinit var mealPollRepository: MealPollRepository

    @Mock
    private lateinit var partyService: PartyService

    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    @Mock
    private lateinit var recommendStoreRepository: RecommendStoreRepository

    @Mock
    private lateinit var voteRepository: VoteRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var mealPollService: MealPollService

    @Test
    fun `createPoll should create a new poll for the party`() {
        // Given
        val partyId = 1L
        val party = Party(id = partyId, name = "Test Party")
        val savedMealPoll = MealPoll(id = 1L, party = party, status = MealPollStatus.IN_PROGRESS)

        `when`(partyService.getParty(partyId)).thenReturn(party)
        `when`(mealPollRepository.save(any(MealPoll::class.java))).thenReturn(savedMealPoll)

        // When
        val result = mealPollService.createPoll(partyId)

        // Then
        assertEquals(1L, result.id)
        assertEquals(partyId, result.party.id)
        assertEquals(MealPollStatus.IN_PROGRESS, result.status)
        verify(partyService).getParty(partyId)
        verify(mealPollRepository).save(any(MealPoll::class.java))
    }

    @Test
    fun `createPoll should throw exception when party not found`() {
        // Given
        val partyId = 999L

        `when`(partyService.getParty(partyId)).thenThrow(IllegalArgumentException("Party with id $partyId not found"))

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            mealPollService.createPoll(partyId)
        }

        assertEquals("Party with id $partyId not found", exception.message)
        verify(partyService).getParty(partyId)
        // No need to verify that save was never called since the exception would prevent it
    }

    @Test
    fun `createPoll should throw exception when there is already an ongoing poll`() {
        // Given
        val partyId = 1L
        val party = Party(id = partyId, name = "Test Party")
        val ongoingPoll = MealPoll(id = 1L, party = party, status = MealPollStatus.IN_PROGRESS)

        `when`(partyService.getParty(partyId)).thenReturn(party)
        `when`(mealPollRepository.findByParty(party)).thenReturn(listOf(ongoingPoll))

        // When & Then
        val exception = assertThrows(IllegalStateException::class.java) {
            mealPollService.createPoll(partyId)
        }

        assertEquals("There is already an ongoing poll for this party.", exception.message)
        verify(partyService).getParty(partyId)
        verify(mealPollRepository, never()).save(any(MealPoll::class.java))
    }

    @Test
    fun `getPartyPolls should return polls for the party`() {
        // Given
        val partyId = 1L
        val party = Party(id = partyId, name = "Test Party")
        val completedPoll1 = MealPoll(id = 1L, party = party, status = MealPollStatus.DONE)
        val completedPoll2 = MealPoll(id = 2L, party = party, status = MealPollStatus.DONE)
        val ongoingPoll = MealPoll(id = 3L, party = party, status = MealPollStatus.IN_PROGRESS)

        `when`(partyService.getParty(partyId)).thenReturn(party)
        `when`(mealPollRepository.findByParty(party))
            .thenReturn(listOf(completedPoll1, completedPoll2, ongoingPoll))

        // When
        val result = mealPollService.getPartyPolls(partyId)

        // Then
        assertEquals(listOf(1L, 2L), result.done)
        assertEquals(3L, result.ongoing)
        verify(partyService).getParty(partyId)
        verify(mealPollRepository).findByParty(party)
    }

    @Test
    fun `getPartyPolls should return empty lists when no polls exist`() {
        // Given
        val partyId = 1L
        val party = Party(id = partyId, name = "Test Party")

        `when`(partyService.getParty(partyId)).thenReturn(party)
        `when`(mealPollRepository.findByParty(party))
            .thenReturn(emptyList())

        // When
        val result = mealPollService.getPartyPolls(partyId)

        // Then
        assertEquals(emptyList<Long>(), result.done)
        assertNull(result.ongoing)
        verify(partyService).getParty(partyId)
        verify(mealPollRepository).findByParty(party)
    }

    @Test
    fun `getPartyPolls should throw exception when party not found`() {
        // Given
        val partyId = 999L

        `when`(partyService.getParty(partyId)).thenThrow(IllegalArgumentException("Party with id $partyId not found"))

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            mealPollService.getPartyPolls(partyId)
        }

        assertEquals("Party with id $partyId not found", exception.message)
        verify(partyService).getParty(partyId)
        // No need to verify repository calls since the exception would prevent them
    }

    @Test
    fun `getPollDetails should return poll details`() {
        // Given
        val partyId = 1L
        val pollId = 1L
        val currentUserName = "김진홍"
        val party = Party(id = partyId, name = "Test Party")
        val poll = MealPoll(id = pollId, party = party, status = MealPollStatus.IN_PROGRESS)

        // Create users
        val user1 = User(id = 1L, name = "김진홍")
        val user2 = User(id = 2L, name = "정종찬")

        // Create preferences
        val preference1 = Preference(
            id = 1L,
            user = user1,
            poll = poll,
            content = "매운 거"
        )
        val preference2 = Preference(
            id = 2L,
            user = user2,
            poll = poll,
            content = "한식 아무거나"
        )

        // Create store
        val store = Store(
            id = 1L,
            name = "홍콩반점",
            category = "중식",
            distanceInMinutesByWalk = 10,
            businessOpenHour = LocalTime.of(9, 0),
            businessCloseHour = LocalTime.of(22, 0),
            address = "서울시 강남구",
            contact = "02-123-4567",
            ratingStars = BigDecimal("4.5")
        )

        // Add menu to store
        val menu = Menu(
            id = 1L,
            store = store,
            name = "짬뽕",
            price = 8000,
            imageUrl = ""
        )
        store.menus.add(menu)

        // Create recommend store
        val recommendStore = RecommendStore(
            id = 1L,
            poll = poll,
            store = store
        )

        // Create vote
        val vote = Vote(
            id = 1L,
            poll = poll,
            store = recommendStore,
            user = user1
        )

        `when`(partyService.getParty(partyId)).thenReturn(party)
        `when`(mealPollRepository.findById(pollId)).thenReturn(Optional.of(poll))
        `when`(preferenceRepository.findByPoll(poll)).thenReturn(listOf(preference1, preference2))
        `when`(recommendStoreRepository.findByPoll(poll)).thenReturn(listOf(recommendStore))
        `when`(voteRepository.findByPollAndStore(poll, recommendStore)).thenReturn(listOf(vote))
        `when`(userRepository.findByName(currentUserName)).thenReturn(user1)

        // When
        val result = mealPollService.getPollDetails(partyId, pollId, currentUserName)

        // Then
        assertEquals("IN_PROGRESS", result.status)
        assertEquals(2, result.preferences.size)
        assertEquals("매운 거", result.preferences["김진홍"])
        assertEquals("한식 아무거나", result.preferences["정종찬"])
        assertEquals(1, result.recommendedStores.size)

        val resultStore = result.recommendedStores[0]
        assertEquals(1L, resultStore.id)
        assertEquals("홍콩반점", resultStore.storeName)
        assertEquals("짬뽕", resultStore.representativeMenu.name)
        assertEquals("", resultStore.representativeMenu.imageUrl)
        assertEquals("중식", resultStore.category)
        assertEquals(10, resultStore.distanceInMinutesByWalk)
        assertEquals(listOf("김진홍"), resultStore.votedMembers)
        assertTrue(resultStore.isVotedByMe)

        verify(partyService).getParty(partyId)
        verify(mealPollRepository).findById(pollId)
        verify(preferenceRepository).findByPoll(poll)
        verify(recommendStoreRepository).findByPoll(poll)
        verify(voteRepository).findByPollAndStore(poll, recommendStore)
        verify(userRepository).findByName(currentUserName)
    }

    @Test
    fun `getPollDetails should throw exception when party not found`() {
        // Given
        val partyId = 999L
        val pollId = 1L
        val currentUserName = "김진홍"

        `when`(partyService.getParty(partyId)).thenThrow(IllegalArgumentException("Party with id $partyId not found"))

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            mealPollService.getPollDetails(partyId, pollId, currentUserName)
        }

        assertEquals("Party with id $partyId not found", exception.message)
        verify(partyService).getParty(partyId)
        verify(mealPollRepository, never()).findById(pollId)
    }

    @Test
    fun `getPollDetails should throw exception when poll not found`() {
        // Given
        val partyId = 1L
        val pollId = 999L
        val currentUserName = "김진홍"
        val party = Party(id = partyId, name = "Test Party")

        `when`(partyService.getParty(partyId)).thenReturn(party)
        `when`(mealPollRepository.findById(pollId)).thenReturn(Optional.empty())

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            mealPollService.getPollDetails(partyId, pollId, currentUserName)
        }

        assertEquals("Poll with id $pollId not found", exception.message)
        verify(partyService).getParty(partyId)
        verify(mealPollRepository).findById(pollId)
    }

    @Test
    fun `getPollDetails should throw exception when poll does not belong to party`() {
        // Given
        val partyId = 1L
        val pollId = 1L
        val currentUserName = "김진홍"
        val party = Party(id = partyId, name = "Test Party")
        val otherParty = Party(id = 2L, name = "Other Party")
        val poll = MealPoll(id = pollId, party = otherParty, status = MealPollStatus.IN_PROGRESS)

        `when`(partyService.getParty(partyId)).thenReturn(party)
        `when`(mealPollRepository.findById(pollId)).thenReturn(Optional.of(poll))

        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            mealPollService.getPollDetails(partyId, pollId, currentUserName)
        }

        assertEquals("Poll with id $pollId does not belong to party with id $partyId", exception.message)
        verify(partyService).getParty(partyId)
        verify(mealPollRepository).findById(pollId)
    }
}
