/*
 * Copyright 2025 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.compute.pre.utils;

import com.iexec.worker.compute.pre.PreComputeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_TASK_ID_MISSING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SystemStubsExtension.class)
class EnvUtilsTests {
    private static final String ENVIRONMENT_VAR = "envVar";
    private static final String ENVIRONMENT_VAR_VALUE = "envVarValue";

    // region getEnvVarOrThrow
    @Test
    void shouldGetEnvVarOrThrow(EnvironmentVariables environment) {
        environment.set(ENVIRONMENT_VAR, ENVIRONMENT_VAR_VALUE);
        final String actualValue = Assertions.assertDoesNotThrow(() -> EnvUtils.getEnvVarOrThrow(ENVIRONMENT_VAR, POST_COMPUTE_TASK_ID_MISSING));
        assertEquals(ENVIRONMENT_VAR_VALUE, actualValue);
    }

    @Test
    void shouldNotGetEnvVarOrThrowSinceEmptyVar(EnvironmentVariables environment) {
        environment.set(ENVIRONMENT_VAR, "");
        final PreComputeException exception = assertThrows(PreComputeException.class, () -> EnvUtils.getEnvVarOrThrow(ENVIRONMENT_VAR, POST_COMPUTE_TASK_ID_MISSING));
        assertEquals(POST_COMPUTE_TASK_ID_MISSING, exception.getExitCause());
    }

    @Test
    void shouldNotGetEnvVarOrThrowSinceUnknownVar() {
        final PreComputeException exception = assertThrows(PreComputeException.class, () -> EnvUtils.getEnvVarOrThrow(ENVIRONMENT_VAR, POST_COMPUTE_TASK_ID_MISSING));
        assertEquals(POST_COMPUTE_TASK_ID_MISSING, exception.getExitCause());
    }
    // endregion
}
