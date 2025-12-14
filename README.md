# SIGA Mobile App

Aplicación Android oficial del ecosistema SIGA. Esta versión corresponde a la primera etapa funcional: autenticación, selección de local e inventario sincronizado en tiempo real con el backend productivo.

---

## 1. Resumen ejecutivo
- **Estado**: MVP operativo (inventario y stock en línea).
- **Integraciones activas**:
  - Web comercial: https://siga-web.vercel.app/
  - Web App SaaS: https://siga-appweb.vercel.app/
  - Backend Kotlin + PostgreSQL en Railway: https://siga-backend-production.up.railway.app
  - Base de datos en AlwaysData (PostgreSQL) dividida en dos esquemas: uno para la web comercial y otro dedicado al SaaS (app móvil + web app).
- **Rol principal**: Administradores pueden revisar locales, productos y stock actualizados desde la app o la Web App con la misma API.

## 1.1 Arquitectura del ecosistema
```
┌─────────────────────────────────────────────────────────┐
│  WEB COMERCIAL (Portal de Suscripciones)               │
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
│  WEBAPP (Sistema Operativo - CORAZÓN DEL SISTEMA)      │
│  ✓ Gestión completa del negocio                        │
│  ✓ Creación y gestión de usuarios operativos           │
│  ✓ Asignación de permisos según confianza              │
│  ✓ Asistente IA para operaciones diarias               │
│  ✓ Reportes y análisis                                 │
│  ✓ Toma de decisiones de negocio                       │
│  ✓ Gestión de inventario, stock, ventas                │
└─────────────────────────────────────────────────────────┘
                        │
                        │ Misma autenticación
                        ▼
┌─────────────────────────────────────────────────────────┐
│  APP MÓVIL (Extensión Móvil)                           │
│  - Acceso móvil al sistema                              │
│  - Mismas funcionalidades que WebApp                    │
│  - Respeta permisos del usuario                         │
│  - NO gestiona usuarios (solo admin en WebApp)          │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Requisitos
| Componente | Versión recomendada |
| ---------- | ------------------- |
| Android Studio | Ladybug o superior |
| Android Gradle Plugin | 8.7.x |
| Kotlin | 1.9+ |
| JDK | 21 (incluido en Android Studio JBR) |
| SDK objetivo | API 34 (Android 14) |

---

## 3. Configuración rápida
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

## 4. Credenciales de prueba
| Usuario | Rol | Contraseña |
| ------- | --- | ---------- |
| `admin@test.cl` | Administrador | `test123` |

> Usa entornos personales para pruebas destructivas. El backend productivo está compartido con la Web App.

---

## 5. Endpoints consumidos
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

## 6. Ejecución de pruebas
```bash
./gradlew test
```
- `MainDispatcherRule` fija el dispatcher principal para ViewModels basados en coroutines.
- Los tests actuales se centran en el flujo de inventario; futuras etapas cubrirán biometría, notificaciones y asistente por voz.

---

## 7. Firma y distribución
1. Genera un keystore (`keytool -genkeypair ...`).
2. Crea `keystore.properties` (mantener fuera del control de versiones).
3. Configura `build.gradle.kts` para leer esas propiedades.
4. Ejecuta:
   ```bash
   ./gradlew assembleRelease
   ```
5. El APK firmado se genera en `app/build/outputs/apk/release/`.

Entrega el APK junto con su hash SHA-256 y las credenciales anteriores para evaluación.

---

## 8. Roadmap inmediato
| Iteración | Objetivo |
| --------- | -------- |
| 1 (actual) | Inventario en línea con CRUD básico y sesiones persistentes. |
| 2 | Gestión de locales desde la app, biometría para reingreso rápido, limpieza de advertencias Compose. |
| 3 | Asistente SIGA con lenguaje natural (voz/texto) y notificaciones push. |

---

## 9. Stack técnico de la app
- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp / Ktor client
- **Coroutines**: Flow, StateFlow, ViewModelScope
- **Persistencia**: DataStore para sesión (en desarrollo Room para caché offline)
- **Testing**: JUnit5, MockK, Turbine (cuando aplique)

---

## 10. Contribuciones
1. Crea un branch `feature/<tarea>`.
2. Agrega pruebas y actualiza README si la funcionalidad lo requiere.
3. Ejecuta `./gradlew test` antes del PR.
4. Describe claramente los cambios y el impacto en backend/web.

Para dudas sobre el backend o la web app, coordina con el equipo principal; todas las capas están desplegadas y sincronizadas.
