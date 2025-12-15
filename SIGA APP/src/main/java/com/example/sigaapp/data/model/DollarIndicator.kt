package com.example.sigaapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DollarIndicatorResponse(
    val version: String? = null,
    val autor: String? = null,
    val codigo: String? = null,
    val nombre: String? = null,
    @SerialName("unidad_medida")
    val unidadMedida: String? = null,
    val serie: List<DollarIndicatorValue> = emptyList()
)

@Serializable
data class DollarIndicatorValue(
    val fecha: String,
    val valor: Double
)

