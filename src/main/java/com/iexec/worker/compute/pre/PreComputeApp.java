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
import com.iexec.common.security.CipherUtils;
import com.iexec.common.utils.FileHashUtils;
import com.iexec.common.utils.FileHelper;
import com.iexec.commons.poco.utils.HashUtils;
import com.iexec.commons.poco.utils.MultiAddressHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Base64;

@Slf4j
public class PreComputeApp {

    private final String chainTaskId; // just for convenience
    private PreComputeArgs preComputeArgs;

    public PreComputeApp(String chainTaskId) {
        this.chainTaskId = chainTaskId;
    }

    /**
     * Download, decrypt, and save the plain dataset file in "/iexec_in".
     * If the decrypted file is an archive, it won't be extracted.
     *
     * @throws PreComputeException if dataset or input files could not be made available for the application enclave
     */
    void run() throws PreComputeException {
        preComputeArgs = PreComputeArgs.readArgs(chainTaskId);
        checkOutputFolder();
        if (preComputeArgs.isDatasetRequired()) {
            byte[] encryptedContent = downloadEncryptedDataset();
            byte[] plainContent = decryptDataset(encryptedContent);
            savePlainDatasetFile(plainContent);
        }
        downloadInputFiles();
    }

    /**
     * Check that output folder (/iexec_in) folder exists.
     *
     * @throws PreComputeException if output folder not found
     */
    void checkOutputFolder() throws PreComputeException {
        String outputDir = getPreComputeArgs().getOutputDir();
        log.info("Checking output folder [chainTaskId:{}, path:{}]",
                chainTaskId, outputDir);
        if (new File(outputDir).isDirectory()) {
            return;
        }
        log.error("Output folder not found [chainTaskId:{}, path:{}]",
                chainTaskId, outputDir);
        throw new PreComputeException(ReplicateStatusCause.PRE_COMPUTE_OUTPUT_FOLDER_NOT_FOUND);
    }

    /**
     * Download encrypted dataset file and check its checksum.
     *
     * @return downloaded file bytes
     * @throws PreComputeException if download fails or bad file checksum
     */
    byte[] downloadEncryptedDataset() throws PreComputeException {
        String encryptedDatasetUrl = getPreComputeArgs().getEncryptedDatasetUrl();
        log.info("Downloading encrypted dataset file [chainTaskId:{}, url:{}]",
                chainTaskId, encryptedDatasetUrl);
        byte[] encryptedContent = null;
        if (MultiAddressHelper.isMultiAddress(encryptedDatasetUrl)) {
            for (String ipfsGateway : MultiAddressHelper.IPFS_GATEWAYS) {
                log.debug("Try to download dataset from {}", ipfsGateway);
                encryptedContent = FileHelper.readFileBytesFromUrl(ipfsGateway + encryptedDatasetUrl);
                if (encryptedContent != null) {
                    break;
                }
            }
        } else {
            encryptedContent = FileHelper.readFileBytesFromUrl(encryptedDatasetUrl);
        }
        if (encryptedContent == null) {
            log.error("Failed to download encrypted dataset file [chainTaskId:{}, url:{}]",
                    chainTaskId, encryptedDatasetUrl);
            throw new PreComputeException(ReplicateStatusCause.PRE_COMPUTE_DATASET_DOWNLOAD_FAILED);
        }
        log.info("Checking encrypted dataset checksum [chainTaskId:{}]", chainTaskId);
        String expectedChecksum = getPreComputeArgs().getEncryptedDatasetChecksum();
        String actualChecksum = HashUtils.sha256(encryptedContent);
        if (!actualChecksum.equals(expectedChecksum)) {
            log.info("Invalid dataset checksum [chainTaskId:{}, expected:{}, actual:{}]",
                    chainTaskId, expectedChecksum, actualChecksum);
            throw new PreComputeException(ReplicateStatusCause.PRE_COMPUTE_INVALID_DATASET_CHECKSUM);
        }
        return encryptedContent;
    }

    /**
     * Decrypt dataset content.
     *
     * @param encryptedContent bytes
     * @return plain dataset content bytes
     * @throws PreComputeException if decryption fails
     */
    byte[] decryptDataset(byte[] encryptedContent) throws PreComputeException {
        log.info("Decrypting dataset [chainTaskId:{}]", chainTaskId);
        try {
            final String key = getPreComputeArgs().getEncryptedDatasetBase64Key();
            final byte[] decodeKey = Base64.getDecoder().decode(key);
            byte[] plainDatasetContent = CipherUtils.aesDecrypt(encryptedContent, decodeKey);
            log.info("Decrypted dataset [chainTaskId:{}]", chainTaskId);
            return plainDatasetContent;
        } catch (Exception e) {
            log.error("Failed to decrypt dataset [chainTaskId:{}]", chainTaskId, e);
            throw new PreComputeException(ReplicateStatusCause.PRE_COMPUTE_DATASET_DECRYPTION_FAILED);
        }
    }

    /**
     * Save plain dataset content in output folder (iexec_in).
     * The created file will have the name provided in the env
     * variable IEXEC_DATASET_FILENAME.
     *
     * @param plainContent bytes
     * @throws PreComputeException if saving the file fails
     */
    void savePlainDatasetFile(byte[] plainContent) throws PreComputeException {
        String plainDatasetFilepath = getPreComputeArgs().getOutputDir() + File.separator +
                getPreComputeArgs().getPlainDatasetFilename();
        log.info("Saving plain dataset file [chainTaskId:{}, path:{}]",
                chainTaskId, plainDatasetFilepath);
        if (!FileHelper.writeFile(plainDatasetFilepath, plainContent)) {
            log.error("Failed to write plain dataset file [chainTaskId:{}, path:{}]",
                    chainTaskId, plainDatasetFilepath);
            throw new PreComputeException(ReplicateStatusCause.PRE_COMPUTE_SAVING_PLAIN_DATASET_FAILED);
        }
        log.info("Saved plain dataset file to disk [chainTaskId:{}]", chainTaskId);
    }

    /**
     * Download files and save them in the output folder (iexec_in)
     * if the list is not empty.
     *
     * @throws PreComputeException if download of one of the files fails
     */
    void downloadInputFiles() throws PreComputeException {
        for (String url : getPreComputeArgs().getInputFiles()) {
            log.info("Downloading input file [chainTaskId:{}, url:{}]", chainTaskId, url);
            if (FileHelper.downloadFile(url, getPreComputeArgs().getOutputDir(), FileHashUtils.createFileNameFromUri(url))
                    .isEmpty()) {
                throw new PreComputeException(ReplicateStatusCause.PRE_COMPUTE_INPUT_FILE_DOWNLOAD_FAILED);
            }
        }
    }

    /**
     * Added for testing purpose.
     *
     * @return A {@link PreComputeArgs} instance
     */
    PreComputeArgs getPreComputeArgs() {
        return preComputeArgs;
    }
}
