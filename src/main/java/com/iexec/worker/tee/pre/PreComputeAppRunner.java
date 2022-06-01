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
import com.iexec.common.worker.api.ExitMessage;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import static com.iexec.common.utils.IexecEnvUtils.IEXEC_TASK_ID;
import static com.iexec.worker.tee.pre.worker.WorkerApiManager.getWorkerApiClient;

@Slf4j
public class PreComputeAppRunner {

    public static PreComputeAppRunner build() {
        return new PreComputeAppRunner();
    }

    /**
     * Run PreComputeApp and handle possible exceptions.
     * Exits:
     * - 0: Success
     * - 1: Failure; Reported cause (known or unknown)
     * - 2: Failure; Unreported cause (report issue)
     * - 3: Failure; Unreported cause (task context missing)
     */
    public int start() {
        log.info("TEE pre-compute started");
        ReplicateStatusCause exitCause = ReplicateStatusCause.PRE_COMPUTE_FAILED_UNKNOWN_ISSUE;
        String chainTaskId = "";
        try {
            chainTaskId = PreComputeArgs.getEnvVarOrThrow(IEXEC_TASK_ID);
        } catch (PreComputeException e) {
            log.error("TEE pre-compute cannot go further without taskID context", e);
            return 3;
        }
        try {
            createPreComputeApp(chainTaskId).run();
            log.info("TEE pre-compute completed");
            return 0;
        } catch (PreComputeException e) {
            exitCause = e.getExitCause();
            log.error("TEE pre-compute failed with a known exitCause " +
                    "[exitCause:{}]", exitCause, e);
        } catch (Exception e) {
            log.error("TEE pre-compute failed without explicit exitCause", e);
        }
        try {
            getWorkerApiClient()
                    .sendExitCauseForPreComputeStage(chainTaskId,
                            new ExitMessage(exitCause));
            return 1;
        } catch (FeignException e) {
            log.error("Failed to report exit exitCause [exitCause:{}]", exitCause, e);
        }
        return 2;
    }

    PreComputeApp createPreComputeApp(String chainTaskId) {
        return new PreComputeApp(chainTaskId);
    }
}
