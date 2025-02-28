package com.iexec.worker.compute.pre.utils;

import com.iexec.common.replicate.ReplicateStatusCause;
import com.iexec.worker.compute.pre.PreComputeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvUtils {

    private EnvUtils() {

    }

    public static String getEnvVar(String envVarName) {
        String envVar = System.getenv(envVarName);
        if (envVar == null || envVar.isEmpty()) {
            return "";
        }
        return envVar;
    }

    public static String getEnvVarOrThrow(String envVarName, ReplicateStatusCause statusCauseIfMissing) throws PreComputeException {
        String envVar = System.getenv(envVarName);
        if (envVar == null || envVar.isEmpty()) {
            throw new PreComputeException(statusCauseIfMissing);
        }
        return envVar;
    }
}
