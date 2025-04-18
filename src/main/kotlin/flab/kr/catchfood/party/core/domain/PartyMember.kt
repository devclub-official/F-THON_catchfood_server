package flab.kr.catchfood.party.core.domain

import flab.kr.catchfood.user.domain.User
import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "`PartyMember`")
class PartyMember(
    @EmbeddedId
    val id: PartyMemberId,

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    val user: User,

    @MapsId("partyId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTY_ID", nullable = false)
    val party: Party
)

@Embeddable
data class PartyMemberId(
    @Column(name = "USER_ID")
    val userId: Long,

    @Column(name = "PARTY_ID")
    val partyId: Long
) : Serializable
