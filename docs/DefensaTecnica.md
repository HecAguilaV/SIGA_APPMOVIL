# SIGA Mobile App — Documento Técnico de Defensa

## 1. Propósito y Alcance
SIGA es un ecosistema SaaS para operar locales físicos y canales digitales. La aplicación móvil extiende la WebApp operativa a dispositivos Android, ofreciendo inventarios en tiempo real, control de stock y un asistente contextual por voz. Este documento arma la defensa técnica solicitada para la presentación final.

## 2. Arquitectura General
| Capa | Descripción |
| --- | --- |
| Web Comercial | Registro de clientes y adquisición de suscripciones (Vercel). |
| WebApp SaaS | Gestión completa del negocio: usuarios, permisos, inventario, asistentes IA (Vercel). |
| App Mobile (este repo) | Cliente Android Kotlin/Compose que consume los mismos microservicios. |
| Backend | Microservicios Spring Boot desplegados en Railway con PostgreSQL (AlwaysData). |
| Integraciones externas | `https://mindicador.cl/api/dolar` para referencia financiera diaria. |

La app móvil comparte autenticación JWT con la WebApp. El backend expone un esquema específico para SaaS, separado del esquema comercial.

## 3. Flujos Clave
1. **Autenticación**: `POST /api/auth/login` → SessionManager almacena token y rol en DataStore.
2. **Selección de local**: `GET /api/saas/locales` → GlobalViewModel conserva el local elegido.
3. **Inventario**: `GET /api/saas/productos` + `GET /api/saas/stock` → InventoryViewModel fusiona data para CRUD.
4. **Actualización de stock**: `PUT /api/saas/stock/{productoId}` con payload validado en la UI.
5. **Indicador financiero**: `GlobalViewModel.refreshDollarIndicator()` consulta mindicador.cl vía Ktor.
6. **Asistente IA**: `ChatRepository` envía prompts al servicio LLM interno (misma base de permisos).

## 4. Tecnologías Implementadas
- **Frontend móvil**: Kotlin, Jetpack Compose, Material 3, Navigation Compose, StateFlow.
- **Networking**: Retrofit para backend propio, Ktor Client + kotlinx.serialization para API externa.
- **Persistencia local**: DataStore para tokens, rol y local por defecto.
- **Recursos nativos**: Text-to-Speech (tts) y SpeechRecognizer para dictado; infraestructura para biometría (pendiente de habilitar).
- **Tests**: JUnit + MockK + Coroutines Test (`InventoryViewModelTest`).

## 5. Seguridad y Roles
- JWT almacenado cifrado en DataStore.
- Roles disponibles: ADMINISTRADOR, OPERADOR, CAJERO (view-model filtra permisos). El operador ve solo inventario básico; administrador accede a ajustes, documentos y operaciones CRUD completas. Se contempla un cuarto rol (Supervisor) para siguiente sprint.
- Asistente IA respeta el ámbito de permisos (mismo token).

## 6. Integraciones y Endpoints
| Servicio | Método | Ruta |
| --- | --- | --- |
| Auth | POST | `/api/auth/login` |
| Locales | GET | `/api/saas/locales` |
| Productos | GET / PUT | `/api/saas/productos`, `/api/saas/productos/{id}` |
| Stock | GET / PUT | `/api/saas/stock`, `/api/saas/stock/{productoId}` |
| Ventas | GET | `/api/saas/ventas` (en WebApp y backlog móvil) |
| Indicador externo | GET | `https://mindicador.cl/api/dolar` |

## 7. Pruebas y Calidad
- `./gradlew test` ejecuta los unit tests (InventoryViewModel, MainDispatcherRule).
- Warnings controlados en Compose (únicamente íconos deprecated y `Locale` deprecado).
- QA funcional: instalación directa vía `./gradlew uninstallDebug installDebug`.

## 8. Firma y Distribución
1. Generar keystore (ejemplo en Git Bash):
   ```bash
   cd /c/Users/hdagu/Documents/DevAppMobile
   keytool -genkeypair -v \
     -keystore siga-release.jks \
     -storepass Kike4466. \
     -keypass Kike4466. \
     -alias sigaRelease \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -dname "CN=SIGA, OU=Dev, O=SIGA, L=Puerto Montt, S=Los Lagos, C=CL"
   ```
2. Referenciar el `.jks` mediante variables (`gradle.properties` o env vars) y configurar `signingConfigs` en `app/build.gradle.kts`.
3. Generar el APK:
   ```bash
   ./gradlew assembleRelease
   ```
4. Entregar `app/build/outputs/apk/release/app-release.apk`, hash SHA-256 y ubicación segura del keystore.

## 9. Cumplimiento de Requisitos
| Requisito | Estado |
| --- | --- |
| Tema contextualizado | ✓ Ecosistema SIGA (comercial + SaaS + mobile). |
| API externa | ✓ Mindicador (dólar observado). |
| Backend propio | ✓ Microservicios Spring Boot + PostgreSQL. |
| Recursos nativos | ✓ Voz (TTS) y micrófono. |
| Pruebas unitarias | ✓ `./gradlew test`. |
| APK firmado | ✓ Instrucciones + keystore (`siga-release.jks`). |
| Roles diferenciados | ✓ Admin/Operador/Cajero; expansión planificada. |
| Formularios validados | ✓ Inventario y CRUD con feedback. |
| Gestión de estado | ✓ ViewModels + StateFlow. |
| Animaciones/transiciones | ✓ Compose (BottomSheet, ripples, etc.). |

## 10. Riesgos y Próximos Pasos
1. **Registro y recuperación en mobile**: se controla desde la WebApp para evitar duplicar flujos sensibles.
2. **Biometría y notificaciones**: infraestructura ya declarada; implementación planificada en la iteración 2.
3. **Cobertura de pruebas**: añadir casos para GlobalViewModel y ChatRepository.
4. **Supervisor role**: habilitar permisos intermedios para auditoría.

## 11. Evidencias de Trabajo
- Commits en GitHub (`git shortlog -sn`): Héctor Aguila guía las integraciones con backend/web.
- Repos relacionados:
  >- `SIGA_BACKEND`https://github.com/HecAguilaV/SIGA_BACKEND.git,
   - `SIGA_WEBCOMERCIAL`https://github.com/HecAguilaV/SIGA-WEBCOMERCIAL.git
   - `SIGA_APPWEB`https://github.com/HecAguilaV/SIGA_APPWEB.git,
   - `SIGA_APP`https://github.com/HecAguilaV/SIGA_APP.git.
- Infraestructura compartida verificada por logs de despliegue en Railway y Vercel.

Con este documento y el README se cubre la defensa técnica solicitada, justificando decisiones de arquitectura, integraciones y estado actual del MVP móvil.

