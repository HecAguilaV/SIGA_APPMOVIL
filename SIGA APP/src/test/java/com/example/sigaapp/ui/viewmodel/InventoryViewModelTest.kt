package com.example.sigaapp.ui.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import com.example.sigaapp.data.model.Local
import com.example.sigaapp.data.model.Product
import com.example.sigaapp.data.model.StockItem
import com.example.sigaapp.data.repository.SaaSRepository
import com.example.sigaapp.testutils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository: SaaSRepository = mockk(relaxed = true)

    private val sampleLocal = Local(id = 3, nombre = "Casa Matriz")

    private fun sampleProduct() = Product(
        id = 10,
        nombre = "Producto A",
        precioUnitario = "1000"
    )

    private fun sampleStock() = StockItem(
        id = 1,
        producto_id = 10,
        local_id = sampleLocal.id,
        cantidad = 5,
        min_stock = 1,
        producto = sampleProduct()
    )

    private fun stubSuccessfulCommonCalls() {
        coEvery { repository.getLocales() } returns Result.success(listOf(sampleLocal))
        coEvery { repository.getCategories() } returns Result.success(emptyList())
        every { repository.getDefaultLocalId() } returns sampleLocal.id
    }

    @Test
    fun `loadInventory enriquece stock cuando el repo responde OK`() = runTest {
        stubSuccessfulCommonCalls()
        coEvery { repository.getProducts() } returns Result.success(emptyList())
        coEvery { repository.getStock() } returns Result.success(emptyList())

        val stock = sampleStock()
        val viewModel = InventoryViewModel(repository, autoLoad = false)
        viewModel.setRawStockForTest(listOf(stock), sampleLocal)
        advanceUntilIdle()

        val enrichedStock = viewModel.stockItems.value.firstOrNull { it.producto != null }
        assertNotNull("Se esperaba un producto enriquecido", enrichedStock)
        assertEquals(sampleProduct().id, enrichedStock?.producto?.id)
        assertEquals(sampleLocal.id, enrichedStock?.local_id)
    }

    @Test
    fun `loadInventory emite error cuando el repo falla`() = runTest {
        stubSuccessfulCommonCalls()
        val failure = Result.failure<List<Product>>(Exception("Network error"))
        coEvery { repository.getProducts() } returns failure
        coEvery { repository.getStock() } returns Result.failure(Exception("Network error"))

        val viewModel = InventoryViewModel(repository)
        advanceUntilIdle()

        assertEquals("Network error", viewModel.error.value)
    }
}
