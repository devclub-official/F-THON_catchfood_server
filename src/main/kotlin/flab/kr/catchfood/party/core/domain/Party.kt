package flab.kr.catchfood.party.core.domain

import jakarta.persistence.*

@Entity
@Table(name = "`Party`")
class Party(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "NAME", length = 100)
    val name: String
)
