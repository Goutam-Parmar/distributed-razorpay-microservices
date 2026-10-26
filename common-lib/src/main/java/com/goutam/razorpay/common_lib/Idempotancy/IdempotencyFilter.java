package com.goutam.razorpay.common_lib.Idempotancy;

import com.goutam.razorpay.common_lib.context.MerchantContext;
import com.goutam.razorpay.common_lib.exception.IdempotencyConflictException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Set<String> GUARDED_METHODS =
            Set.of("POST", "PUT", "PATCH");

    private static final Duration IN_PROGRESS_TTL =
            Duration.ofSeconds(30);

    private static final Duration COMPLETED_TTL =
            Duration.ofHours(24);

    private static final String SEPARATOR = "|";

    private final MerchantContext merchantContext;
    private final IdempotencyStore idempotencyStore;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        // Only protect POST, PUT and PATCH requests
        if (!GUARDED_METHODS.contains(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Read idempotency key
        String rawKey = request.getHeader("X-Idempotency-Key");

        // No idempotency key -> normal request
        if (rawKey == null || rawKey.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        // Make key merchant-specific
        UUID merchantId = merchantContext.getMerchantId();

        String key = merchantId != null
                ? merchantId + ":" + rawKey
                : rawKey;

        /*
         * Try to claim the idempotency key.
         *
         * First request:
         *     setIfAbsent() -> true
         *
         * Duplicate request:
         *     setIfAbsent() -> false
         */
        boolean claimed =
                idempotencyStore.setIfAbsent(
                        key,
                        IN_PROGRESS_TTL
                );

        if (!claimed) {

            // Another request has already claimed this key
            Optional<String> existing =
                    idempotencyStore.get(key);

            /*
             * Request has already completed.
             * Replay the previously stored response.
             */
            if (existing.isPresent()
                    && !IdempotencyStore.IN_PROGRESS.equals(existing.get())) {

                replay(
                        request,
                        response,
                        existing.get()
                );

            } else {

                /*
                 * Request is still being processed by another thread.
                 */
                var ex =
                        new IdempotencyConflictException(
                                "A request with this idempotency key is in progress"
                        );

                handlerExceptionResolver.resolveException(
                        request,
                        response,
                        null,
                        ex
                );
            }

            // VERY IMPORTANT:
            // Do NOT continue into the first-request logic.
            return;
        }

        /*
         * First request using this idempotency key.
         *
         * We wrap the response so that we can capture
         * the response body before sending it to the client.
         */
        ContentCachingResponseWrapper wrapper =
                new ContentCachingResponseWrapper(response);

        try {

            chain.doFilter(
                    request,
                    wrapper
            );

        } finally {

            int status = wrapper.getStatus();

            byte[] bodyBytes =
                    wrapper.getContentAsByteArray();

            String body =
                    new String(
                            bodyBytes,
                            StandardCharsets.UTF_8
                    );

            /*
             * Successful response:
             *
             * Store:
             *
             * 200|{"orderId":"..."}
             *
             * or
             *
             * 201|{"id":"..."}
             */
            if (status < 400 && bodyBytes.length > 0) {

                String stored =
                        status
                                + SEPARATOR
                                + body;

                idempotencyStore.store(
                        key,
                        stored,
                        COMPLETED_TTL
                );

                log.debug(
                        "IdempotencyFilter: stored response status={} key={}",
                        status,
                        key
                );

            } else {

                /*
                 * Error response or empty response.
                 *
                 * Remove the IN_PROGRESS placeholder
                 * so the client can retry.
                 */
                idempotencyStore.delete(key);

                log.debug(
                        "IdempotencyFilter: deleted placeholder after error status={} key={}",
                        status,
                        key
                );
            }

            /*
             * VERY IMPORTANT:
             *
             * ContentCachingResponseWrapper buffers the response.
             * Without this line, the client may receive an empty body.
             */
            wrapper.copyBodyToResponse();
        }
    }

    /**
     * Replays the previously stored response from Redis.
     */
    private void replay(
            HttpServletRequest request,
            HttpServletResponse response,
            String stored
    ) throws IOException {

        int separatorIndex =
                stored.indexOf(SEPARATOR);

        /*
         * Stored value should look like:
         *
         * 200|{"orderId":"123"}
         */
        if (separatorIndex < 0) {

            var ex =
                    new IdempotencyConflictException(
                            "A request with this idempotency key is in progress"
                    );

            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    ex
            );

            return;
        }

        // Extract HTTP status
        int status =
                Integer.parseInt(
                        stored.substring(
                                0,
                                separatorIndex
                        )
                );

        // Extract JSON response body
        String body =
                stored.substring(
                        separatorIndex + 1
                );

        // Recreate the original response
        response.setStatus(status);

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.getOutputStream().write(
                body.getBytes(StandardCharsets.UTF_8)
        );
    }
}