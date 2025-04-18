package flab.kr.catchfood.user.application

import flab.kr.catchfood.user.domain.User
import flab.kr.catchfood.user.infrastructure.UserRepository
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
}
