# Intención de la Aplicación SIGA

## 1. La Idea (El Ideal)
**SIGA (Sistema Integral de Gestión y Administración)** está concebido como una solución SaaS móvil robusta.
Su objetivo es permitir a **Administradores** y **Operadores** gestionar sus locales comerciales desde el teléfono.

### Funcionalidades Clave:
*   **Gestión de Inventario Real**: Ver productos, buscar por código, editar precios y stock.
*   **Roles Claros**:
    *   **Admin**: Ve todo (Nombre Empresa, Todos los Locales), gestiona usuarios/productos.
    *   **Operador**: Vista simplificada, limitado a su sucursal asignada.
*   **Experiencia Fluida**:
    *   Si un usuario tiene una empresa, se muestra su nombre (`nombreEmpresa`).
    *   Si tiene un local por defecto, entra directo a él (`localPorDefecto`).

---

## 2. Estado de los Fallos (Reporte de Batalla)

### A. El "Operador Fantasma" (Roles Cruzados)
*   **Problema**: Admin logueado era tratado como Operador debido a discrepancias en el texto del Rol del backend.
*   **Estado**: **CORREGIDO** (en verificación).
    *   Se implementó detección flexible de roles (busca "ADMIN" en cualquier parte del string).
    *   Se agregó debug visual (Toast) para confirmar qué rol está recibiendo la app.

### B. Datos de Productos Rotos ($0 y S/N)
*   **Problema**: Backend enviaba precios como Enteros o Strings indistintamente, rompiendo el parser de la app (que esperaba solo String).
*   **Estado**: **PERSISTE / EN INVESTIGACIÓN**.
    *   Se intentó actualizar modelo `Product` para aceptar formatos híbridos.
    *   **Resultado**: El usuario reporta que los datos siguen sin mostrarse correctamente. Requiere revisión profunda del JSON bruto del backend.

### C. Backend Incompleto (Error 404 Stock)
*   **Problema**: El endpoint `PUT /api/saas/stock/{id}` no existe.
*   **Estado**: **MITIGADO**.
    *   Se deshabilitó la actualización de stock en la UI para prevenir crashes.
    *   Se muestra un aviso al usuario indicando que el stock no se guardará hasta que el backend implemente la ruta.

### D. "Ghost Local" (Admin no veía su local)
*   **Problema**: Al entrar como Admin, la lista de locales aparecía vacía/no seleccionada.
*   **Estado**: **CORREGIDO**.
    *   Se implementó **Auto-Selección**:
        1.  Si el login trae `localPorDefecto`, se usa ese.
        2.  Si el usuario solo tiene 1 local, se selecciona automáticamente.

---

## 3. Próximos Pasos (Roadmap Inmediato)
1.  **Verificación Final**: Usuario debe hacer `git pull` e `installDebug` para confirmar que los parches funcionan en su dispositivo real.
2.  **UI Info Empresa**: Mostrar `nombreEmpresa` en el Dashboard (AppBar) para reforzar la identidad corporativa.
34.  **UX Inteligente**: Que si soy Admin de un solo local, la app entre directo a él. (Hecho: Auto-selección).
5.  **Datos Completos**: La app ahora captura `nombre`, `apellido`, `nombreEmpresa` y `ciudad` del local, alineándose 100% con el JSON del backend.

---
*Documento actualizado para reflejar la sincronización de modelos (Apellido, Ciudad) y la integración de empresa.*
