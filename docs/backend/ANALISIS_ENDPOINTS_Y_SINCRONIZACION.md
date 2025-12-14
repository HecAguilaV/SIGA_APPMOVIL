# Análisis de Endpoints y Sincronización App/Web

_Fechas de referencia: documentación entregada (2025-01-XX) y código actual en `main`._

## 1. Resumen Ejecutivo
- **Backend expuesto:** todos los equipos deben usar `https://siga-backend-production.up.railway.app` con los mismos endpoints `/api/saas/*` y `/api/auth/*`.
- **Desalineaciones encontradas en la app:**
  1. `ApiService.updateStock()` llama a `PUT /api/saas/stock/{id}`, endpoint que no existe según `ENDPOINTS_COMPLETOS_POR_EQUIPO.md`.
  2. El flujo de inventario (`InventoryViewModel`) depende de filas en `/api/saas/stock`; si el backend no las crea junto al producto, la app queda sin datos reales.
  3. Los precios se consumen como `precioUnitario`, pero debemos asegurar que ninguna parte de la app consulte un campo inexistente (`precio`).
- **Impacto:** ni la app ni la web pueden reflejar en “tiempo real” altas/ajustes de stock mientras usemos endpoints distintos o el backend no genere stock al crear producto.

## 2. Diferencias Detectadas

| Área | Documentación | Código actual | Riesgo |
| ---- | ------------- | ------------- | ------ |
| Stock update | `POST /api/saas/stock` (crea o actualiza según `productoId` + `localId`. Acepta camel/snake case). | `ApiService.updateStock(id, cantidad)` → `PUT /api/saas/stock/{id}` vía `SaaSRepository.updateStock` y `InventoryViewModel.updateStock`. | Requests terminan en 404/405, el stock nunca se persiste ni en app ni en web. |
| Lectura de stock | `GET /api/saas/stock` con `localId` opcional. | La app lo usa, pero si el backend no devuelve filas (producto sin stock inicial) mostramos placeholders con `id` negativo. | Vista “vacía” aunque existan productos recién creados. |
| Precios | Único campo: `precioUnitario` (String). | `Product.getPrecioInt()` ya lo usa, pero debemos revisar otras pantallas (ventas, dashboards) para evitar referencias a `precio`. | Datos `null` o 0 si se consulta un campo inexistente. |
| Resync app/web | Documentación indica sincronización automática compartiendo endpoints. | Al intentar PUT inexistente, las operaciones no llegan al backend → la web tampoco verá los cambios. | Incumple la expectativa de “tiempo real”. |

## 3. Propuestas de Acciones

1. **Normalizar el endpoint de stock en la app**
   - Actualizar `ApiService.updateStock` → `postStock(StockUpdatePayload)` con el cuerpo documentado.
   - `SaaSRepository.updateStock(productoId, localId, cantidad, cantidadMinima)` debe enviar camelCase o snake_case según convenga.
   - Actualizar `InventoryViewModel.updateStock` (y cualquier otro uso) para pasar `productoId` y `localId` en lugar de `id` auto-incremental.

2. **Crear stock inicial tras crear producto (o guiar al usuario)**
   - Opciones:
     1. Después de `createProduct`, llamar automáticamente a `POST /api/saas/stock` con cantidad 0 y `cantidadMinima` default para cada local disponible.
     2. Mostrar un aviso/botón “Crear stock inicial” mientras el backend no genere filas automáticas.
   - Esto elimina la necesidad de placeholders con `id < 0` y alinea la app con la web.

3. **Verificar consumo de precios**
   - Auditar pantallas (`DashboardTile`, `SalesScreen`, etc.) para asegurar que sólo se use `precioUnitario` y se manejen valores `null`.
   - Proveer fallback visual (“Sin precio configurado”) cuando venga `null` para evitar confusiones.

4. **Confirmar filtros por empresa/local**
   - Según los documentos, todo se filtra por `usuario_comercial_id`. Validar con el backend que los tokens usados en la app tengan la empresa asignada (si no, `/stock` puede venir vacío).
   - Añadir logs temporales en `SaaSRepository` para detectar respuestas sin datos y compartirlos con backend durante el ajuste.

5. **Plan de pruebas compartido**
   - Escenarios a validar junto al equipo backend:
     1. Crear producto → crear/actualizar stock → verificar `GET /stock` en Postman, WebApp y App.
     2. Ajustar stock en web → refrescar App (`InventoryViewModel.loadInventory`).
     3. Crear stock con `cantidadMinima` distinta para confirmar que la app lo muestra.

## 4. Solicitudes para el Equipo Backend

1. Confirmar si hoy el backend crea automáticamente registros en `/stock` al crear un producto. Si no, ¿pueden habilitarlo? De lo contrario, implementaremos el paso manual desde la app.
2. Validar que `POST /api/saas/stock` acepte ambos formatos (`camelCase` y `snake_case`) tal como indica el documento.
3. Revisar si sigue existiendo algún endpoint legacy (`PUT /stock/{id}`) y, en caso negativo, considerar devolver un error aclaratorio para detectar clientes desactualizados.

## 5. Próximos Pasos (tras la discusión)
1. Ajustar `ApiService`/`SaaSRepository`/`InventoryViewModel` según lo acordado.
2. Quitar progresivamente los “placeholders” de stock una vez que el backend confirme registros reales por local.
3. Preparar pruebas end-to-end (Postman + App) para validar que web y móvil reflejan los cambios dentro de los segundos esperados.

> _Documento preparado para discusión con el equipo backend. Sin modificaciones al código hasta acordar el plan de acción._

