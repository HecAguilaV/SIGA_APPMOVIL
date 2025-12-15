# SIGA Mobile App

Aplicación Android oficial del ecosistema SIGA. Esta versión corresponde a la primera etapa funcional: autenticación, selección de local e inventario sincronizado en tiempo real con el backend productivo.

---

## 0. Información general
| Elemento                  | Descripción                                         |
|---------------------------|-----------------------------------------------------|
| Nombre de la app          | SIGA Mobile App                                     |
| Integrantes               | Héctor Aguila V. – Rol: Fullstack / Mobile          |
| Repositorio backend       | https://github.com/HecAguilaV/SIGA_BACKEND.git      |
| Repositorio web comercial | https://github.com/HecAguilaV/SIGA-WEBCOMERCIAL.git |
| Repositorio app web       | https://github.com/HecAguilaV/SIGA_APPWEB.git       |

---

## 1. Resumen ejecutivo
- **Estado**: MVP operativo (inventario y stock en línea).
- **Integraciones activas**:
  - Web comercial: https://siga-web.vercel.app/
  - Web App SaaS: https://siga-appweb.vercel.app/
  - Backend Kotlin + PostgreSQL en Railway: https://siga-backend-production.up.railway.app
  - Base de datos en AlwaysData (PostgreSQL) dividida en dos esquemas: uno para la web comercial y otro dedicado al SaaS (app móvil + web app).
  - **API externa**: https://mindicador.cl/api/dolar (indicador financiero diario consumido vía Ktor).
- **Rol principal**: Administradores pueden revisar locales, productos y stock actualizados desde la app o la Web App con la misma API.

## 1.1 Arquitectura del ecosistema
```
┌─────────────────────────────────────────────────────────┐
│  WEB COMERCIAL (Portal de Suscripciones)                │
│  - Registro de clientes                                 │
│  - Adquisición de suscripciones                         │
│  - Botón "Acceder a WebApp" (SSO)                       │
│  - NO gestiona usuarios operativos                      │
│  - NO toma decisiones de negocio                        │
└─────────────────────────────────────────────────────────┘
                        ��
                        │ SSO (Token Exchange)
                        ▼
┌─────────────────────────────────────────────────────────┐
│  WEBAPP (Sistema Operativo - CORAZÓN DEL SISTEMA)       │
│  ✓ Gestión completa del negocio                         │
│  ✓ Creación y gestión de usuarios operativos            │
│  ✓ Asignación de permisos según confianza               │
│  ✓ Asistente IA para operaciones diarias                │
│  ✓ Reportes y análisis                                  │
│  ✓ Toma de decisiones de negocio                        │
│  ✓ Gestión de inventario, stock, ventas                 │
└─────────────────────────────────────────────────────────┘
                        │
                        │ Misma autenticación
                        ▼
┌─────────────────────────────────────────────────────────┐
│  APP MÓVIL (Extensión Móvil)                            │
│  - Acceso móvil al sistema                              │
│  - Mismas funcionalidades que WebApp                    │
│  - Respeta permisos del usuario                         │
│  - NO gestiona usuarios (solo admin en WebApp)          │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Funcionalidades principales
1. Autenticación y persistencia de sesión (DataStore).
2. Navegación dashboard → inventario con selección de local.
3. CRUD de productos y categorías con validaciones y feedback háptico.
4. Sincronización en línea contra backend SaaS (stock y precios en tiempo real).
5. Reportes rápidos (alerta de stock bajo, totales) y permisos por rol.
6. Asistente TTS (infraestructura lista) y futuras integraciones: biometría + notificaciones.

---

## 3. Requisitos
| Componente | Versión recomendada |
| ---------- | ------------------- |
| Android Studio | Ladybug o superior |
| Android Gradle Plugin | 8.7.x |
| Kotlin | 1.9+ |
| JDK | 21 (incluido en Android Studio JBR) |
| SDK objetivo | API 34 (Android 14) |

---

## 4. Configuración y ejecución
```bash
# Clonar
git clone https://github.com/HecAguilaV/SIGA_APP.git
cd SIGA_APP

