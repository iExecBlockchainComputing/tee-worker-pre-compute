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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.iexec.common.precompute.PreComputeUtils.*;
import static com.iexec.common.utils.IexecEnvUtils.*;

@Slf4j
@Data
@Builder
@AllArgsConstructor
public class PreComputeArgs {

    private static final Map<String, ReplicateStatusCause> envVarNameMissingToCause = Map.of(
            IEXEC_TASK_ID, ReplicateStatusCause.PRE_COMPUTE_TASK_ID_MISSING,
            IEXEC_PRE_COMPUTE_OUT, ReplicateStatusCause.PRE_COMPUTE_OUTPUT_PATH_MISSING,
            IS_DATASET_REQUIRED, ReplicateStatusCause.PRE_COMPUTE_IS_DATASET_REQUIRED_MISSING,
            IEXEC_DATASET_URL, ReplicateStatusCause.PRE_COMPUTE_DATASET_URL_MISSING,
            IEXEC_DATASET_KEY, ReplicateStatusCause.PRE_COMPUTE_DATASET_KEY_MISSING,
            IEXEC_DATASET_CHECKSUM, ReplicateStatusCause.PRE_COMPUTE_DATASET_CHECKSUM_MISSING,
            IEXEC_DATASET_FILENAME, ReplicateStatusCause.PRE_COMPUTE_DATASET_FILENAME_MISSING,
            IEXEC_INPUT_FILES_NUMBER, ReplicateStatusCause.PRE_COMPUTE_INPUT_FILES_NUMBER_MISSING);

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
                .outputDir(getEnvVarOrThrow(IEXEC_PRE_COMPUTE_OUT))
                .isDatasetRequired(Boolean.parseBoolean(getEnvVarOrThrow(IS_DATASET_REQUIRED)))
                .inputFiles(new ArrayList<>())
                .build();
        if (args.isDatasetRequired()) {
            args.setEncryptedDatasetUrl(getEnvVarOrThrow(IEXEC_DATASET_URL));
            args.setEncryptedDatasetBase64Key(getEnvVarOrThrow(IEXEC_DATASET_KEY));
            args.setEncryptedDatasetChecksum(getEnvVarOrThrow(IEXEC_DATASET_CHECKSUM));
            args.setPlainDatasetFilename(getEnvVarOrThrow(IEXEC_DATASET_FILENAME));
        }
        int inputFilesNb = Integer.parseInt(getEnvVarOrThrow(IEXEC_INPUT_FILES_NUMBER));
        for (int i = 1; i <= inputFilesNb; i++) {
            String url = getEnvVarOrThrow(IEXEC_INPUT_FILE_URL_PREFIX + i);
            args.getInputFiles().add(url);
        }
        return args;
    }

    public static String getEnvVarOrThrow(String envVarName)
            throws PreComputeException {
        String envVar = System.getenv(envVarName);
        if (StringUtils.isEmpty(envVar)) {
            ReplicateStatusCause cause = buildReplicateCauseIfMissing(envVarName);
            log.error("Required env var is empty [name:{}, cause:{}]",
                    envVarName, cause);

            throw new PreComputeException(cause);
        }
        return envVar;
    }

    static ReplicateStatusCause buildReplicateCauseIfMissing(String envVarName) {
        if (envVarNameMissingToCause.containsKey(envVarName)) {
            return envVarNameMissingToCause.get(envVarName);
        }
        if (envVarName.startsWith(IEXEC_INPUT_FILE_URL_PREFIX)) {
            return ReplicateStatusCause.PRE_COMPUTE_AT_LEAST_ONE_INPUT_FILE_URL_MISSING;
        }
        return null;
    }

}
