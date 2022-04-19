/*
 * Copyright 2020 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.tee.pre;

import com.iexec.common.replicate.ReplicateStatusCause;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static com.iexec.common.precompute.PreComputeUtils.*;
import static com.iexec.common.utils.IexecEnvUtils.*;

class PreComputeArgsTests {


    private static Stream<Map.Entry<String, ReplicateStatusCause>> buildReplicateCauseIfMissing() {
        return Map.of(
                IEXEC_TASK_ID, ReplicateStatusCause.PRE_COMPUTE_TASK_ID_MISSING,
                IEXEC_PRE_COMPUTE_OUT, ReplicateStatusCause.PRE_COMPUTE_OUTPUT_PATH_MISSING,
                IS_DATASET_REQUIRED, ReplicateStatusCause.PRE_COMPUTE_IS_DATASET_REQUIRED_MISSING,
                IEXEC_DATASET_URL, ReplicateStatusCause.PRE_COMPUTE_AT_LEAST_ONE_INPUT_FILE_URL_MISSING,
                IEXEC_DATASET_KEY, ReplicateStatusCause.PRE_COMPUTE_DATASET_KEY_MISSING,
                IEXEC_DATASET_CHECKSUM, ReplicateStatusCause.PRE_COMPUTE_DATASET_CHECKSUM_MISSING,
                IEXEC_DATASET_FILENAME, ReplicateStatusCause.PRE_COMPUTE_DATASET_FILENAME_MISSING,
                IEXEC_INPUT_FILES_NUMBER, ReplicateStatusCause.PRE_COMPUTE_INPUT_FILES_NUMBER_MISSING,
                IEXEC_INPUT_FILE_URL_PREFIX + RandomStringUtils.random(100), ReplicateStatusCause.PRE_COMPUTE_AT_LEAST_ONE_INPUT_FILE_URL_MISSING
        ).entrySet().stream();
    }

    @ParameterizedTest
    @MethodSource("buildReplicateCauseIfMissing")
    void shouldBuildReplicateCauseIfMissing(Map.Entry<String, ReplicateStatusCause> entry) {
        Assertions.assertEquals(entry.getValue(), PreComputeArgs.buildReplicateCauseIfMissing(entry.getKey()));
    }

}
