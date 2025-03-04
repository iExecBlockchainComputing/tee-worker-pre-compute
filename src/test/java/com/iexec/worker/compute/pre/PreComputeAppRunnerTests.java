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

import com.iexec.worker.api.WorkerApiClient;
import com.iexec.worker.api.WorkerApiManager;
import com.iexec.worker.compute.pre.signer.SignerService;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @Mock
    SignerService signerService;

    @Spy
    @InjectMocks
    PreComputeAppRunner preComputeAppRunner;

    @Test
    void noTaskId() {
        final int exitStatus = preComputeAppRunner.start();
        assertEquals(3, exitStatus);
    }

    @Test
    void preComputeSuccess(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);

        PreComputeApp preComputeApp = mock(PreComputeApp.class);
        when(preComputeAppRunner.createPreComputeApp(CHAIN_TASK_ID)).thenReturn(preComputeApp);
        doNothing().when(preComputeApp).run();

        final int exitStatus = preComputeAppRunner.start();

        assertEquals(0, exitStatus);
    }

    @Test
    void knownCauseTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);

        PreComputeApp preComputeApp = mock(PreComputeApp.class);
        doThrow(new PreComputeException(POST_COMPUTE_COMPUTED_FILE_NOT_FOUND))
                .when(preComputeApp).run();

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
    void unknownCauseTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);

        PreComputeApp preComputeApp = mock(PreComputeApp.class);
        WorkerApiClient workerApiClient = mock(WorkerApiClient.class);

        doThrow(new RuntimeException("Unknown cause")).when(preComputeApp).run();
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

        PreComputeApp preComputeApp = mock(PreComputeApp.class);
        doThrow(new PreComputeException(POST_COMPUTE_COMPUTED_FILE_NOT_FOUND))
                .when(preComputeApp).run();

        when(signerService.getChallenge(CHAIN_TASK_ID)).thenReturn("authorization");
        WorkerApiClient workerApiClient = mock(WorkerApiClient.class);
        doThrow(FeignException.NotFound.class)
                .when(workerApiClient).sendExitCauseForPreComputeStage(
                        anyString(),
                        eq(CHAIN_TASK_ID),
                        any()
                );

        when(preComputeAppRunner.createPreComputeApp(CHAIN_TASK_ID)).thenReturn(preComputeApp);
        try (MockedStatic<WorkerApiManager> workerApiManager = Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient)
                    .thenReturn(workerApiClient);
            final int exitStatus = preComputeAppRunner.start();
            assertEquals(2, exitStatus);
        }
    }
}
