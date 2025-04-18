package flab.kr.catchfood.party.core.application

import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.core.domain.PartyMember
import flab.kr.catchfood.party.core.domain.PartyMemberId
import flab.kr.catchfood.party.core.domain.PartyMemberRepository
import flab.kr.catchfood.party.core.domain.PartyRepository
import flab.kr.catchfood.user.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PartyService(
    private val partyRepository: PartyRepository,
    private val partyMemberRepository: PartyMemberRepository
) {

    @Transactional
    fun createParty(partyName: String, creator: User): Party {
        val party = Party(name = partyName)
        val savedParty = partyRepository.save(party)
        
        val partyMember = PartyMember(
            id = PartyMemberId(userId = creator.id!!, partyId = savedParty.id!!),
            user = creator,
            party = savedParty
        )
        partyMemberRepository.save(partyMember)
        
        return savedParty
    }

    @Transactional(readOnly = true)
    fun getPartiesForUser(user: User): List<Party> {
        val partyMembers = partyMemberRepository.findByUser(user)
        return partyMembers.map { it.party }
    }

    @Transactional(readOnly = true)
    fun getPartyMembers(party: Party): List<User> {
        val partyMembers = partyMemberRepository.findAll()
            .filter { it.party.id == party.id }
        return partyMembers.map { it.user }
    }
}
