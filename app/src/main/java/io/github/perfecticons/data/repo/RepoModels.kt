package io.github.perfecticons.data.repo


/** A file selected from the upstream tree. */
data class RepoFile(
    val path: String,
    val blobId: String,
    val size: Long,
)

/** Snapshot metadata persisted next to the materialized files. */
data class RepoSnapshot(
    val commit: String,
    val styles: List<String>,
    val syncedAt: Long,
    val files: List<RepoFile>,
    /**
     * Upstream repository the snapshot was fetched from. Null for snapshots
     * written before this field existed. Compared against the configured
     * repository to flag a snapshot that came from somewhere else.
     */
    val sourceRepo: String? = null,
) {
    val totalBytes: Long get() = files.sumOf { it.size }
}

sealed interface SyncProgress {
    data object FetchingTrees : SyncProgress
    data object Resolving : SyncProgress
    data class FetchingBlobs(val fetched: Int, val total: Int, val batch: Int, val batches: Int) : SyncProgress
    data class Materializing(val written: Int, val total: Int) : SyncProgress
    data object Done : SyncProgress
    data class Failed(val message: String) : SyncProgress
}
