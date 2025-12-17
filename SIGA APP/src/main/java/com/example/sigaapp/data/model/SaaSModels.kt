package com.example.sigaapp.data.model

import java.text.NumberFormat
import java.util.Locale
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null,
    @kotlinx.serialization.SerialName("precioUnitario")
    val precioUnitario: String? = null, // Backend envía precio como String
    val precio: Int? = null, // Fallback si envía int
    val activo: Boolean = true, // Soft delete flag
    val codigo: String? = null
) {
    // Helper para obtener precio como Int
    fun getPrecioInt(): Int {
        return precioUnitario?.toIntOrNull() ?: precio ?: 0
    }

    fun getPrecioDouble(): Double? {
        val normalized = precioUnitario?.replace(",", ".")
        return normalized?.toDoubleOrNull() ?: precio?.toDouble()
    }

    fun getPrecioDisplay(locale: Locale = Locale("es", "CL")): String {
        val value = getPrecioDouble()
        return if (value != null) {
            val format = NumberFormat.getCurrencyInstance(locale)
            format.maximumFractionDigits = 0
            format.format(value)
        } else "Sin precio"
    }
}

@Serializable
data class ProductosListResponse(
    val success: Boolean,
    val productos: List<Product> = emptyList()
)

@Serializable
data class StockItem(
    val id: Int,
    val producto_id: Int,
    val local_id: Int,
    val cantidad: Int,
    val min_stock: Int,
    val producto: Product? = null // Si viene anidado
)

@Serializable
data class StockListResponse(
    val success: Boolean,
    val stock: List<StockItem> = emptyList()
)

@Serializable
data class Sale(
    val id: Int,
    val fecha: String, // ISO date
    val total: Int,
    val items: Int, // Cantidad de items o calculado
    val local_id: Int,
    val local_nombre: String? = null // Si la API lo devuelve
)

@Serializable
data class VentasListResponse(
    val success: Boolean,
    val ventas: List<Sale> = emptyList()
)

@Serializable
data class Category(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null
)

@Serializable
data class CategoryRequest(
    val nombre: String,
    val descripcion: String? = null
)

@Serializable
data class CategoriesResponse(
    val success: Boolean,
    val categorias: List<Category> = emptyList()
)

@Serializable
data class CategoryResponse(
    val success: Boolean,
    val categoria: Category
)

@Serializable
data class StockUpdateRequest(
    @kotlinx.serialization.SerialName("productoId")
    val productoId: Int,
    @kotlinx.serialization.SerialName("localId")
    val localId: Int,
    val cantidad: Int,
    @kotlinx.serialization.SerialName("cantidadMinima")
    val cantidadMinima: Int = 0
)

@Serializable
data class ProductRequest(
    val nombre: String,
    @kotlinx.serialization.SerialName("precioUnitario")
    val precioUnitario: String, // Backend espera String, no Int
    val descripcion: String? = null,
    val categoria_id: Int? = null 
)

@Serializable
data class ProductResponse(
    val success: Boolean,
    val producto: Product
)

@Serializable
data class Local(
    val id: Int,
    val nombre: String,
    val direccion: String? = null,
    val ciudad: String? = null
)

@Serializable
data class LocalesResponse(
    val success: Boolean,
    val locales: List<Local> = emptyList()
)
