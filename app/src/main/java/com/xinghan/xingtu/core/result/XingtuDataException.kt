package com.xinghan.xingtu.core.result

/**
 * Domain-level data error. The data layer translates storage exceptions
 * into this type so the UI never sees SQLite-specific exceptions.
 */
class XingtuDataException(
    val kind: Kind,
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    enum class Kind { NOT_FOUND, STORAGE }
}
