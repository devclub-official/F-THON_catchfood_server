package flab.kr.catchfood.party.core.ui.dto

import jakarta.validation.constraints.NotBlank

data class CreatePartyRequest(
    @field:NotBlank(message = "Party name is required")
    val partyName: String
)
