package flab.kr.catchfood.party.core.application

import flab.kr.catchfood.party.core.domain.Party
import flab.kr.catchfood.party.core.domain.PartyMember
import flab.kr.catchfood.party.core.domain.PartyMemberId
import flab.kr.catchfood.party.core.domain.PartyMemberRepository
import flab.kr.catchfood.party.core.domain.PartyRepository
import flab.kr.catchfood.user.domain.User
import flab.kr.catchfood.user.application.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PartyService(
    private val partyRepository: PartyRepository,
    private val partyMemberRepository: PartyMemberRepository,
    private val userService: UserService
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

    @Transactional(readOnly = true)
    fun getParty(id: Long): Party {
        return partyRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Party with id $id not found") }
    }

    @Transactional(readOnly = true)
    fun getPartyMembersById(partyId: Long): List<User> {
        val party = getParty(partyId)
        return getPartyMembers(party)
    }

    @Transactional
    fun addMemberToParty(partyId: Long, memberName: String): PartyMember {
        val party = getParty(partyId)
        val user = userService.getUserByName(memberName)

        // Check if the user is already a member of the party
        if (isUserPartyMember(user, partyId)) {
            throw IllegalArgumentException("User $memberName is already a member of this party")
        }

        val partyMember = PartyMember(
            id = PartyMemberId(userId = user.id!!, partyId = partyId),
            user = user,
            party = party
        )

        return partyMemberRepository.save(partyMember)
    }

    @Transactional(readOnly = true)
    fun isUserPartyMember(user: User, partyId: Long): Boolean {
        return partyMemberRepository.findAll()
            .any { it.party.id == partyId && it.user.id == user.id }
    }
}
