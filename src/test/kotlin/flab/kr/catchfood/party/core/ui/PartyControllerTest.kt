package flab.kr.catchfood.party.core.ui

import com.fasterxml.jackson.databind.ObjectMapper
import flab.kr.catchfood.party.core.application.PartyService
import flab.kr.catchfood.party.core.domain.Party
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
}
