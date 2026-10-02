package de.shurablack.core.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * discord-webhooks is built against okhttp 4, but runs on the okhttp 5 that JDA brings.
 * The interceptor answers locally, nothing is sent to Discord.
 */
class WebhookOkHttpTest {

    @Test
    void webhookClientSendsThroughJdaOkHttp() throws Exception {
        AtomicReference<String> requested = new AtomicReference<>();
        OkHttpClient http = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    requested.set(chain.request().url().encodedPath());
                    return new Response.Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(204)
                            .message("No Content")
                            .body(ResponseBody.create(new byte[0], null))
                            .build();
                })
                .build();

        try (WebhookClient client = new WebhookClientBuilder(123L, "token").setHttpClient(http).setWait(false).build()) {
            client.send("test").get(5, TimeUnit.SECONDS);
        }

        assertTrue(requested.get().endsWith("/webhooks/123/token"), requested.get());
    }
}
