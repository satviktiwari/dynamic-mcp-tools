package com.mcp.tools.mdd_tools.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class StreamEventBus {
    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(Object event) {
        try {
            String json = new ObjectMapper().writeValueAsString(event);
            sink.tryEmitNext(json + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Flux<String> stream() {
        return sink.asFlux();
    }
}

