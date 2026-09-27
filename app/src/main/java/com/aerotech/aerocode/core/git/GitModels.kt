package com.aerotech.aerocode.core.git

data class GitBranch(
    val name: String,
    val isCurrent: Boolean,
    val isRemote: Boolean = false
)

data class GitCommit(
    val hash: String,
    val shortHash: String,
    val author: String,
    val date: String,
    val message: String
)

data class GitStatus(
    val currentBranch: String,
    val modifiedFiles: List<String> = emptyList(),
    val untrackedFiles: List<String> = emptyList(),
    val stagedFiles: List<String> = emptyList(),
    val aheadCount: Int = 0,
    val behindCount: Int = 0,
    val isClean: Boolean = true
)

data class GitOperationResult(
    val success: Boolean,
    val message: String,
    val output: String = ""
)
