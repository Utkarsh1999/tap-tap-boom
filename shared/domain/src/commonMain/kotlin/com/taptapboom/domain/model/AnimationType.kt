package com.taptapboom.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sealed class representing all supported animation types.
 * Each type maps to a unique visual renderer on the Canvas.
 */
@Serializable
enum class AnimationType {
    @SerialName("ripple") RIPPLE,
    @SerialName("burst") BURST,
    @SerialName("spiral") SPIRAL,
    @SerialName("wave") WAVE,
    @SerialName("scatter") SCATTER,
    @SerialName("pulse") PULSE,
    @SerialName("bloom") BLOOM,
    @SerialName("shatter") SHATTER,
    @SerialName("orbit") ORBIT,
    @SerialName("flash") FLASH,
    @SerialName("mirror") MIRROR,
    @SerialName("slice") SLICE
}
