package com.mcp.tools.mdd_tools.controller;

import com.mcp.tools.mdd_tools.model.ApiToolMetadata;
import com.mcp.tools.mdd_tools.service.ToolRegistry;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;

@RestController
public class ToolSseController {

    private final ToolRegistry toolRegistry;

    public ToolSseController(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @GetMapping(path = "/sse/tools", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTools() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        new Thread(() -> {
            try {
                List<ApiToolMetadata> tools = toolRegistry.getAllTools();
                for (ApiToolMetadata tool : tools) {
                    emitter.send(SseEmitter.event()
                            .name("tool")
                            .data(tool));
                }
                emitter.send(SseEmitter.event().name("end").data("done"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}
