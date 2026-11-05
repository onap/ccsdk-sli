/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
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

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import java.net.HttpURLConnection;
import java.util.Map;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.NotificationMessageContext;

final class DistributionTracing {
    private static final W3CTraceContextPropagator PROPAGATOR = W3CTraceContextPropagator.getInstance();
    private static final TextMapGetter<Map<String, String>> GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier.keySet();
        }

        @Override
        public String get(Map<String, String> carrier, String key) {
            return carrier.get(key);
        }
    };

    private DistributionTracing() {
    }

    static OpenTelemetrySdk initialize() {
        if (!GlobalOpenTelemetry.isSet()) {
            return AutoConfiguredOpenTelemetrySdk.builder()
                    .addPropertiesSupplier(() -> Map.of(
                            "otel.service.name", "sdnc-ueb-listener",
                            "otel.traces.exporter", "otlp",
                            "otel.exporter.otlp.protocol", "http/protobuf",
                            "otel.metrics.exporter", "none",
                            "otel.logs.exporter", "none"))
                    .setResultAsGlobal()
                    .build().getOpenTelemetrySdk();
        }
        return null;
    }

    static Span startNotification(INotificationData data) {
        NotificationMessageContext message = data.getMessageContext();
        if (message == null) {
            message = NotificationMessageContext.EMPTY;
        }
        Context parent = PROPAGATOR.extract(Context.root(), message.getHeaders(), GETTER);
        SpanBuilder builder = GlobalOpenTelemetry.getOrNoop().getTracer("org.onap.ccsdk.ueb-listener")
                .spanBuilder("sdc.distribution.process")
                .setSpanKind(SpanKind.CONSUMER)
                .setParent(parent)
                .setAttribute("messaging.system", "kafka")
                .setAttribute("messaging.operation.type", "process");
        if (message.getTopic() != null) {
            builder.setAttribute("messaging.destination.name", message.getTopic())
                    .setAttribute("messaging.destination.partition.id", String.valueOf(message.getPartition()))
                    .setAttribute("messaging.kafka.message.offset", message.getOffset());
        }
        return builder.startSpan();
    }

    static void inject(HttpURLConnection connection) {
        PROPAGATOR.inject(Context.current(), connection, HttpURLConnection::setRequestProperty);
    }
}
