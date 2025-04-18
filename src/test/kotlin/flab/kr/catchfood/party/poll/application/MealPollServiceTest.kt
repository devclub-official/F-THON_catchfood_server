package flab.kr.catchfood.party.poll.application

import flab.kr.catchfood.party.core.application.PartyService
import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.poll.domain.MealPoll
import flab.kr.catchfood.party.poll.domain.MealPollRepository
import flab.kr.catchfood.party.poll.domain.MealPollStatus
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

@ExtendWith(MockitoExtension::class)
class MealPollServiceTest {

    @Mock
    private lateinit var mealPollRepository: MealPollRepository

    @Mock
    private lateinit var partyService: PartyService

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
}
