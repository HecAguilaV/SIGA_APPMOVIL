# Visión Completa del Ecosistema SIGA

**Fecha**: Diciembre 2024  
**Objetivo**: Documentar la visión completa del ecosistema SIGA y el flujo de decisiones

---

## 🎯 FILOSOFÍA DEL SISTEMA

### SIGA: Sistema Multifuncional para PYMEs

SIGA está diseñado para **PYMEs** donde:
- Los empleados son **multifuncionales**
- La confianza se gana con el tiempo
- No hay tiempo para trabajo de oficina complejo
- El asistente IA es el **orquestador** que facilita todo

### WebApp: El Corazón del Sistema

**WebApp es el actor padre del ecosistema**:
- ✅ Es donde se toman **todas las decisiones de negocio**
- ✅ Es donde el dueño (admin) gestiona su empresa
- ✅ Es donde se crean y gestionan usuarios operativos
- ✅ Es donde se asignan permisos según confianza
- ✅ Es donde se usa el asistente IA para operaciones diarias
- ✅ Es donde se generan reportes y análisis

**WebApp NO es una página de prueba**, es el sistema operativo completo.

---

## 🏗️ ARQUITECTURA DEL ECOSISTEMA

```
┌─────────────────────────────────────────────────────────┐
│  WEB COMERCIAL (Portal de Suscripciones)               │
│  - Registro de clientes                                 │
│  - Adquisición de suscripciones                         │
│  - Botón "Acceder a WebApp" (SSO)                       │
│  - NO gestiona usuarios operativos                      │
│  - NO toma decisiones de negocio                        │
└─────────────────────────────────────────────────────────┘
                        │
                        │ SSO (Token Exchange)
                        ▼
┌─────────────────────────────────────────────────────────┐
│  WEBAPP (Sistema Operativo - CORAZÓN DEL SISTEMA)      │
│  ✅ Gestión completa del negocio                        │
│  ✅ Creación y gestión de usuarios operativos           │
│  ✅ Asignación de permisos según confianza              │
│  ✅ Asistente IA para operaciones diarias              │
│  ✅ Reportes y análisis                                 │
│  ✅ Toma de decisiones de negocio                       │
│  ✅ Gestión de inventario, stock, ventas               │
└─────────────────────────────────────────────────────────┘
                        │
                        │ Misma autenticación
                        ▼
┌─────────────────────────────────────────────────────────┐
│  APP MÓVIL (Extensión Móvil)                           │
│  - Acceso móvil al sistema                              │
│  - Mismas funcionalidades que WebApp                    │
│  - Respeta permisos del usuario                         │
│  - NO gestiona usuarios (solo admin en WebApp)         │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 FLUJO COMPLETO DEL ECOSISTEMA

### 1. Registro y Suscripción (Web Comercial)

```
Usuario → Web Comercial
  → Se registra como cliente
  → Adquiere suscripción
  → Backend crea automáticamente usuario operativo ADMINISTRADOR
  → Ve botón "Acceder a WebApp"
```

**Decisión**: El usuario comercial (dueño) se convierte automáticamente en ADMINISTRADOR operativo.

### 2. Acceso a WebApp (SSO)

```
Admin (Dueño) → Click "Acceder a WebApp" en Web Comercial
  → Backend intercambia token comercial por token operativo
  → Redirige a WebApp con token en URL
  → WebApp extrae token y lo guarda
  → Admin accede a WebApp sin login adicional
```

**Decisión**: Flujo fluido para admin, sin necesidad de login adicional.

### 3. Gestión de Usuarios Operativos (WebApp - Solo Admin)

```
Admin → WebApp → Gestión de Usuarios
  → Crea usuario OPERADOR (juan@empresa.cl)
    - Email diferente
    - Password diferente
    - Rol: OPERADOR
    - Permisos por defecto: PRODUCTOS_CREAR, STOCK_ACTUALIZAR, etc.
  
  → Crea usuario CAJERO (maria@empresa.cl)
    - Email diferente
    - Password diferente
    - Rol: CAJERO
    - Permisos por defecto: VENTAS_CREAR, PRODUCTOS_VER, etc.
```

**Decisión**: Cada empleado tiene credenciales propias, seguridad real.

### 4. Asignación de Permisos Adicionales (WebApp - Solo Admin)

```
Admin → WebApp → Gestión de Usuarios → Ver Permisos de CAJERO
  → Asigna permiso REPORTES_VER (CAJERO de confianza)
  → Asigna permiso ANALISIS_IA (CAJERO puede pedir análisis)
  → CAJERO ahora puede ver reportes y solicitar análisis
```

**Decisión**: Permisos flexibles según confianza y necesidades del negocio.

### 5. Operaciones Diarias (WebApp y App Móvil)

```
OPERADOR → WebApp/App Móvil
  → Ve productos, stock
  → Crea productos nuevos (tiene permiso por defecto)
  → Actualiza stock
  → Usa asistente IA: "¿Qué productos debería comprar?"
  → Asistente genera análisis (tiene permiso ANALISIS_IA)

CAJERO → WebApp/App Móvil
  → Ve productos (solo para vender)
  → Crea ventas
  → Si tiene permiso REPORTES_VER: ve reportes
  → Si tiene permiso ANALISIS_IA: puede pedir análisis
