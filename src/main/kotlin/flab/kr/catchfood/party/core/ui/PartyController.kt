package flab.kr.catchfood.party.core.ui

import flab.kr.catchfood.common.ui.dto.ApiResponse
import flab.kr.catchfood.party.core.application.PartyService
import flab.kr.catchfood.party.core.ui.dto.CreatePartyRequest
import flab.kr.catchfood.party.core.ui.dto.PartyResponse
import flab.kr.catchfood.user.domain.User
import flab.kr.catchfood.user.ui.annotation.CurrentUser
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class PartyController(private val partyService: PartyService) {

    @PostMapping("/parties")
    @ResponseStatus(HttpStatus.CREATED)
    fun createParty(
        @CurrentUser user: User,
        @Valid @RequestBody request: CreatePartyRequest
    ): ApiResponse<Void> {
        partyService.createParty(request.partyName, user)
        return ApiResponse.success()
    }

    @GetMapping("/parties")
    fun getMyParties(@CurrentUser user: User): ApiResponse<List<PartyResponse>> {
        val parties = partyService.getPartiesForUser(user)
        val response = parties.map { party ->
            val members = partyService.getPartyMembers(party)
            PartyResponse(
                id = party.id!!,
                name = party.name,
                members = members.map { it.name }
            )
        }
        return ApiResponse.success(response)
    }
}
