/*
 * Copyright 2022 IEXEC BLOCKCHAIN TECH
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

package com.iexec.worker.compute.pre.worker;

import com.iexec.common.worker.api.ExitMessage;
import feign.Param;
import feign.RequestLine;

public interface WorkerApiClient {

    @RequestLine("POST /compute/pre/{chainTaskId}/exit")
    void sendExitCauseForPreComputeStage(@Param("chainTaskId") String chainTaskId,
                                         ExitMessage exitMessage);

}
