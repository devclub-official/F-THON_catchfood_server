package flab.kr.catchfood.user.application

import flab.kr.catchfood.user.domain.User
import flab.kr.catchfood.user.domain.UserRepository
import flab.kr.catchfood.user.application.dto.UserPreferencesDto
import flab.kr.catchfood.user.ui.dto.SignupRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional
    fun signup(request: SignupRequest): User {
        if (userRepository.existsByName(request.name)) {
            throw IllegalArgumentException("User with name ${request.name} already exists")
        }

        val user = User(
            name = request.name,
        )

        return userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getUserByName(name: String): User {
        return userRepository.findByName(name)
            ?: throw IllegalArgumentException("User with name $name not found")
    }

    @Transactional(readOnly = true)
    fun getUserPreferences(user: User): UserPreferencesDto {
        return UserPreferencesDto(
            likes = user.prefLikes,
            dislikes = user.prefDislikes,
            etc = user.prefEtc
        )
    }

    @Transactional
    fun updateUserPreferences(user: User, preferences: UserPreferencesDto): User {
        user.prefLikes = preferences.likes
        user.prefDislikes = preferences.dislikes
        user.prefEtc = preferences.etc

        return userRepository.save(user)
    }
}
