package com.aerotech.aerocode.core.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GitEngine {

    private val _status = MutableStateFlow(GitStatus(currentBranch = "main"))
    val status: StateFlow<GitStatus> = _status.asStateFlow()

    private val _commits = MutableStateFlow<List<GitCommit>>(emptyList())
    val commits: StateFlow<List<GitCommit>> = _commits.asStateFlow()

    private val _branches = MutableStateFlow<List<GitBranch>>(listOf(GitBranch("main", isCurrent = true)))
    val branches: StateFlow<List<GitBranch>> = _branches.asStateFlow()

    suspend fun refresh(projectPath: String): GitStatus = withContext(Dispatchers.IO) {
        val dir = File(projectPath)
        if (!dir.exists()) return@withContext GitStatus("main")

        val dotGit = File(dir, ".git")
        if (!dotGit.exists()) {
            initRepo(projectPath)
        }

        val branch = getCurrentBranch(projectPath)
        val (modified, untracked, staged) = parseStatus(projectPath)
        val logList = fetchLog(projectPath)
        val branchList = fetchBranches(projectPath, branch)

        val isClean = modified.isEmpty() && untracked.isEmpty() && staged.isEmpty()
        val gitStatus = GitStatus(
            currentBranch = branch,
            modifiedFiles = modified,
            untrackedFiles = untracked,
            stagedFiles = staged,
            aheadCount = 0,
            behindCount = 0,
            isClean = isClean
        )

        _status.value = gitStatus
        _commits.value = logList
        _branches.value = branchList
        gitStatus
    }

    suspend fun initRepo(projectPath: String): GitOperationResult = withContext(Dispatchers.IO) {
        val result = runGitCommand(projectPath, "init -b main")
        if (result.success) {
            runGitCommand(projectPath, "config user.name \"AeroCode Developer\"")
            runGitCommand(projectPath, "config user.email \"dev@aerocode.io\"")
            // Create initial commit if empty
            runGitCommand(projectPath, "add .")
            runGitCommand(projectPath, "commit -m \"Initial commit from AeroCode\"")
        }
        result
    }

    suspend fun commit(projectPath: String, message: String): GitOperationResult = withContext(Dispatchers.IO) {
        if (message.isBlank()) {
            return@withContext GitOperationResult(false, "Commit message cannot be empty")
        }
        runGitCommand(projectPath, "add -A")
        val escapedMsg = message.replace("\"", "\\\"")
        val res = runGitCommand(projectPath, "commit -m \"$escapedMsg\"")
        refresh(projectPath)
        res
    }

    suspend fun pull(projectPath: String, remote: String = "origin", branch: String = ""): GitOperationResult = withContext(Dispatchers.IO) {
        val b = branch.ifBlank { getCurrentBranch(projectPath) }
        val res = runGitCommand(projectPath, "pull $remote $b")
        refresh(projectPath)
        res
    }

    suspend fun push(projectPath: String, remote: String = "origin", branch: String = ""): GitOperationResult = withContext(Dispatchers.IO) {
        val b = branch.ifBlank { getCurrentBranch(projectPath) }
        val res = runGitCommand(projectPath, "push $remote $b")
        refresh(projectPath)
        res
    }

    suspend fun createBranch(projectPath: String, branchName: String): GitOperationResult = withContext(Dispatchers.IO) {
        val cleanName = branchName.trim().replace(" ", "-")
        val res = runGitCommand(projectPath, "checkout -b $cleanName")
        refresh(projectPath)
        res
    }

    suspend fun checkoutBranch(projectPath: String, branchName: String): GitOperationResult = withContext(Dispatchers.IO) {
        val res = runGitCommand(projectPath, "checkout $branchName")
        refresh(projectPath)
        res
    }

    suspend fun deleteBranch(projectPath: String, branchName: String): GitOperationResult = withContext(Dispatchers.IO) {
        val res = runGitCommand(projectPath, "branch -D $branchName")
        refresh(projectPath)
        res
    }

    suspend fun merge(projectPath: String, branchName: String): GitOperationResult = withContext(Dispatchers.IO) {
        val res = runGitCommand(projectPath, "merge $branchName")
        refresh(projectPath)
        res
    }

    suspend fun rebase(projectPath: String, branchName: String): GitOperationResult = withContext(Dispatchers.IO) {
        val res = runGitCommand(projectPath, "rebase $branchName")
        refresh(projectPath)
        res
    }

    private suspend fun getCurrentBranch(projectPath: String): String = withContext(Dispatchers.IO) {
        val res = runGitCommand(projectPath, "rev-parse --abbrev-ref HEAD")
        if (res.success && res.output.isNotBlank()) {
            res.output.trim().lines().firstOrNull() ?: "main"
        } else {
            "main"
        }
    }

    private suspend fun parseStatus(projectPath: String): Triple<List<String>, List<String>, List<String>> = withContext(Dispatchers.IO) {
        val res = runGitCommand(projectPath, "status --porcelain")
        val modified = mutableListOf<String>()
        val untracked = mutableListOf<String>()
        val staged = mutableListOf<String>()

        if (res.success) {
            res.output.lines().forEach { line ->
                if (line.length >= 3) {
                    val code = line.substring(0, 2)
                    val path = line.substring(3).trim()
                    when {
                        code.startsWith("M") || code.startsWith("A") -> staged.add(path)
                        code.endsWith("M") -> modified.add(path)
                        code == "??" -> untracked.add(path)
                    }
                }
            }
        }
        Triple(modified, untracked, staged)
    }

    private suspend fun fetchLog(projectPath: String): List<GitCommit> = withContext(Dispatchers.IO) {
        val res = runGitCommand(projectPath, "log -n 30 --pretty=format:\"%H|%h|%an|%ad|%s\" --date=short")
        val list = mutableListOf<GitCommit>()
        if (res.success && res.output.isNotBlank()) {
            res.output.lines().forEach { line ->
                val parts = line.split("|")
                if (parts.size >= 5) {
                    list.add(
                        GitCommit(
                            hash = parts[0],
                            shortHash = parts[1],
                            author = parts[2],
                            date = parts[3],
                            message = parts.drop(4).joinToString("|")
                        )
                    )
                }
            }
        }
        list
    }

    private suspend fun fetchBranches(projectPath: String, currentBranch: String): List<GitBranch> = withContext(Dispatchers.IO) {
        val res = runGitCommand(projectPath, "branch -a")
        val list = mutableListOf<GitBranch>()
        if (res.success && res.output.isNotBlank()) {
            res.output.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                    val isCurr = trimmed.startsWith("*")
                    val name = trimmed.removePrefix("*").trim()
                    if (!name.contains("->")) {
                        list.add(GitBranch(name = name, isCurrent = isCurr || name == currentBranch))
                    }
                }
            }
        }
        if (list.isEmpty()) {
            list.add(GitBranch("main", isCurrent = true))
        }
        list.distinctBy { it.name }
    }

    private fun runGitCommand(projectPath: String, args: String): GitOperationResult {
        return try {
            val dir = File(projectPath)
            val process = ProcessBuilder("sh", "-c", "git $args")
                .directory(if (dir.exists()) dir else null)
                .redirectErrorStream(true)
                .start()

            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val code = process.waitFor()
            GitOperationResult(
                success = code == 0,
                message = if (code == 0) "git $args completed successfully" else (output.ifBlank { "git error exit code $code" }),
                output = output.trim()
            )
        } catch (e: Exception) {
            GitOperationResult(false, e.message ?: "Git command execution failed")
        }
    }
}
