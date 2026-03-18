# Security Review Findings

Date: 2026-03-18
Scope: `/workspace/Audio-Effects-API`

## Summary

The API has multiple **critical** vulnerabilities that can lead to remote code execution, arbitrary file write/delete, unauthorized access to processing endpoints, and denial-of-service.

## Findings

### 1) OS Command Injection in audio processing (Critical)
- **Location:** `services/AudioProcessingService.kt`
- **Code:** User-controlled `inputPath` and `outputFileName` are interpolated into a shell command string and executed via `/bin/sh -c`.
- **Impact:** An attacker can inject shell metacharacters and execute arbitrary commands as the service user.
- **Evidence:**
  - `val command = "$ffmpegPath -i $inputPath -af $effectFilter -y $outputPath"`
  - `Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", command))`
- **Recommendation:** Avoid invoking a shell. Use `ProcessBuilder` with argument arrays, strict input validation, and allowlisting for file paths.

### 2) Arbitrary file write / path traversal via output filename (High)
- **Location:** `services/AudioProcessingService.kt`
- **Code:** `outputPath = "$outputDirectory/$outputFileName"` directly concatenates untrusted `outputFileName`.
- **Impact:** Attackers can use `../` traversal or absolute/escaped paths to overwrite files outside the intended directory (subject to process privileges).
- **Recommendation:** Normalize and validate against an allowlisted basename pattern (e.g., UUID + extension), then enforce `resolvedPath.startsWith(outputDirectoryCanonical)`.

### 3) Insecure direct object reference on `inputPath` (High)
- **Location:** `models/AudioModels.kt`, `routes/AudioRoutes.kt`, `services/AudioProcessingService.kt`
- **Code:** API accepts client-provided `inputPath` and sends it directly to ffmpeg.
- **Impact:** Attackers may read/process arbitrary server-local files or mounted paths, potentially exposing sensitive data and enabling abuse.
- **Recommendation:** Never accept raw filesystem paths from clients. Use server-issued opaque file IDs mapped to safe storage paths.

### 4) Unprotected processing endpoint (High)
- **Location:** `routes/AudioRoutes.kt`
- **Code:** `/api/audio/effect` route is not wrapped with `authenticate("auth-jwt")`.
- **Impact:** Anyone who can access the API can trigger processing jobs and potentially chain with injection/path issues.
- **Recommendation:** Require authentication and authorization checks on all sensitive routes.

### 5) Arbitrary file deletion primitive (High)
- **Location:** `services/FileStorageService.kt`
- **Code:** `deleteFile(path: String)` deletes any path provided.
- **Impact:** If this method is exposed (now or later), attacker-controlled paths can delete arbitrary files accessible to the process.
- **Recommendation:** Accept only internal file IDs, resolve inside upload directory, and verify canonical path confinement before deletion.

### 6) Denial-of-service risk: no input size/rate limits and blocking process execution (Medium)
- **Location:** `routes/AudioRoutes.kt`, `services/AudioProcessingService.kt`, `services/FileStorageService.kt`
- **Code:** No limits on upload/request size, no throttling, and each request waits synchronously on external process completion.
- **Impact:** Resource exhaustion (CPU, memory, disk, process slots) via large/parallel jobs.
- **Recommendation:** Add request body limits, auth-based quotas, rate limiting, queue-based async processing, and timeouts.

### 7) Sensitive path disclosure in API response (Low)
- **Location:** `services/AudioProcessingService.kt`, `models/AudioModels.kt`
- **Code:** Returns full server output path in `ProcessingResult.outputPath`.
- **Impact:** Leaks internal filesystem layout useful for attackers.
- **Recommendation:** Return opaque job/file IDs or relative public URLs only.

## Additional hardening recommendations
- Run ffmpeg in a sandboxed, least-privileged execution environment (seccomp/AppArmor/container profile).
- Validate JWT claims more strictly (e.g., `sub`, expiration handling and audience constraints) and enforce endpoint-level authorization.
- Add centralized audit logging for file operations and processing requests.
- Add SAST/DAST checks in CI for command injection and path traversal patterns.
