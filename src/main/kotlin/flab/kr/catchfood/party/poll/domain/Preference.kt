package flab.kr.catchfood.party.poll.domain

import flab.kr.catchfood.user.domain.User
import jakarta.persistence.*

@Entity
@Table(name = "Preference")
class Preference(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POLL_ID", nullable = false)
    val poll: MealPoll,

    @Column(name = "CONTENT", nullable = false, length = 200)
    val content: String
)
