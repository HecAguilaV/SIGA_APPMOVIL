# Intención de la Aplicación SIGA

## 1. La Idea (El Ideal)
**SIGA (Sistema Integral de Gestión y Administración)** está concebido como una solución SaaS móvil robusta.
Su objetivo es permitir a **Administradores** y **Operadores** gestionar sus locales comerciales desde el teléfono.

### Funcionalidades Clave Esperadas:
*   **Gestión de Inventario Real**: Ver productos, buscar por código, editar precios y *stock* en tiempo real.
*   **Roles Claros**:
    *   **Admin**: Ve todo, gestiona locales, crea usuarios/productos.
    *   **Operador**: Vista simplificada, limitado a su sucursal asignada.
*   **Ventas Ágiles**: Registrar ventas rápidamente seleccionando productos.
*   **Seguridad**: Sesiones persistentes pero seguras (Biometría), limpieza de datos al cerrar sesión.

---

## 2. Fallos Persistentes (Estado Actual - "El Infierno")
A pesar de múltiples intentos de corrección, la aplicación sufre de una desconexión crítica entre el Frontend (Móvil) y el Backend/Datos.

### A. El "Operador Fantasma" (El problema más grave)
*   **Síntoma**: Un usuario se loguea como ADMINISTRADOR (dueño), pero la aplicación lo trata como OPERADOR.
*   **Consecuencia**:
    *   Interfaz degradada (menos opciones).
    *   El usuario ve "su local" (porque el token es de admin y tiene acceso a los datos), pero la UI piensa que es un operador limitado.
    *   Sensación de "sesión corrupta" o cruzada.
*   **Causa Técnica Identificada**: El string de ROL que envía el backend (ej: "Usuario Admin", "Jefe") no coincide exactamente con el "ADMINISTRADOR" que espera la app, por lo que cae en el `else` y se asigna rol "OPERADOR".

### B. Datos de Productos Rotos ($0 y S/N)
*   **Síntoma**: Los productos aparecen en la lista pero dicen "Producto s/n" (Sin Nombre) y Precio "$0".
*   **Causa**: Mismatch en el modelo de datos JSON.
    *   Backend envía `precio` (Int) o `precioUnitario` formateado de forma inesperada.
    *   La app esperaba `precioUnitario` (String). Al no coincidir, el parser falla silenciosamente y muestra objetos vacíos.

### C. Backend Incompleto (Error 404)
*   **Síntoma**: Al intentar guardar un cambio de Stock, la app crasheaba.
*   **Causa**: El endpoint `PUT /api/saas/stock/{id}` **no existe** en el backend.

---

## 3. El Ideal (La Meta)
Para que SIGA sea funcional y aprobable, debe ocurrir lo siguiente:

1.  **Sincronización Total de Roles**: Que "Admin" sea "Admin" en todas partes. (Solucionado en código v2 con búsqueda flexible de texto "ADMIN").
2.  **Robustez de Datos**: Que la app "comas lo que le echen" (Int o String) en los precios para mostrarlos siempre. (Solucionado en código v2 con modelo híbrido).
3.  **Endpoints Reales**: Que el botón "Guardar" llame a una API que realmente exista y responda 200 OK.
4.  **UX Inteligente**: Que si soy Admin de un solo local, la app entre directo a él, en lugar de mostrarme una pantalla blanca esperando que seleccione un dropdown invisible.

---
*Este documento fue generado por solicitud del usuario para dejar constancia de la brecha entre la intención y la ejecución actual.*
