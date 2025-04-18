package flab.kr.catchfood.user.ui

import com.fasterxml.jackson.databind.ObjectMapper
import flab.kr.catchfood.common.ui.dto.ApiStatus
import flab.kr.catchfood.user.application.UserService
import flab.kr.catchfood.user.application.dto.UserPreferencesDto
import flab.kr.catchfood.user.domain.User
import flab.kr.catchfood.user.ui.dto.SignupRequest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userService: UserService

    // No setup needed

    @TestConfiguration
    class TestConfig {
        @Bean
        fun userService(): UserService = mock(UserService::class.java)
    }

    @Test
    fun `signup should return success response when user is created successfully`() {
        // Given
        val request = SignupRequest("testUser")
        val user = User(1L, "testUser")

        `when`(userService.signup(request)).thenReturn(user)

        // When & Then
        mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value(ApiStatus.SUCCESS.name))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())

        verify(userService).signup(request)
    }

    @Test
    fun `signup should return bad request when request is invalid`() {
        // Given
        val request = SignupRequest("")

        // When & Then
        mockMvc.perform(
            post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(ApiStatus.FAIL.name))
            .andExpect(jsonPath("$.message").isNotEmpty)

        // No need to verify as the request doesn't reach the service
    }

    @Test
    fun `getAllMembers should return list of users`() {
        // Given
        val users = listOf(
            User(1L, "user1"),
            User(2L, "user2")
        )

        `when`(userService.getAllUsers()).thenReturn(users)

        // When & Then
        mockMvc.perform(get("/members"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(ApiStatus.SUCCESS.name))
            .andExpect(jsonPath("$.data[0].name").value("user1"))
            .andExpect(jsonPath("$.data[1].name").value("user2"))

        verify(userService).getAllUsers()
    }

    @Test
    fun `getMyPreferences should return user preferences`() {
        // Given
        val userName = "testUser"
        val user = User(1L, userName)
        val preferences = UserPreferencesDto(
            likes = "likes",
            dislikes = "dislikes",
            etc = "etc"
        )

        `when`(userService.getUserByName(userName)).thenReturn(user)
        `when`(userService.getUserPreferences(user)).thenReturn(preferences)

        // When & Then
        mockMvc.perform(
            get("/my/preferences")
                .header("X-User-Name", userName)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(ApiStatus.SUCCESS.name))
            .andExpect(jsonPath("$.data.likes").value("likes"))
            .andExpect(jsonPath("$.data.dislikes").value("dislikes"))
            .andExpect(jsonPath("$.data.etc").value("etc"))

        // getUserByName is called by the resolver or by getUserPreferences, but not both
        verify(userService, times(1)).getUserByName(userName)
        verify(userService).getUserPreferences(user)
    }

    @Test
    fun `getMyPreferences should return error when X-User-Name header is missing`() {
        // When & Then
        mockMvc.perform(
            get("/my/preferences")
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(ApiStatus.FAIL.name))
            .andExpect(jsonPath("$.message").isNotEmpty)

        // No need to verify as the request doesn't reach the service
    }

    @Test
    fun `updateMyPreferences should update user preferences and return success`() {
        // Given
        val userName = "testUser"
        val user = User(1L, userName)
        val preferences = UserPreferencesDto(
            likes = "new likes",
            dislikes = "new dislikes",
            etc = "new etc"
        )
        val updatedUser = User(
            id = 1L,
            name = userName,
            prefLikes = "new likes",
            prefDislikes = "new dislikes",
            prefEtc = "new etc"
        )

        `when`(userService.getUserByName(userName)).thenReturn(user)
        `when`(userService.updateUserPreferences(user, preferences)).thenReturn(updatedUser)

        // When & Then
        mockMvc.perform(
            put("/my/preferences")
                .header("X-User-Name", userName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(preferences))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(ApiStatus.SUCCESS.name))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.message").doesNotExist())

        // getUserByName is called by the resolver and then by updateUserPreferences
        verify(userService, times(2)).getUserByName(userName)
        verify(userService).updateUserPreferences(user, preferences)
    }

    @Test
    fun `updateMyPreferences should return error when X-User-Name header is missing`() {
        // Given
        val preferences = UserPreferencesDto(
            likes = "new likes",
            dislikes = "new dislikes",
            etc = "new etc"
        )

        // When & Then
        mockMvc.perform(
            put("/my/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(preferences))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(ApiStatus.FAIL.name))
            .andExpect(jsonPath("$.message").isNotEmpty)

        // No need to verify as the request doesn't reach the service
    }

    @Test
    fun `updateMyPreferences should return error when request body is invalid`() {
        // Given
        val userName = "testUser"
        val user = User(1L, userName)
        val invalidJson = "{ invalid json }"

        `when`(userService.getUserByName(userName)).thenReturn(user)

        // When & Then
        mockMvc.perform(
            put("/my/preferences")
                .header("X-User-Name", userName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(ApiStatus.FAIL.name))
            .andExpect(jsonPath("$.message").isNotEmpty)

        // The resolver calls getUserByName multiple times due to error handling
        verify(userService, times(3)).getUserByName(userName)
        // No need to verify updateUserPreferences as the request doesn't reach that method
    }

    @Test
    fun `updateMyPreferences should return error when user is not found`() {
        // Given
        val userName = "nonExistentUser"
        val preferences = UserPreferencesDto(
            likes = "new likes",
            dislikes = "new dislikes",
            etc = "new etc"
        )

        `when`(userService.getUserByName(userName)).thenThrow(IllegalArgumentException("User not found"))

        // When & Then
        mockMvc.perform(
            put("/my/preferences")
                .header("X-User-Name", userName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(preferences))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(ApiStatus.FAIL.name))
            .andExpect(jsonPath("$.message").isNotEmpty)

        verify(userService).getUserByName(userName)
        // No need to verify updateUserPreferences as the request doesn't reach that method
    }
}
