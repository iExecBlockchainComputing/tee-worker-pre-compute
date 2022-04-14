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
import com.iexec.common.worker.api.WorkerApiClientBuilder;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import static com.iexec.common.utils.IexecEnvUtils.IEXEC_TASK_ID;
import static com.iexec.worker.tee.pre.PreComputeArgs.getEnvVar;

@Slf4j
public class PreComputeAppRunner {

    public static final String WORKER_HOST = "worker:13100";

    private PreComputeAppRunner() {
        throw new UnsupportedOperationException();
    }

    /**
     * Run PreComputeApp and handle possible exceptions.
     * Exits:
     * - 0: Success
     * - 1: Failure; Reported cause (known or unknown)
     * - 2: Failure; Unreported cause since reporting issue
     * - 3: Failure; Unreported cause since missing taskID context
     */
    public static void start() {
        log.info("TEE pre-compute started");
        ReplicateStatusCause cause = ReplicateStatusCause.PRE_COMPUTE_FAILED;
        String chainTaskId = getEnvVar(IEXEC_TASK_ID);
        if (chainTaskId.isEmpty()) {
            log.error("TEE pre-compute cannot go further without taskID context");
            System.exit(3);
        }
        try {
            new PreComputeApp().run(chainTaskId);
            log.info("TEE pre-compute completed");
            System.exit(0);
        } catch (PreComputeException e) {
            cause = ReplicateStatusCause.UNKNOWN;//TODO update to e.getCause()
            log.error("TEE pre-compute failed with a known cause " +
                    "[cause:{}]", cause, e);
        } catch (Exception e) {
            log.error("TEE pre-compute failed without explicit cause", e);
        }
        try {
            WorkerApiClientBuilder.getInstance(WORKER_HOST)
                    .sendExitCauseForPreComputeStage(chainTaskId, cause);
            System.exit(1);
        } catch (FeignException e) {
            log.error("Failed to report exit cause [cause:{}]", cause, e);
        }
        System.exit(2);
    }
}
