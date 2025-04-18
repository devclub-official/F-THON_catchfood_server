package flab.kr.catchfood.party.core.application

import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.core.domain.PartyMember
import flab.kr.catchfood.party.core.domain.PartyMemberId
import flab.kr.catchfood.party.core.domain.PartyMemberRepository
import flab.kr.catchfood.party.core.domain.PartyRepository
import flab.kr.catchfood.user.domain.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PartyServiceTest {

    @Mock
    private lateinit var partyRepository: PartyRepository

    @Mock
    private lateinit var partyMemberRepository: PartyMemberRepository

    @InjectMocks
    private lateinit var partyService: PartyService

    @Test
    fun `createParty should create a new party and add creator as member`() {
        // Given
        val partyName = "Test Party"
        val creator = User(id = 1L, name = "testUser")
        val savedParty = Party(id = 1L, name = partyName)
        val partyMember = PartyMember(
            id = PartyMemberId(userId = creator.id!!, partyId = savedParty.id!!),
            user = creator,
            party = savedParty
        )

        `when`(partyRepository.save(any(Party::class.java))).thenReturn(savedParty)
        `when`(partyMemberRepository.save(any(PartyMember::class.java))).thenReturn(partyMember)

        // When
        val result = partyService.createParty(partyName, creator)

        // Then
        assertEquals(1L, result.id)
        assertEquals(partyName, result.name)
        verify(partyRepository).save(any(Party::class.java))
        verify(partyMemberRepository).save(any(PartyMember::class.java))
    }

    @Test
    fun `getPartiesForUser should return parties for the given user`() {
        // Given
        val user = User(id = 1L, name = "testUser")
        val party1 = Party(id = 1L, name = "Party 1")
        val party2 = Party(id = 2L, name = "Party 2")
        val partyMember1 = PartyMember(
            id = PartyMemberId(userId = user.id!!, partyId = party1.id!!),
            user = user,
            party = party1
        )
        val partyMember2 = PartyMember(
            id = PartyMemberId(userId = user.id!!, partyId = party2.id!!),
            user = user,
            party = party2
        )
        val partyMembers = listOf(partyMember1, partyMember2)

        `when`(partyMemberRepository.findByUser(user)).thenReturn(partyMembers)

        // When
        val result = partyService.getPartiesForUser(user)

        // Then
        assertEquals(2, result.size)
        assertEquals("Party 1", result[0].name)
        assertEquals("Party 2", result[1].name)
        verify(partyMemberRepository).findByUser(user)
    }

    @Test
    fun `getPartyMembers should return members of the given party`() {
        // Given
        val party = Party(id = 1L, name = "Test Party")
        val user1 = User(id = 1L, name = "User 1")
        val user2 = User(id = 2L, name = "User 2")
        val partyMember1 = PartyMember(
            id = PartyMemberId(userId = user1.id!!, partyId = party.id!!),
            user = user1,
            party = party
        )
        val partyMember2 = PartyMember(
            id = PartyMemberId(userId = user2.id!!, partyId = party.id!!),
            user = user2,
            party = party
        )
        val otherParty = Party(id = 2L, name = "Other Party")
        val otherPartyMember = PartyMember(
            id = PartyMemberId(userId = user1.id!!, partyId = otherParty.id!!),
            user = user1,
            party = otherParty
        )
        val allPartyMembers = listOf(partyMember1, partyMember2, otherPartyMember)

        `when`(partyMemberRepository.findAll()).thenReturn(allPartyMembers)

        // When
        val result = partyService.getPartyMembers(party)

        // Then
        assertEquals(2, result.size)
        assertEquals("User 1", result[0].name)
        assertEquals("User 2", result[1].name)
        verify(partyMemberRepository).findAll()
    }
}
