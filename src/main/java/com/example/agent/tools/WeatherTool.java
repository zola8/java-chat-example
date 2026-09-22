package com.example.agent.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool implements AgentTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherTool.class);

    @Tool(description = "Get the current weather for a specific city.")
    public String getWeather(String city) {
        LOGGER.debug("WeatherTool called with: {}", city);
        return "It is 22°C and sunny in " + city;
    }

}
