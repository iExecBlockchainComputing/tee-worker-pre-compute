/*
 * Copyright 2022-2025 IEXEC BLOCKCHAIN TECH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iexec.worker.compute.pre;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.common.worker.api.ExitMessage;
import com.iexec.worker.api.WorkerApiClient;
import com.iexec.worker.api.WorkerApiManager;
import com.iexec.worker.compute.pre.signer.SignerService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_COMPUTED_FILE_NOT_FOUND;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.IEXEC_TASK_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SystemStubsExtension.class)
@ExtendWith(MockitoExtension.class)
class PreComputeAppRunnerTests {
    private static final String CHAIN_TASK_ID = "0x0";
    private static final String CHALLENGE = "challenge";

    @Mock
    SignerService signerService;

    @Spy
    PreComputeAppRunner preComputeAppRunner;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(preComputeAppRunner, "signerService", signerService);
    }

    @Test
    void noTaskId() {
        final int exitStatus = preComputeAppRunner.start();
        assertEquals(3, exitStatus);
    }

    @Test
    void preComputeSuccess(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);

        final PreComputeApp preComputeApp = mock(PreComputeApp.class);
        when(preComputeAppRunner.createPreComputeApp(CHAIN_TASK_ID)).thenReturn(preComputeApp);
        doNothing().when(preComputeApp).run();

        final int exitStatus = preComputeAppRunner.start();

        assertEquals(0, exitStatus);
    }

    @Test
    void knownCauseTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);

        final PreComputeApp preComputeApp = mock(PreComputeApp.class);
        doThrow(new PreComputeException(POST_COMPUTE_COMPUTED_FILE_NOT_FOUND))
                .when(preComputeApp).run();

        final WorkerApiClient workerApiClient = mock(WorkerApiClient.class);

        when(preComputeAppRunner.createPreComputeApp(CHAIN_TASK_ID)).thenReturn(preComputeApp);
        when(signerService.getChallenge(CHAIN_TASK_ID)).thenReturn(CHALLENGE);

        try (MockedStatic<WorkerApiManager> workerApiManager = Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient)
                    .thenReturn(workerApiClient);
            final int exitStatus = preComputeAppRunner.start();
            assertEquals(1, exitStatus);
        }
    }

    @Test
    void unknownCauseTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);
        when(signerService.getChallenge(CHAIN_TASK_ID)).thenReturn(CHALLENGE);

        PreComputeApp preComputeApp = mock(PreComputeApp.class);
        doThrow(new RuntimeException("Unknown cause")).when(preComputeApp).run();

        WorkerApiClient workerApiClient = mock(WorkerApiClient.class);

        when(preComputeAppRunner.createPreComputeApp(CHAIN_TASK_ID)).thenReturn(preComputeApp);
        try (MockedStatic<WorkerApiManager> workerApiManager = Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient)
                    .thenReturn(workerApiClient);
            final int exitStatus = preComputeAppRunner.start();
            assertEquals(1, exitStatus);
        }
    }

    @Test
    void causeNotTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);
        when(signerService.getChallenge(CHAIN_TASK_ID)).thenReturn(CHALLENGE);

        PreComputeApp preComputeApp = mock(PreComputeApp.class);
        doThrow(new PreComputeException(ReplicateStatusCause.POST_COMPUTE_COMPUTED_FILE_NOT_FOUND))
                .when(preComputeApp).run();

        WorkerApiClient workerApiClient = mock(WorkerApiClient.class);
        doThrow(FeignException.NotFound.class)
                .when(workerApiClient).sendExitCauseForPreComputeStage(
                        eq(CHALLENGE),
                        eq(CHAIN_TASK_ID),
                        any(ExitMessage.class));

        when(preComputeAppRunner.createPreComputeApp(CHAIN_TASK_ID)).thenReturn(preComputeApp);
        try (MockedStatic<WorkerApiManager> workerApiManager = Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient)
                    .thenReturn(workerApiClient);
            final int exitStatus = preComputeAppRunner.start();
            assertEquals(2, exitStatus);
        }
    }
}
