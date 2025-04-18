package flab.kr.catchfood.party.poll.ui

import flab.kr.catchfood.common.ui.dto.ApiResponse
import flab.kr.catchfood.party.poll.application.MealPollService
import flab.kr.catchfood.party.poll.application.PartyPollsDto
import flab.kr.catchfood.user.domain.User
import flab.kr.catchfood.user.ui.annotation.CurrentUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class MealPollController(private val mealPollService: MealPollService) {

    @PostMapping("/parties/{id}/polls")
    @ResponseStatus(HttpStatus.CREATED)
    fun createPoll(
        @PathVariable id: Long,
        @CurrentUser user: User
    ): ApiResponse<Void> {
        mealPollService.createPoll(id)
        return ApiResponse.success()
    }

    @GetMapping("/parties/{id}/polls")
    fun getPartyPolls(
        @PathVariable id: Long,
        @CurrentUser user: User
    ): ApiResponse<List<PartyPollsDto>> {
        val polls = mealPollService.getPartyPolls(id)
        return ApiResponse.success(listOf(polls))
    }
}
