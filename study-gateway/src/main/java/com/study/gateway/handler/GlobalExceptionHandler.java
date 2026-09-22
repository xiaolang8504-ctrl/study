package com.study.gateway.handler;

import com.google.gson.JsonObject;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关异常通用处理器
 */
@Order(-1)
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(((ResponseStatusException) ex).getStatus());
        }

        JsonObject message = new JsonObject();
        message.addProperty("code", 100503);
        message.addProperty("msg", "网关异常");
        message.addProperty("data", ex.getMessage());

        DataBuffer buffer = response.bufferFactory().wrap(
                message.toString().getBytes(StandardCharsets.UTF_8)
        );
        return response.writeWith(Mono.just(buffer));
    }
}
