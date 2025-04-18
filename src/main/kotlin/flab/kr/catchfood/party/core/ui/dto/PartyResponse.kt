package flab.kr.catchfood.party.core.ui.dto

data class PartyResponse(
    val id: Long,
    val name: String,
    val members: List<String>
)
