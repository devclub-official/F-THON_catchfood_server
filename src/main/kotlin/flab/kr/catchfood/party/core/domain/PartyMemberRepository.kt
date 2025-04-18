package flab.kr.catchfood.party.core.domain

import flab.kr.catchfood.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PartyMemberRepository : JpaRepository<PartyMember, PartyMemberId> {
    fun findByUser(user: User): List<PartyMember>
}
