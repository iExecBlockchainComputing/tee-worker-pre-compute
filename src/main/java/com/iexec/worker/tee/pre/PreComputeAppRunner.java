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

import com.iexec.common.precompute.PreComputeExitCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PreComputeAppRunner {

    /**
     * Run PreComputeApp and handle possible exceptions.
     */
    public static void start() {
        log.info("TEE pre-compute started");
        int exitCode = PreComputeExitCode.SUCCESS.value();
        try {
            new PreComputeApp().run();
        } catch(PreComputeException e) {
            log.error("TEE pre-compute failed with a known error " +
                    "[errorMessage:{}, errorCode:{}]", e.getExitCode(),
                    e.getExitCode().value(), e);
            exitCode = e.getExitCode().value();
        } catch (Exception e) {
            log.error("TEE pre-compute failed with an unknown error", e);
            exitCode = PreComputeExitCode.UNKNOWN_ERROR.value();
        } finally {
            log.info("TEE pre-compute finished");
            System.exit(exitCode);
        }
    }
}
