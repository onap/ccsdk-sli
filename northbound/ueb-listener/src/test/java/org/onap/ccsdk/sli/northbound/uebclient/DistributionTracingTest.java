/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.northbound.uebclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.INotificationData;

public class DistributionTracingTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
    private SdkTracerProvider provider;

    @Before
    public void setUp() {
        GlobalOpenTelemetry.resetForTest();
        provider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
        GlobalOpenTelemetry.set(OpenTelemetrySdk.builder().setTracerProvider(provider).build());
    }

    @After
    public void tearDown() {
        provider.close();
        GlobalOpenTelemetry.resetForTest();
    }

    @Test
    public void failuresEndSpansAndRestoreAmbientContextBeforeTheNextNotification() throws Exception {
        SdncUebConfiguration config = mock(SdncUebConfiguration.class);
        when(config.getIncomingDir()).thenReturn(tmp.newFolder("incoming").getAbsolutePath());
        when(config.getArchiveDir()).thenReturn(tmp.newFolder("archive").getAbsolutePath());
        INotificationData failed = mock(INotificationData.class);
        when(failed.getResources()).thenThrow(new IllegalStateException("processing failed"));
        INotificationData successful = mock(INotificationData.class);
        when(successful.getResources()).thenReturn(Collections.emptyList());
        SdncUebCallback callback = new SdncUebCallback(mock(IDistributionClient.class), config);
        Span ambient = GlobalOpenTelemetry.getTracer("test").spanBuilder("ambient").startSpan();
        try (Scope scope = ambient.makeCurrent()) {
            callback.activateCallback(failed);
            assertEquals(ambient.getSpanContext(), Span.current().getSpanContext());
            callback.activateCallback(successful);
            assertEquals(ambient.getSpanContext(), Span.current().getSpanContext());
        } finally {
            ambient.end();
        }
        List<SpanData> spans = exporter.getFinishedSpanItems();
        assertEquals(3, spans.size());
        assertEquals(StatusCode.ERROR, spans.get(0).getStatus().getStatusCode());
        assertEquals(1, spans.get(0).getEvents().size());
        assertEquals(StatusCode.UNSET, spans.get(1).getStatus().getStatusCode());
        assertFalse(spans.get(0).getParentSpanContext().isValid());
        assertFalse(spans.get(1).getParentSpanContext().isValid());
        assertNotEquals(spans.get(0).getTraceId(), spans.get(1).getTraceId());
        assertFalse(Span.current().getSpanContext().isValid());
    }

    @Test
    public void standaloneSdkExportsWithConfiguredOtlpEndpoint() throws Exception {
        provider.close();
        GlobalOpenTelemetry.resetForTest();
        LinkedBlockingQueue<byte[]> requests = new LinkedBlockingQueue<>();
        HttpServer collector = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        collector.createContext("/v1/traces", exchange -> {
            requests.add(exchange.getRequestBody().readAllBytes());
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        collector.start();
        Map<String, String> configuration = Map.of(
                "otel.exporter.otlp.protocol", "http/protobuf",
                "otel.exporter.otlp.endpoint", "http://localhost:" + collector.getAddress().getPort(),
                "otel.traces.sampler", "always_on");
        Map<String, String> previous = new java.util.HashMap<>();
        configuration.forEach((key, value) -> previous.put(key, System.setProperty(key, value)));
        try {
            OpenTelemetrySdk sdk = DistributionTracing.initialize();
            provider = sdk.getSdkTracerProvider();
            GlobalOpenTelemetry.getTracer("bootstrap-test").spanBuilder("export-test").startSpan().end();
            assertTrue(provider.forceFlush().join(10, TimeUnit.SECONDS).isSuccess());
            byte[] request = requests.poll(10, TimeUnit.SECONDS);
            assertTrue(request != null && request.length > 0);
            assertTrue(new String(request, java.nio.charset.StandardCharsets.ISO_8859_1)
                    .contains("sdnc-ueb-listener"));
        } finally {
            previous.forEach((key, value) -> {
                if (value == null) {
                    System.clearProperty(key);
                } else {
                    System.setProperty(key, value);
                }
            });
            collector.stop(0);
        }
    }

    @Test
    public void sdkCanBeDisabledThroughStandardConfiguration() {
        provider.close();
        GlobalOpenTelemetry.resetForTest();
        String previousDisabled = System.setProperty("otel.sdk.disabled", "true");
        String previousExporter = System.setProperty("otel.traces.exporter", "unavailable-exporter");
        try {
            OpenTelemetrySdk sdk = DistributionTracing.initialize();
            assertTrue(sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS).isSuccess());
        } finally {
            if (previousDisabled == null) {
                System.clearProperty("otel.sdk.disabled");
            } else {
                System.setProperty("otel.sdk.disabled", previousDisabled);
            }
            if (previousExporter == null) {
                System.clearProperty("otel.traces.exporter");
            } else {
                System.setProperty("otel.traces.exporter", previousExporter);
            }
        }
    }

    @Test
    public void initializationReusesAnExistingProvider() {
        OpenTelemetry telemetry = GlobalOpenTelemetry.get();
        DistributionTracing.initialize();
        assertSame(telemetry, GlobalOpenTelemetry.get());
        assertTrue(GlobalOpenTelemetry.isSet());
    }
}
