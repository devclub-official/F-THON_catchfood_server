package flab.kr.catchfood.party.poll.ui

import com.fasterxml.jackson.databind.ObjectMapper
import flab.kr.catchfood.party.core.application.PartyService
import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.poll.application.MealPollService
import flab.kr.catchfood.party.poll.application.PartyPollsDto
import flab.kr.catchfood.party.poll.application.PollDetailsDto
import flab.kr.catchfood.party.poll.application.RecommendedStoreDto
import flab.kr.catchfood.party.poll.application.dto.PreferenceRequestDto
import flab.kr.catchfood.party.poll.domain.MealPoll
import flab.kr.catchfood.party.poll.domain.MealPollRepository
import flab.kr.catchfood.party.poll.domain.MealPollStatus
import flab.kr.catchfood.party.poll.domain.PreferenceRepository
import flab.kr.catchfood.party.poll.domain.RecommendStoreRepository
import flab.kr.catchfood.party.poll.domain.VoteRepository
import flab.kr.catchfood.store.application.dto.RepresentativeMenuDto
import flab.kr.catchfood.user.domain.UserRepository
import flab.kr.catchfood.user.application.UserService
import flab.kr.catchfood.user.domain.User
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
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

    @Test
    fun `GET parties-id-polls-id should return poll details`() {
        // Given
        val partyId = 1L
        val pollId = 1L
        val user = User(id = 1L, name = "김진홍")

        val recommendedStore = RecommendedStoreDto(
            id = 1L,
            storeName = "홍콩반점",
            representativeMenu = RepresentativeMenuDto(
                name = "짬뽕",
                imageUrl = ""
            ),
            category = "중식",
            distanceInMinutesByWalk = 10,
            votedMembers = listOf("김진홍"),
            isVotedByMe = true
        )

        val pollDetailsDto = PollDetailsDto(
            status = "IN_PROGRESS",
            preferences = mapOf(
                "김진홍" to "매운 거",
                "정종찬" to "한식 아무거나"
            ),
            recommendedStores = listOf(recommendedStore)
        )

        `when`(userService.getUserByName(user.name)).thenReturn(user)
        `when`(mealPollService.getPollDetails(partyId, pollId, user.name)).thenReturn(pollDetailsDto)

        // When & Then
        mockMvc.perform(
            get("/parties/$partyId/polls/$pollId")
                .header("X-User-Name", user.name)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.data.preferences.김진홍").value("매운 거"))
            .andExpect(jsonPath("$.data.preferences.정종찬").value("한식 아무거나"))
            .andExpect(jsonPath("$.data.recommendedStores[0].id").value(1))
            .andExpect(jsonPath("$.data.recommendedStores[0].storeName").value("홍콩반점"))
            .andExpect(jsonPath("$.data.recommendedStores[0].representativeMenu.name").value("짬뽕"))
            .andExpect(jsonPath("$.data.recommendedStores[0].representativeMenu.imageUrl").value(""))
            .andExpect(jsonPath("$.data.recommendedStores[0].category").value("중식"))
            .andExpect(jsonPath("$.data.recommendedStores[0].distanceInMinutesByWalk").value(10))
            .andExpect(jsonPath("$.data.recommendedStores[0].votedMembers[0]").value("김진홍"))
            .andExpect(jsonPath("$.data.recommendedStores[0].isVotedByMe").value(true))
            .andExpect(jsonPath("$.message").doesNotExist())

        verify(mealPollService).getPollDetails(partyId, pollId, user.name)
    }

    @Test
    fun `GET parties-id-polls-id should return unauthorized when user header is missing`() {
        // Given
        val partyId = 1L
        val pollId = 1L

        // When & Then
        mockMvc.perform(get("/parties/$partyId/polls/$pollId"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }

    @Test
    fun `POST parties-id-polls-id-preferences should add preference and return success`() {
        // Given
        val partyId = 1L
        val pollId = 1L
        val user = User(id = 1L, name = "testUser")
        val preferenceRequest = PreferenceRequestDto(preference = "매운 거")

        `when`(userService.getUserByName(user.name)).thenReturn(user)

        // When & Then
        mockMvc.perform(
            post("/parties/$partyId/polls/$pollId/preferences")
                .header("X-User-Name", user.name)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(preferenceRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())

        verify(mealPollService).addPreference(partyId, pollId, user.name, preferenceRequest)
    }

    @Test
    fun `POST parties-id-polls-id-preferences should return unauthorized when user header is missing`() {
        // Given
        val partyId = 1L
        val pollId = 1L
        val preferenceRequest = PreferenceRequestDto(preference = "매운 거")

        // When & Then
        mockMvc.perform(
            post("/parties/$partyId/polls/$pollId/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(preferenceRequest))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.message").isNotEmpty)
    }
}
