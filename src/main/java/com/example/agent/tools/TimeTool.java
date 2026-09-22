package com.example.agent.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TimeTool implements AgentTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTool.class);

    @Tool(description = "Get the current date and time. Use this when the user asks about the time, date, or today.")
    public String getCurrentTime() {
        LOGGER.debug("TimeTool called");
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
