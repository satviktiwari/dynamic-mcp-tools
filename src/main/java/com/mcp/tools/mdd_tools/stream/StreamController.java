package com.mcp.tools.mdd_tools.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class StreamController {
    private final StreamEventBus bus;

    public StreamController(StreamEventBus bus) {
        this.bus = bus;
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<String> stream() {
        return bus.stream();
    }
}

