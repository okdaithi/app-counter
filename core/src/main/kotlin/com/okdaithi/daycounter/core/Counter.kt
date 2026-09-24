package com.okdaithi.daycounter.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

enum class CountUnit { DAYS, WEEKS, MONTHS, YEARS }

@Serializable
data class Counter(
    val id: String,
    val title: String,
    /** Stored as ISO yyyy-MM-dd. */
    @Serializable(with = IsoLocalDateSerializer::class)
    val date: LocalDate,
    val unit: CountUnit = CountUnit.DAYS,
    val includeToday: Boolean = false,
    val repeatYearly: Boolean = false,
    val notes: String = "",
)

object IsoLocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("IsoLocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}
