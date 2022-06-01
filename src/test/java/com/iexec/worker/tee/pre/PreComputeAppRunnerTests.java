package com.iexec.worker.tee.pre;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.worker.tee.pre.worker.WorkerApiClient;
import com.iexec.worker.tee.pre.worker.WorkerApiManager;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.internal.verification.Times;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static com.iexec.common.utils.IexecEnvUtils.IEXEC_TASK_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.org.webcompere.systemstubs.SystemStubs.catchSystemExit;

@ExtendWith(SystemStubsExtension.class)
class PreComputeAppRunnerTests {
    private static final String CHAIN_TASK_ID = "0x0";

    @Mock
    PreComputeApp preComputeApp;
    @Mock
    WorkerApiClient workerApiClient;
    @Spy
    PreComputeAppRunner preComputeAppRunner = new PreComputeAppRunner();

    @BeforeEach
    void openMocks() {
        MockitoAnnotations.openMocks(this);
        when(preComputeAppRunner.createPreComputeApp(CHAIN_TASK_ID)).thenReturn(preComputeApp);
    }

    @Test
    void noTaskId() throws Exception {
        final int actualExitCode = catchSystemExit(() -> preComputeAppRunner.start());
        assertEquals(3, actualExitCode);
    }

    @Test
    void preComputeSuccess(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);

        doNothing().when(preComputeApp).run();

        final int actualExitCode = catchSystemExit(() -> preComputeAppRunner.start());
        int expected = 0;
        assertEquals(expected, actualExitCode);
        verify(preComputeAppRunner, times(1)).exit(expected);
    }

    @Test
    void knownCauseTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);

        doThrow(new PreComputeException(ReplicateStatusCause.POST_COMPUTE_COMPUTED_FILE_NOT_FOUND))
                .when(preComputeApp).run();

        try (MockedStatic<WorkerApiManager> workerApiManager =
                     Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient)
                    .thenReturn(mock(WorkerApiClient.class));
            final int actualExitCode = catchSystemExit(() -> preComputeAppRunner.start());
            assertEquals(1, actualExitCode);
        }
    }

    @Test
    void unknownCauseTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);

        doThrow(new RuntimeException("Unknown cause")).when(preComputeApp).run();
        try (MockedStatic<WorkerApiManager> workerApiManager =
                     Mockito.mockStatic(WorkerApiManager.class)) {
            workerApiManager.when(WorkerApiManager::getWorkerApiClient)
                    .thenReturn(workerApiClient);
            final int actualExitCode = catchSystemExit(() -> preComputeAppRunner.start());
            assertEquals(1, actualExitCode);
        }
    }

    @Test
    void causeNotTransmitted(EnvironmentVariables environment) throws Exception {
        environment.set(IEXEC_TASK_ID, CHAIN_TASK_ID);

        doThrow(new PreComputeException(ReplicateStatusCause.POST_COMPUTE_COMPUTED_FILE_NOT_FOUND))
                .when(preComputeApp).run();
        doThrow(mock(FeignException.NotFound.class))
                .when(workerApiClient).sendExitCauseForPreComputeStage(eq(CHAIN_TASK_ID), any());

        final int actualExitCode = catchSystemExit(() -> preComputeAppRunner.start());
        assertEquals(2, actualExitCode);
    }
}
