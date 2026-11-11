package io.github.perfecticons.domain.model

/** Validates upstream icon identifiers before writing paths. */
object IconId {

    fun isValid(id: String): Boolean =
        id.isNotBlank() &&
            id != "." &&
            id != ".." &&
            !id.startsWith('.') &&
            id.none { it == '/' || it == '\\' || it == '\u0000' }

}
