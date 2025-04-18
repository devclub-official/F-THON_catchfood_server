package flab.kr.catchfood.party.poll.domain

import flab.kr.catchfood.user.domain.User
import jakarta.persistence.*

@Entity
@Table(name = "Vote")
class Vote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POLL_ID", nullable = false)
    val poll: MealPoll,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID", nullable = false)
    val store: RecommendStore,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    val user: User
)
