package flab.kr.catchfood.party.poll.domain

import flab.kr.catchfood.party.core.domain.Party
import jakarta.persistence.*

@Entity
@Table(name = "MealPoll")
class MealPoll(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTY_ID", nullable = false)
    val party: Party,

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    var status: MealPollStatus = MealPollStatus.IN_PROGRESS
)

enum class MealPollStatus {
    IN_PROGRESS, DONE
}