```

**Decisión**: Cada usuario opera según sus permisos, el asistente IA respeta permisos.

---

## 🎭 ROLES Y PERMISOS

### ADMINISTRADOR (Dueño)

**Permisos**: TODOS (no se pueden restringir)

**Responsabilidades**:
- ✅ Gestionar usuarios operativos
- ✅ Asignar permisos según confianza
- ✅ Ver reportes y costos
- ✅ Tomar decisiones de negocio
- ✅ Configurar el sistema

**Acceso**: SSO desde Web Comercial (flujo fluido)

### OPERADOR (Encargado de Inventario)

**Permisos por defecto**:
- ✅ `PRODUCTOS_VER`
- ✅ `PRODUCTOS_CREAR` ⭐ (según experiencia real)
- ✅ `PRODUCTOS_ACTUALIZAR`
- ✅ `STOCK_VER`
- ✅ `STOCK_ACTUALIZAR`
- ✅ `LOCALES_VER`
- ✅ `CATEGORIAS_VER`
- ✅ `ASISTENTE_USAR`
- ✅ `ANALISIS_IA` (puede pedir análisis de productos más/menos vendidos)

**Permisos que NO tiene por defecto** (pero admin puede asignar):
- ❌ `PRODUCTOS_ELIMINAR` (solo admin)
- ❌ `VENTAS_VER` (pero puede pedir análisis de ventas por IA)
- ❌ `REPORTES_VER` (pero puede pedir análisis por IA)
- ❌ `COSTOS_VER` (solo admin)

**Acceso**: Login directo en WebApp/App Móvil

### CAJERO (Vendedor)

**Permisos por defecto**:
- ✅ `PRODUCTOS_VER` (solo para vender)
- ✅ `STOCK_VER` (solo para verificar disponibilidad)
- ✅ `VENTAS_CREAR`
- ✅ `VENTAS_VER` (solo sus ventas)
- ✅ `ASISTENTE_USAR`

**Permisos que NO tiene por defecto** (pero admin puede asignar):
- ❌ `PRODUCTOS_CREAR` (pero admin puede asignarlo si es necesario)
- ❌ `STOCK_ACTUALIZAR` (solo admin/operador)
- ❌ `REPORTES_VER` (pero admin puede asignarlo si es de confianza)
- ❌ `ANALISIS_IA` (pero admin puede asignarlo si es de confianza)
- ❌ `COSTOS_VER` (solo admin)

**Acceso**: Login directo en WebApp/App Móvil

---

## 🤖 ASISTENTE IA: El Orquestador

### Filosofía

El asistente IA es el **corazón operativo** del sistema:
- ✅ Facilita operaciones sin interfaces complejas
- ✅ Respeta permisos del usuario
- ✅ Permite análisis según permisos
- ✅ Ejecuta CRUD según permisos

### Casos de Uso Reales

**OPERADOR**:
```
OPERADOR: "Agrega producto X con precio Y"
Asistente: ✅ Crea producto (tiene permiso PRODUCTOS_CREAR)

OPERADOR: "¿Qué productos se venden más?"
Asistente: ✅ Genera análisis (tiene permiso ANALISIS_IA)

OPERADOR: "Actualiza stock de producto X a 50 unidades"
Asistente: ✅ Actualiza stock (tiene permiso STOCK_ACTUALIZAR)
```

**CAJERO sin permisos adicionales**:
```
CAJERO: "¿Qué productos se venden más?"
Asistente: ❌ "No tienes permiso para solicitar análisis. Contacta al administrador."

CAJERO: "Crea venta de producto X"
Asistente: ✅ Crea venta (tiene permiso VENTAS_CREAR)
```

**CAJERO con permisos adicionales** (admin asignó):
```
CAJERO: "¿Qué productos se venden más?"
Asistente: ✅ Genera análisis (admin le asignó permiso ANALISIS_IA)

CAJERO: "Muéstrame reporte de ventas del mes"
Asistente: ✅ Muestra reporte (admin le asignó permiso REPORTES_VER)
```

---

## 💡 DECISIONES DE NEGOCIO

### ¿Por qué OPERADOR puede crear productos?

**Decisión basada en experiencia real**:
- El dueño compra productos nuevos
- El OPERADOR (encargado de inventario) los agrega al sistema
- Es más eficiente que el dueño tenga que agregar cada producto

### ¿Por qué permisos flexibles?

**Decisión basada en realidad PYME**:
- CAJERO de confianza puede ayudar con reportes
- OPERADOR puede hacer análisis para compras
- Permisos se ajustan según necesidad y confianza

### ¿Por qué WebApp es el centro de decisiones?

**Decisión arquitectónica**:
- WebApp es donde el dueño gestiona su negocio
- WebApp es donde se toman decisiones operativas
- WebApp es donde se configura el sistema
- Web Comercial solo es el portal de suscripciones

---

## 🎯 CONCLUSIÓN

**SIGA es un ecosistema completo** donde:
- **Web Comercial**: Portal de suscripciones
- **WebApp**: Sistema operativo completo (corazón del sistema)
- **App Móvil**: Extensión móvil del sistema

**WebApp es el actor padre** donde:
- Se toman todas las decisiones de negocio
- Se gestionan usuarios y permisos
- Se opera el negocio día a día
- Se usa el asistente IA como orquestador

**El sistema de permisos granular** permite:
- Flexibilidad real para PYMEs
- Seguridad sin perder funcionalidad
- Adaptación según confianza y necesidad

---

**Visión completa del ecosistema SIGA**
