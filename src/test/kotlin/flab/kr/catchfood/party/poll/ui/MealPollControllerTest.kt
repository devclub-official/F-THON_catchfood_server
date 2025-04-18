package flab.kr.catchfood.party.poll.ui

import com.fasterxml.jackson.databind.ObjectMapper
import flab.kr.catchfood.party.core.application.PartyService
import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.poll.application.MealPollService
import flab.kr.catchfood.party.poll.application.PartyPollsDto
import flab.kr.catchfood.party.poll.domain.MealPoll
import flab.kr.catchfood.party.poll.domain.MealPollStatus
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
class MealPollControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mealPollService: MealPollService

    @Autowired
    private lateinit var userService: UserService

    @TestConfiguration
    class TestConfig {
        @Bean
        fun mealPollService(): MealPollService = mock(MealPollService::class.java)

        @Bean
        fun userService(): UserService = mock(UserService::class.java)
    }

    @Test
    fun `POST parties-id-polls should create a poll and return success`() {
        // Given
        val partyId = 1L
        val user = User(id = 1L, name = "testUser")
        val party = Party(id = partyId, name = "Test Party")
        val mealPoll = MealPoll(id = 1L, party = party)

        `when`(userService.getUserByName(user.name)).thenReturn(user)
        `when`(mealPollService.createPoll(partyId)).thenReturn(mealPoll)

        // When & Then
        mockMvc.perform(
            post("/parties/$partyId/polls")
                .header("X-User-Name", user.name)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())

        verify(mealPollService).createPoll(partyId)
    }

    @Test
    fun `POST parties-id-polls should return unauthorized when user header is missing`() {
        // Given
        val partyId = 1L

        // When & Then
        mockMvc.perform(post("/parties/$partyId/polls"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }

    @Test
    fun `GET parties-id-polls should return polls for a party`() {
        // Given
        val partyId = 1L
        val user = User(id = 1L, name = "testUser")
        val partyPollsDto = PartyPollsDto(
            done = listOf(1L, 2L),
            ongoing = 3L
        )

        `when`(userService.getUserByName(user.name)).thenReturn(user)
        `when`(mealPollService.getPartyPolls(partyId)).thenReturn(partyPollsDto)

        // When & Then
        mockMvc.perform(
            get("/parties/$partyId/polls")
                .header("X-User-Name", user.name)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].done[0]").value(1))
            .andExpect(jsonPath("$.data[0].done[1]").value(2))
            .andExpect(jsonPath("$.data[0].ongoing").value(3))
            .andExpect(jsonPath("$.message").doesNotExist())

        verify(mealPollService).getPartyPolls(partyId)
    }

    @Test
    fun `GET parties-id-polls should return unauthorized when user header is missing`() {
        // Given
        val partyId = 1L

        // When & Then
        mockMvc.perform(get("/parties/$partyId/polls"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }
}
