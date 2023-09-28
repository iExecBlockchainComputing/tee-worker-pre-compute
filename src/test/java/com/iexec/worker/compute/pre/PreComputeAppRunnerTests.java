package com.iexec.worker.compute.pre;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.worker.api.WorkerApiClient;
import com.iexec.worker.api.WorkerApiManager;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static com.iexec.common.utils.IexecEnvUtils.IEXEC_TASK_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SystemStubsExtension.class)
class PreComputeAppRunnerTests {
    private static final String CHAIN_TASK_ID = "0x0";

    @Spy
    PreComputeAppRunner preComputeAppRunner = new PreComputeAppRunner();

    @BeforeEach
    void openMocks() {
        MockitoAnnotations.openMocks(this);
    }

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
        doThrow(new PreComputeException(ReplicateStatusCause.POST_COMPUTE_COMPUTED_FILE_NOT_FOUND))
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
        doThrow(new PreComputeException(ReplicateStatusCause.POST_COMPUTE_COMPUTED_FILE_NOT_FOUND))
                .when(preComputeApp).run();

        WorkerApiClient workerApiClient = mock(WorkerApiClient.class);
        doThrow(FeignException.NotFound.class)
                .when(workerApiClient).sendExitCauseForPreComputeStage(eq(CHAIN_TASK_ID), any());

        when(preComputeAppRunner.createPreComputeApp(CHAIN_TASK_ID)).thenReturn(preComputeApp);
        try (MockedStatic<WorkerApiManager> workerApiManager = Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient)
                    .thenReturn(workerApiClient);
            final int exitStatus = preComputeAppRunner.start();
            assertEquals(2, exitStatus);
        }
    }
}
