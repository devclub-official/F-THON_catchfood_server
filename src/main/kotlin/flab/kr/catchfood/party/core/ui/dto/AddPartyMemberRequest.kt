package flab.kr.catchfood.party.core.ui.dto

import jakarta.validation.constraints.NotBlank

data class AddPartyMemberRequest(
    @field:NotBlank(message = "Member name is required")
    val memberName: String
)
