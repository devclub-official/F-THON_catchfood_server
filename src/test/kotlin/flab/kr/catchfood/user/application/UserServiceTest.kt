package flab.kr.catchfood.user.application

import flab.kr.catchfood.user.domain.User
import flab.kr.catchfood.user.infrastructure.UserRepository
import flab.kr.catchfood.user.ui.dto.SignupRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun `signup should create a new user when name is unique`() {
        // Given
        val request = SignupRequest("testUser")
        val savedUser = User(1L, "testUser")
        
        `when`(userRepository.existsByName("testUser")).thenReturn(false)
        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)
        
        // When
        val result = userService.signup(request)
        
        // Then
        assertEquals("testUser", result.name)
        assertEquals(1L, result.id)
        verify(userRepository).existsByName("testUser")
        verify(userRepository).save(any(User::class.java))
    }
    
    @Test
    fun `signup should throw exception when user with same name already exists`() {
        // Given
        val request = SignupRequest("existingUser")
        
        `when`(userRepository.existsByName("existingUser")).thenReturn(true)
        
        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            userService.signup(request)
        }
        
        assertEquals("User with name existingUser already exists", exception.message)
        verify(userRepository).existsByName("existingUser")
        verify(userRepository, never()).save(any(User::class.java))
    }
    
    @Test
    fun `getAllUsers should return all users from repository`() {
        // Given
        val users = listOf(
            User(1L, "user1"),
            User(2L, "user2")
        )
        
        `when`(userRepository.findAll()).thenReturn(users)
        
        // When
        val result = userService.getAllUsers()
        
        // Then
        assertEquals(2, result.size)
        assertEquals("user1", result[0].name)
        assertEquals("user2", result[1].name)
        verify(userRepository).findAll()
    }
}
