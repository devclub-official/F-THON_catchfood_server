package flab.kr.catchfood.user.infrastructure

import flab.kr.catchfood.user.domain.User
import flab.kr.catchfood.user.domain.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `save should persist user to database`() {
        // Given
        val user = User(name = "testUser")

        // When
        val savedUser = userRepository.save(user)

        // Then
        assertNotNull(savedUser.id)
        assertEquals("testUser", savedUser.name)

        // Verify it's in the database
        val foundUser = entityManager.find(User::class.java, savedUser.id)
        assertNotNull(foundUser)
        assertEquals("testUser", foundUser.name)
    }

    @Test
    fun `findById should return user when exists`() {
        // Given
        val user = User(name = "testUser")
        val persistedUser = entityManager.persist(user)
        entityManager.flush()

        // When
        val foundUser = userRepository.findById(persistedUser.id!!)

        // Then
        assertTrue(foundUser.isPresent)
        assertEquals("testUser", foundUser.get().name)
    }

    @Test
    fun `findById should return empty when user does not exist`() {
        // When
        val foundUser = userRepository.findById(999L)

        // Then
        assertFalse(foundUser.isPresent)
    }

    @Test
    fun `existsByName should return true when user with name exists`() {
        // Given
        val user = User(name = "existingUser")
        entityManager.persist(user)
        entityManager.flush()

        // When
        val exists = userRepository.existsByName("existingUser")

        // Then
        assertTrue(exists)
    }

    @Test
    fun `existsByName should return false when user with name does not exist`() {
        // When
        val exists = userRepository.existsByName("nonExistingUser")

        // Then
        assertFalse(exists)
    }

    @Test
    fun `findAll should return all users`() {
        // Given
        val user1 = User(name = "user1")
        val user2 = User(name = "user2")
        entityManager.persist(user1)
        entityManager.persist(user2)
        entityManager.flush()

        // When
        val users = userRepository.findAll()

        // Then
        assertEquals(2, users.size)
        assertTrue(users.any { it.name == "user1" })
        assertTrue(users.any { it.name == "user2" })
    }

    @Test
    fun `findByName should return user when exists`() {
        // Given
        val user = User(name = "testUser")
        entityManager.persist(user)
        entityManager.flush()

        // When
        val foundUser = userRepository.findByName("testUser")

        // Then
        assertNotNull(foundUser)
        assertEquals("testUser", foundUser?.name)
    }

    @Test
    fun `findByName should return null when user does not exist`() {
        // When
        val foundUser = userRepository.findByName("nonExistingUser")

        // Then
        assertNull(foundUser)
    }
}
