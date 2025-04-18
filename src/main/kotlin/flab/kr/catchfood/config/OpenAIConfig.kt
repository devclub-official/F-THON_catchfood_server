package flab.kr.catchfood.config

import com.theokanning.openai.service.OpenAiService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class OpenAIConfig {

    @Value("\${openai.api-key}")
    private lateinit var apiKey: String

    @Bean
    fun openAiService(): OpenAiService {
        return OpenAiService(apiKey, Duration.ofSeconds(60))
    }
}
