package flab.kr.catchfood.party.core.ui

import com.fasterxml.jackson.databind.ObjectMapper
import flab.kr.catchfood.party.core.application.PartyService
import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.core.domain.PartyMember
import flab.kr.catchfood.party.core.domain.PartyMemberId
import flab.kr.catchfood.party.core.ui.dto.AddPartyMemberRequest
import flab.kr.catchfood.party.core.ui.dto.CreatePartyRequest
import flab.kr.catchfood.user.application.UserService
import flab.kr.catchfood.user.domain.User
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class PartyControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var partyService: PartyService

    @Autowired
    private lateinit var userService: UserService

    @TestConfiguration
    class TestConfig {
        @Bean
        fun partyService(): PartyService = mock(PartyService::class.java)

        @Bean
        fun userService(): UserService = mock(UserService::class.java)
    }

    @Test
    fun `POST parties should create a party and return success`() {
        // Given
        val user = User(id = 1L, name = "testUser")
        val request = CreatePartyRequest(partyName = "Test Party")
        val party = Party(id = 1L, name = request.partyName)

        `when`(partyService.createParty(request.partyName, user)).thenReturn(party)
        `when`(userService.getUserByName(user.name)).thenReturn(user)

        // When & Then
        mockMvc.perform(
            post("/parties")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Name", user.name)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())

        verify(partyService).createParty(request.partyName, user)
    }

    @Test
    fun `POST parties should return bad request when request is invalid`() {
        // Given
        val user = User(id = 1L, name = "testUser")
        val request = CreatePartyRequest(partyName = "")

        `when`(userService.getUserByName(user.name)).thenReturn(user)

        // When & Then
        mockMvc.perform(
            post("/parties")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Name", user.name)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }

    @Test
    fun `POST parties should return unauthorized when user header is missing`() {
        // Given
        val request = CreatePartyRequest(partyName = "Test Party")

        // When & Then
        mockMvc.perform(
            post("/parties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }

    @Test
    fun `GET parties should return user's parties`() {
        // Given
        val user = User(id = 1L, name = "testUser")
        val party = Party(id = 1L, name = "Test Party")
        val members = listOf(user)

        `when`(userService.getUserByName(user.name)).thenReturn(user)
        `when`(partyService.getPartiesForUser(user)).thenReturn(listOf(party))
        `when`(partyService.getPartyMembers(party)).thenReturn(members)

        // When & Then
        mockMvc.perform(
            get("/parties")
                .header("X-User-Name", user.name)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].id").value(party.id))
            .andExpect(jsonPath("$.data[0].name").value(party.name))
            .andExpect(jsonPath("$.data[0].members[0]").value(user.name))
            .andExpect(jsonPath("$.message").doesNotExist())

        verify(partyService).getPartiesForUser(user)
        verify(partyService).getPartyMembers(party)
    }

    @Test
    fun `GET parties should return unauthorized when user header is missing`() {
        // When & Then
        mockMvc.perform(get("/parties"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }

    @Test
    fun `GET parties-id-members should return members of a party`() {
        // Given
        val partyId = 1L
        val user1 = User(id = 1L, name = "user1")
        val user2 = User(id = 2L, name = "user2")
        val members = listOf(user1, user2)

        `when`(partyService.getPartyMembersById(partyId)).thenReturn(members)

        // When & Then
        mockMvc.perform(get("/parties/$partyId/members"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0]").value(user1.name))
            .andExpect(jsonPath("$.data[1]").value(user2.name))
            .andExpect(jsonPath("$.message").doesNotExist())

        verify(partyService).getPartyMembersById(partyId)
    }

    @Test
    fun `GET parties-id-members should return bad request when party does not exist`() {
        // Given
        val partyId = 999L

        `when`(partyService.getPartyMembersById(partyId))
            .thenThrow(IllegalArgumentException("Party with id $partyId not found"))

        // When & Then
        mockMvc.perform(get("/parties/$partyId/members"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }

    @Test
    fun `POST parties-id-members should add a member to a party`() {
        // Given
        val partyId = 1L
        val request = AddPartyMemberRequest(memberName = "newMember")
        val party = Party(id = partyId, name = "Test Party")
        val user = User(id = 2L, name = request.memberName)
        val partyMember = PartyMember(
            id = PartyMemberId(userId = user.id!!, partyId = partyId),
            user = user,
            party = party
        )

        `when`(partyService.addMemberToParty(partyId, request.memberName)).thenReturn(partyMember)

        // When & Then
        mockMvc.perform(
            post("/parties/$partyId/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())

        verify(partyService).addMemberToParty(partyId, request.memberName)
    }

    @Test
    fun `POST parties-id-members should return bad request when request is invalid`() {
        // Given
        val partyId = 1L
        val request = AddPartyMemberRequest(memberName = "")

        // When & Then
        mockMvc.perform(
            post("/parties/$partyId/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }

    @Test
    fun `POST parties-id-members should return bad request when party does not exist`() {
        // Given
        val partyId = 999L
        val request = AddPartyMemberRequest(memberName = "newMember")

        `when`(partyService.addMemberToParty(partyId, request.memberName))
            .thenThrow(IllegalArgumentException("Party with id $partyId not found"))

        // When & Then
        mockMvc.perform(
            post("/parties/$partyId/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }

    @Test
    fun `POST parties-id-members should return bad request when user does not exist`() {
        // Given
        val partyId = 1L
        val request = AddPartyMemberRequest(memberName = "nonExistentUser")

        `when`(partyService.addMemberToParty(partyId, request.memberName))
            .thenThrow(IllegalArgumentException("User with name ${request.memberName} not found"))

        // When & Then
        mockMvc.perform(
            post("/parties/$partyId/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }

    @Test
    fun `POST parties-id-members should return bad request when user is already a member`() {
        // Given
        val partyId = 1L
        val request = AddPartyMemberRequest(memberName = "existingMember")

        `when`(partyService.addMemberToParty(partyId, request.memberName))
            .thenThrow(IllegalArgumentException("User ${request.memberName} is already a member of this party"))

        // When & Then
        mockMvc.perform(
            post("/parties/$partyId/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }
}
