/*
 * Copyright 2020-2025 IEXEC BLOCKCHAIN TECH
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.iexec.common.utils.IexecEnvUtils.IEXEC_INPUT_FILE_URL_PREFIX;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.*;
import static com.iexec.worker.compute.pre.utils.EnvUtils.getEnvVarOrThrow;

@Slf4j
@Data
@Builder
@AllArgsConstructor
public class PreComputeArgs {

    private String chainTaskId;
    private String outputDir;
    // dataset
    private boolean isDatasetRequired;
    private String encryptedDatasetUrl;
    private String encryptedDatasetBase64Key;
    private String encryptedDatasetChecksum;
    private String plainDatasetFilename;
    // input files
    private List<String> inputFiles;

    public static PreComputeArgs readArgs(String chainTaskId) throws PreComputeException {
        PreComputeArgs args = PreComputeArgs.builder()
                .chainTaskId(chainTaskId)
                .outputDir(getEnvVarOrThrow(IEXEC_PRE_COMPUTE_OUT, ReplicateStatusCause.PRE_COMPUTE_OUTPUT_PATH_MISSING))
                .isDatasetRequired(Boolean.parseBoolean(getEnvVarOrThrow(IS_DATASET_REQUIRED, ReplicateStatusCause.PRE_COMPUTE_IS_DATASET_REQUIRED_MISSING)))
                .inputFiles(new ArrayList<>())
                .build();
        if (args.isDatasetRequired()) {
            args.setEncryptedDatasetUrl(getEnvVarOrThrow(IEXEC_DATASET_URL, ReplicateStatusCause.PRE_COMPUTE_DATASET_URL_MISSING));
            args.setEncryptedDatasetBase64Key(getEnvVarOrThrow(IEXEC_DATASET_KEY, ReplicateStatusCause.PRE_COMPUTE_DATASET_KEY_MISSING));
            args.setEncryptedDatasetChecksum(getEnvVarOrThrow(IEXEC_DATASET_CHECKSUM, ReplicateStatusCause.PRE_COMPUTE_DATASET_CHECKSUM_MISSING));
            args.setPlainDatasetFilename(getEnvVarOrThrow(IEXEC_DATASET_FILENAME, ReplicateStatusCause.PRE_COMPUTE_DATASET_FILENAME_MISSING));
        }
        int inputFilesNb = Integer.parseInt(getEnvVarOrThrow(IEXEC_INPUT_FILES_NUMBER, ReplicateStatusCause.PRE_COMPUTE_INPUT_FILES_NUMBER_MISSING));
        for (int i = 1; i <= inputFilesNb; i++) {
            String url = getEnvVarOrThrow(IEXEC_INPUT_FILE_URL_PREFIX + i, ReplicateStatusCause.PRE_COMPUTE_AT_LEAST_ONE_INPUT_FILE_URL_MISSING);
            args.getInputFiles().add(url);
        }
        return args;
    }

}