# (Opcional) configurar JAVA_HOME en Git Bash / PowerShell
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
export PATH="$JAVA_HOME/bin:$PATH"

# Verificar dependencias
./gradlew help

# Instalar en dispositivo o emulador
./gradlew uninstallDebug installDebug
```
En Android Studio basta con abrir la carpeta `DevAppMobile`, esperar el sincronizado y pulsar *Run App*.

---

## 5. Credenciales de prueba
| Usuario | Rol | Contraseña |
| ------- | --- | ---------- |
| `admin@test.cl` | Administrador | `test123` |

> Usa entornos personales para pruebas destructivas. El backend productivo está compartido con la Web App.

---

## 6. Endpoints consumidos (API propia)
Base URL: `https://siga-backend-production.up.railway.app`

| Servicio | Método | Ruta | Uso en la app |
| -------- | ------ | ---- | ------------- |
| Autenticación | POST | `/api/auth/login` | Inicio de sesión y obtención de token JWT |
| Locales | GET | `/api/saas/locales` | Listado de locales disponibles por empresa |
| Productos | GET | `/api/saas/productos` | Catálogo de productos y precios |
| Stock | GET | `/api/saas/stock` | Cantidades por producto/local |
| Stock | PUT | `/api/saas/stock/{productoId}` | Actualización de stock puntual (rol admin) |
| Productos | PUT | `/api/saas/productos/{id}` | Ajuste de nombre/precio desde la app |

Los datos de ambos clientes (web y móvil) se sirven desde el mismo backend y reposan en la base PostgreSQL alojada en AlwaysData (esquema comercial + esquema SaaS).

---

## 7. Ejecución de pruebas
```bash
./gradlew test
```
- `MainDispatcherRule` fija el dispatcher principal para ViewModels basados en coroutines.
- Los tests actuales se centran en el flujo de inventario; futuras etapas cubrirán biometría, notificaciones y asistente por voz.

---

## 8. Firma y distribución
1. Genera un keystore (`keytool -genkeypair ...`).
2. Crea `keystore.properties` (mantener fuera del control de versiones).
3. Configura `build.gradle.kts` para leer esas propiedades.
4. Ejecuta:
   ```bash
   ./gradlew assembleRelease
   ```
5. El APK firmado se genera como `app-release.apk` en `app/build/outputs/apk/release/` (nombre por defecto de Gradle).
6. Guarda el `.jks` en un lugar seguro (`/keystore/siga-release.jks` recomendado pero fuera del repo) y documenta la ruta real en la entrega.
7. Incluye este APK firmado en la carpeta de entregables indicada por la cátedra.

Entrega el APK junto con su hash SHA-256 y las credenciales anteriores para evaluación.

---

## 9. Roadmap inmediato
| Iteración | Objetivo |
| --------- | -------- |
| 1 (actual) | Inventario en línea con CRUD básico y sesiones persistentes. |
| 2 | Gestión de locales desde la app, biometría para reingreso rápido, limpieza de advertencias Compose. |
| 3 | Asistente SIGA con lenguaje natural (voz/texto) y notificaciones push. |

---

## 10. Stack técnico de la app
- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp / Ktor client
- **Coroutines**: Flow, StateFlow, ViewModelScope
- **Persistencia**: DataStore para sesión (en desarrollo Room para caché offline)
- **Testing**: JUnit5, MockK, Turbine (cuando aplique)

---

## 11. Evidencia de colaboración
`git shortlog -sn`
```
39  Héctor Aguila
12  HecAguilaV
1   Hec Aguila
```
Cada commit incluye mensajes claros (en español) y se coordina con la Web App/Backend.

---

## 12. Contribuciones
1. Crea un branch `feature/<tarea>`.
2. Agrega pruebas y actualiza README si la funcionalidad lo requiere.
3. Ejecuta `./gradlew test` antes del PR.

---
>Héctor Aguila
>>Un Soñador con Poca Ram