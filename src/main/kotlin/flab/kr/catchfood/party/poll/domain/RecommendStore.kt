package flab.kr.catchfood.party.poll.domain

import flab.kr.catchfood.store.domain.Store
import jakarta.persistence.*

@Entity
@Table(name = "RecommendStore")
class RecommendStore(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POLL_ID", nullable = false)
    val poll: MealPoll,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID", nullable = false)
    val store: Store
)
