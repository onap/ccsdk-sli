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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IComponentDoneStatusMessage;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.api.results.DistributionActionResultEnum;
import org.onap.sdc.api.notification.DistributionStatusEnum;

public class SdncUebCallbackStatusTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private final InMemorySpanExporter exporter = InMemorySpanExporter.create();
    private SdkTracerProvider provider;
    private IDistributionClient client;
    private INotificationData data;
    private IArtifactInfo csar;
    private SdncUebCallback callback;

    @Before
    public void setUp() throws Exception {
        GlobalOpenTelemetry.resetForTest();
        provider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build();
        GlobalOpenTelemetry.set(OpenTelemetrySdk.builder().setTracerProvider(provider).build());
        SdncUebConfiguration config = mock(SdncUebConfiguration.class);
        when(config.getIncomingDir()).thenReturn(tmp.newFolder("incoming").getAbsolutePath());
        when(config.getArchiveDir()).thenReturn(tmp.newFolder("archive").getAbsolutePath());
        when(config.getMaxPasses()).thenReturn(2);

        client = mock(IDistributionClient.class);
        data = mock(INotificationData.class);
        when(data.getDistributionID()).thenReturn("dist-1");
        when(data.getServiceName()).thenReturn("svc");
        when(data.getResources()).thenReturn(Collections.emptyList());

        csar = mock(IArtifactInfo.class);
        when(csar.getArtifactName()).thenReturn("service-Svc-csar.csar");
        when(csar.getArtifactType()).thenReturn("TOSCA_CSAR");
        when(data.getServiceArtifacts()).thenReturn(Collections.singletonList(csar));

        callback = new SdncUebCallback(client, config);
    }

    @After
    public void tearDown() {
        provider.close();
        GlobalOpenTelemetry.resetForTest();
    }

    private void assertErrorSpan() {
        assertEquals(1, exporter.getFinishedSpanItems().size());
        assertEquals(StatusCode.ERROR, exporter.getFinishedSpanItems().get(0).getStatus().getStatusCode());
    }

    private IDistributionClientDownloadResult downloadResult(DistributionActionResultEnum result, byte[] payload) {
        IDistributionClientDownloadResult downloadResult = mock(IDistributionClientDownloadResult.class);
        when(downloadResult.getDistributionActionResult()).thenReturn(result);
        when(downloadResult.getArtifactPayload()).thenReturn(payload);
        return downloadResult;
    }

    private String verifyComponentDoneError() {
        ArgumentCaptor<IComponentDoneStatusMessage> message = ArgumentCaptor.forClass(IComponentDoneStatusMessage.class);
        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);
        verify(client).sendComponentDoneStatus(message.capture(), reason.capture());
        verify(client, never()).sendComponentDoneStatus(any());
        assertEquals(DistributionStatusEnum.COMPONENT_DONE_ERROR, message.getValue().getStatus());
        assertFalse(reason.getValue().isEmpty());
        return reason.getValue();
    }

    @Test
    public void csarDeployFailureWithoutToscaTemplateSendsComponentDoneError() {
        IDistributionClientDownloadResult downloadResult =
                downloadResult(DistributionActionResultEnum.SUCCESS, "not a csar".getBytes());
        when(client.download(csar)).thenReturn(downloadResult);

        callback.activateCallback(data);

        ArgumentCaptor<IDistributionStatusMessage> deploy = ArgumentCaptor.forClass(IDistributionStatusMessage.class);
        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);
        verify(client).sendDeploymentStatus(deploy.capture(), reason.capture());
        assertEquals(DistributionStatusEnum.DEPLOY_ERROR, deploy.getValue().getStatus());
        assertFalse(reason.getValue().isEmpty());
        verifyComponentDoneError();
        assertErrorSpan();
    }

    @Test
    public void failedDownloadSendsDownloadErrorWithReason() {
        IDistributionClientDownloadResult downloadResult = downloadResult(DistributionActionResultEnum.FAIL, new byte[0]);
        when(downloadResult.getDistributionMessageResult()).thenReturn("connection refused");
        when(client.download(csar)).thenReturn(downloadResult);

        callback.activateCallback(data);

        ArgumentCaptor<IDistributionStatusMessage> download = ArgumentCaptor.forClass(IDistributionStatusMessage.class);
        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);
        verify(client).sendDownloadStatus(download.capture(), reason.capture());
        verify(client, never()).sendDownloadStatus(any());
        assertEquals(DistributionStatusEnum.DOWNLOAD_ERROR, download.getValue().getStatus());
        assertEquals("SDN-C failed to download artifact service-Svc-csar.csar: FAIL connection refused",
                reason.getValue());
        verifyComponentDoneError();
        assertErrorSpan();
    }

    @Test
    public void unexpectedExceptionStillSendsComponentDoneError() {
        when(client.download(csar)).thenThrow(new IllegalStateException("boom"));

        callback.activateCallback(data);

        String reason = verifyComponentDoneError();
        assertEquals(true, reason.contains("boom"));
        verify(client, never()).sendDownloadStatus(any(), anyString());
        assertErrorSpan();
    }
}
