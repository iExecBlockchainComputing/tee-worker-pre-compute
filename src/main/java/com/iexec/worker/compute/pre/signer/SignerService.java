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

package com.iexec.worker.compute.pre.signer;

import com.iexec.commons.poco.security.Signature;
import com.iexec.commons.poco.utils.CredentialsUtils;
import com.iexec.commons.poco.utils.HashUtils;
import com.iexec.worker.compute.pre.PreComputeException;
import com.iexec.worker.compute.pre.utils.EnvUtils;
import lombok.extern.slf4j.Slf4j;

import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.SIGN_TEE_CHALLENGE_PRIVATE_KEY;
import static com.iexec.common.worker.tee.TeeSessionEnvironmentVariable.SIGN_WORKER_ADDRESS;
import static com.iexec.commons.poco.utils.SignatureUtils.isExpectedSignerOnSignedMessageHash;
import static com.iexec.commons.poco.utils.SignatureUtils.signMessageHashAndGetSignature;

@Slf4j
public class SignerService {

    public String signEnclaveChallenge(final String messageHash, final String enclaveChallengePrivateKey) throws PreComputeException {
        final Signature enclaveChallengeSignature = signMessageHashAndGetSignature(messageHash, enclaveChallengePrivateKey);

        final boolean isSignatureValid = isExpectedSignerOnSignedMessageHash(messageHash, enclaveChallengeSignature,
                CredentialsUtils.getAddress(enclaveChallengePrivateKey));

        if (!isSignatureValid) {
            throw new PreComputeException(
                    PRE_COMPUTE_INVALID_TEE_SIGNATURE,
                    "Failed to verify TeeEnclaveChallenge signature (exiting)"
            );
        }

        return enclaveChallengeSignature.getValue();
    }

    public String getChallenge(final String chainTaskId) throws PreComputeException {
        final String workerAddress = EnvUtils.getEnvVarOrThrow(SIGN_WORKER_ADDRESS.name(), SIGN_WORKER_ADDRESS_MISSING);
        final String teeChallengePrivateKey = EnvUtils.getEnvVarOrThrow(SIGN_TEE_CHALLENGE_PRIVATE_KEY.name(), SIGN_TEE_CHALLENGE_PRIVATE_KEY_MISSING);
        final String messageHash = HashUtils.concatenateAndHash(chainTaskId, workerAddress);
        return signEnclaveChallenge(messageHash, teeChallengePrivateKey);
    }

}
