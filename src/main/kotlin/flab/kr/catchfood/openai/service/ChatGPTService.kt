package flab.kr.catchfood.openai.service

import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.service.OpenAiService
import flab.kr.catchfood.party.poll.domain.Preference
import flab.kr.catchfood.store.domain.Store
import flab.kr.catchfood.user.domain.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatGPTService(private val openAiService: OpenAiService) {

    @Value("\${openai.model}")
    private lateinit var model: String

    @Value("\${openai.max-tokens}")
    private var maxTokens: Int = 2000

    @Value("\${openai.temperature}")
    private var temperature: Double = 0.7

    /**
     * Recommends stores based on store information, party members' preferences, and poll preferences.
     *
     * @param stores List of all available stores
     * @param partyMembers List of party members
     * @param preferences List of preferences registered in the poll
     * @return List of recommended store IDs
     */
    fun recommendStores(
        stores: List<Store>,
        partyMembers: List<User>,
        preferences: List<Preference>
    ): List<Long> {
        // Prepare the prompt for ChatGPT
        val prompt = buildPrompt(stores, partyMembers, preferences)

        // Create the chat completion request
        val chatCompletionRequest = ChatCompletionRequest.builder()
            .model(model)
            .messages(listOf(ChatMessage("user", prompt)))
            .maxTokens(maxTokens)
            .temperature(temperature)
            .build()

        // Send the request to ChatGPT
        val response = openAiService.createChatCompletion(chatCompletionRequest)

        // Parse the response to get the recommended store IDs
        return parseResponse(response.choices[0].message.content)
    }

    /**
     * Builds the prompt for ChatGPT.
     */
    private fun buildPrompt(
        stores: List<Store>,
        partyMembers: List<User>,
        preferences: List<Preference>
    ): String {
        val sb = StringBuilder()

        // Introduction
        sb.append("You are a restaurant recommendation system. ")
        sb.append("Based on the following information, recommend the most suitable restaurants for a group of people. ")
        sb.append("Please provide your recommendations in a JSON format with an array of store IDs only.\n\n")

        // Store information
        sb.append("Available Restaurants:\n")
        stores.forEach { store ->
            sb.append("- ID: ${store.id}, Name: ${store.name}, Category: ${store.category}, ")
            sb.append("Distance: ${store.distanceInMinutesByWalk} minutes by walk, ")
            sb.append("Rating: ${store.ratingStars}, ")
            sb.append("Address: ${store.address}\n")
            
            // Add menu information
            sb.append("  Menus:\n")
            store.menus.forEach { menu ->
                sb.append("    - ${menu.name}: ${menu.price} won\n")
            }
            sb.append("\n")
        }

        // Party members' preferences
        sb.append("Party Members' General Preferences:\n")
        partyMembers.forEach { user ->
            sb.append("- ${user.name}:\n")
            sb.append("  Likes: ${user.prefLikes ?: "Not specified"}\n")
            sb.append("  Dislikes: ${user.prefDislikes ?: "Not specified"}\n")
            sb.append("  Other preferences: ${user.prefEtc ?: "Not specified"}\n")
        }
        sb.append("\n")

        // Poll-specific preferences
        sb.append("Poll-specific Preferences:\n")
        preferences.forEach { preference ->
            sb.append("- ${preference.user.name}: ${preference.content}\n")
        }
        sb.append("\n")

        // Instructions for response format
        sb.append("Please recommend the most suitable restaurants for this group. ")
        sb.append("Consider their preferences, the distance, and the restaurant ratings. ")
        sb.append("Provide your response in the following JSON format:\n")
        sb.append("{\n  \"recommendedStoreIds\": [id1, id2, id3, ...]\n}")

        return sb.toString()
    }

    /**
     * Parses the response from ChatGPT to extract the recommended store IDs.
     */
    private fun parseResponse(response: String): List<Long> {
        // Extract the JSON part from the response
        val jsonRegex = """\{[\s\S]*"recommendedStoreIds"\s*:\s*\[([\s\S]*?)\][\s\S]*\}""".toRegex()
        val matchResult = jsonRegex.find(response)
        
        if (matchResult != null) {
            val idsString = matchResult.groupValues[1]
            return idsString.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { it.toLongOrNull() }
                .filterNotNull()
        }
        
        // Fallback: try to find any numbers in the response
        val numberRegex = """\d+""".toRegex()
        return numberRegex.findAll(response)
            .map { it.value.toLongOrNull() }
            .filterNotNull()
            .toList()
    }
}
