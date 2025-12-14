# SIGA Mobile App
> Última Sincronización: 13/12/2025 (Recuperación)

# SIGA (Sistema Inteligente de Gestión de Activos)
> Para que nunca te detengas. • No gestiones tu Inventario, Gestiona tu Tiempo.

<!-- Badges de estado y colores -->
<p>
  <img src="https://img.shields.io/badge/Estado-Gateando-00B4D8?style=for-the-badge" alt="Estado: Gateando" />
  <img src="https://img.shields.io/badge/Visión-El_tiempo_es_la_moneda-80FFDB?style=for-the-badge&labelColor=03045E" alt="Visión: El tiempo es la moneda" />
  <img src="https://img.shields.io/badge/Licencia-MIT-blue?style=for-the-badge" alt="Licencia: MIT" />
</p>
<p>
  <img src="https://img.shields.io/badge/Primario-03045E-03045E?style=flat-square" alt="Color Primario" />
  <img src="https://img.shields.io/badge/Acento-00B4D8-00B4D8?style=flat-square" alt="Color de Acento" />
  <img src="https://img.shields.io/badge/Acento_Sec-80FFDB-80FFDB?style=flat-square" alt="Color de Acento Secundario" />
  <img src="https://img.shields.io/badge/Neutro-FFFFFF-FFFFFF?style=flat-square" alt="Color Neutro" />
</p>

---

## 🧭 TL;DR para desarrolladores

| Ítem | Detalle |
| --- | --- |
| **Stack Mobile** | Kotlin · Jetpack Compose · Coroutines · Hilt · Retrofit/Ktor client |
| **Backend Prod** | Railway (Spring/Ktor) + PostgreSQL |
| **Credenciales demo** | `admin@test.cl` · `test123` |
| **Mínimos** | Android Studio Ladybug / AGP 8.7 · JDK 21 · Android 14 SDK |
| **Run** | `./gradlew uninstallDebug installDebug` o botón *Run App* |
| **Tests** | `./gradlew test` (usa `MainDispatcherRule`) |
| **APK Release** | `./gradlew assembleRelease` + firma con keystore propio |

> **Nota:** la app se integra en tiempo real con el backend desplegado. No uses datos de producción para pruebas destructivas.

---

## 🚀 Guía rápida de instalación

```bash
# 1. Clonar
$ git clone https://github.com/HecAguilaV/SIGA_APP.git
$ cd SIGA_APP

# 2. Configurar Java (Windows/Git Bash)
$ export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
$ export PATH="$JAVA_HOME/bin:$PATH"

# 3. Sincronizar dependencias
$ ./gradlew help

# 4. Ejecutar en dispositivo/emulador
$ ./gradlew uninstallDebug installDebug
```

> En Android Studio basta con abrir la carpeta `DevAppMobile`, esperar el *Sync*, seleccionar un emulador físico/virtual y presionar ▶️ (*Run App*).

### ⚙️ Variables y endpoints
Toda la configuración remota está centralizada en `build.gradle` (flavor `prod`). Si necesitas apuntar a un backend alternativo, actualiza las constantes dentro de `BuildConfig` o el archivo `local.properties` según corresponda.

---

## 🧪 Pruebas y calidad

- **Unit tests:**
  ```bash
  ./gradlew test
  ```
  Usa `MainDispatcherRule` (`app/src/test/java/com/example/sigaapp/testutils/MainDispatcherRule.kt`) para fijar el dispatcher principal y poder testear ViewModels con Coroutines.
- **Logs clave:** `INVENTORY_DEBUG` y `STARTUP_DEBUG` registran la sincronización de inventario y el flujo de sesión.
- **Warnings de Compose:** existen iconos de Material deprecated que no bloquean la entrega; se migrarán a `AutoMirrored` en una iteración posterior.

---

## 📱 Flujo funcional actual

1. Autenticación con credenciales de administrador (`admin@test.cl` / `test123`).
2. Selección de local, visualización de inventario en tiempo real y acciones CRUD básicas.
3. Sincronización automática con el backend cada vez que se actualiza stock/productos.
4. Persistencia local de la sesión; se puede forzar modo operador seleccionando otro local tras reiniciar.

Próximos hitos incluyen: edición de locales desde la app, biometría para re-login rápido y refinamiento del asistente SIGA (voz + IA CRUD).

---

## 📦 Firmar y distribuir APK

1. Genera/ubica tu keystore (ejemplo `release.keystore`).
2. Crea `keystore.properties` (no versionado):
   ```properties
   storeFile=../release.keystore
   storePassword=********
   keyAlias=siga
   keyPassword=********
   ```
3. Ajusta `build.gradle` para leer dichas propiedades.
4. Ejecuta el build release:
   ```bash
   ./gradlew assembleRelease
   ```
5. El APK se genera en `app/build/outputs/apk/release/app-release.apk`. Entrégalo firmado al docente junto al hash SHA-256.

---

## 📚 Documentación complementaria

- **Corazón & visión:** `docs/SIGA.md`
- **Ecosistema completo:** `docs/ECOSISTEMA_SIGA.md`
- **Guías técnicas:** revisa `docs/README.md` y las secciones `INSTRUCCIONES_BACKEND`, `SECURITY`, `MIGRACION_BACKEND` según tu rol.
- **Roadmap visual:** `docs/SIGA_Roadmap_Visual.svg`

---

## 📖 Tabla de Contenidos
- [Carta del Fundador (1 min)](#carta-del-fundador-1-min)
- [La Problemática](#la-problemática)
- [La Solución](#la-solución)
- [Propuesta de Valor](#propuesta-de-valor)
- [Identidad de Marca y Sistema de Diseño](#identidad-de-marca-y-sistema-de-diseño)
- [Visión de la Arquitectura](#visión-de-la-arquitectura)
- [Stack Tecnológico](#stack-tecnológico)
- [Modelo de datos inicial (v1)](#modelo-de-datos-inicial-v1)
- [Guía rápida para devs (TL;DR)](#guía-rápida-para-devs-tldr)
- [Flujo de trabajo con GitHub (equipo)](#flujo-de-trabajo-con-github-equipo)
- [Plan de Desarrollo (Gatear → Caminar → Correr)](#plan-de-desarrollo-gatear--caminar--correr)
- [Documentación Detallada](#documentación-detallada)
- [Únete a la Visión](#únete-a-la-visión)

---

## Carta del Fundador (1 min)
Esta idea no nació en un aula: nació en la cabina de una camioneta. Mi mayor frustración era una sola palabra: detenerme. Detenerme a contar, a cuadrar, a pelear con planillas mientras el negocio seguía sin mí. Me quitó el sueño; más de una vez lo soñé.

SIGA nace para que el emprendedor no se detenga. Una herramienta que hace el trabajo pesado y devuelve minutos reales. El tiempo es la moneda.

Más contexto humano y técnico: [Corazón de SIGA](docs/SIGA.md)

---

## La Problemática
Para muchas PYMES, la gestión de activos es parálisis operativa: sistemas complejos, planillas frágiles, fricción constante. Resultado: quiebres, mermas y pérdida de tiempo, el recurso más valioso.

---

## La Solución
SIGA es un asistente de operaciones proactivo con tres pilares:
1) **Asistente Conversacional:** Actualizar, consultar y reportar en lenguaje natural.
2) **Inteligencia Proactiva:** Anticipa quiebres y sugiere acciones con IA.
3) **Simplicidad Radical:** Interfaz clara y reportes accionables que se explican solos.

---

## Propuesta de Valor
No vendemos software; vendemos tiempo y tranquilidad.

---

## Identidad de Marca y Sistema de Diseño

### Logotipo
Cuatro variantes disponibles en `/docs/brand`: Primary (gradient), Solid, Monochrome y Reversed (blanco).

### Paleta de Colores
- Azul marino `#03045E` (principal)
- Cian `#00B4D8` (acento)
- Cian claro `#80FFDB` (acento secundario)
- Blanco `#FFFFFF` (neutro)

### Tipografía
Inter: Headings en Bold, cuerpo en Regular.

---

## Visión de la Arquitectura
- `siga.com`: Marketing y conversión.
- `app.siga.com`: Aplicación SaaS (lógica de negocio).
- **Flujo:** Interfaz (móvil/PC) → API (Ktor) → PostgreSQL → Respuesta.
- **Offline-first:** Las acciones se guardan localmente y se sincronizan al recuperar la conexión.
- Documentos técnicos (modelo 4+1 y ER) en `/docs`.

### Clientes: Web y Apps Nativas
- **Web App:** `app.siga.com`, responsive y funcional.
- **Android (Nativa):** Kotlin + Jetpack Compose.
- **iOS (Nativa):** Lógica compartida con Kotlin Multiplatform (KMM) + UI nativa en SwiftUI.
- El Asistente Conversacional será el núcleo de la experiencia móvil.

### ¿Por qué Kotlin?
Unifica el backend (Ktor) y la lógica móvil compartida (KMM), reduciendo la duplicación de código y acelerando el desarrollo.

---

## Stack Tecnológico
| Capa             | Tecnología                     | Justificación                                               |
| :--------------- | :----------------------------- | :---------------------------------------------------------- |
| Frontend Web     | Svelte + Bulma                 | UX fluida y simple.                                         |
| Mobile (Android) | Kotlin + Jetpack Compose       | Nativo y moderno.                                           |
| Mobile (iOS)     | KMM (core) + SwiftUI           | Lógica compartida, UI nativa.                               |
| Backend          | Kotlin (Ktor)                  | API robusta y de alto rendimiento.                          |
| Base de Datos    | PostgreSQL                     | Fiable y escalable.                                         |
| IA               | Google Gemini API (Kotlin SDK) | Chatbots conversacionales, insights y análisis inteligente. |

---

## Modelo de datos inicial (v1)
El diseño de la base de datos está pensado para ser robusto y escalable, con convenciones claras (español, mayúsculas). Las entidades principales son:

- **Entidades de Catálogo:** `USUARIOS`, `PRODUCTOS`, `CATEGORIAS`, `LOCALES`.
- **Entidades Operacionales:** `STOCK`, `VENTAS`, `DETALLES_VENTA`, `MOVIMIENTOS`, `ALERTAS`.

**Principios Clave:**
- **Stock por Local:** El stock no es un atributo del producto, sino una entidad separada (`STOCK`) que relaciona un `PRODUCTO` con un `LOCAL` y una `cantidad`.
- **Trazabilidad Total:** La tabla `MOVIMIENTOS` registra cada entrada, venta, merma o ajuste, creando un historial completo para cada producto.
- **Roles:** El sistema diferencia entre roles (`ADMINISTRADOR`, `VENDEDOR`) para una gestión de permisos segura.

Para ver el diseño detallado, el Modelo Entidad-Relación y el script DDL completo, consulta la documentación en la carpeta `/docs`.

---

## Guía rápida para devs (TL;DR)
- **Requisitos:** Git, Docker Desktop, Node LTS, Java 21, VS Code.
- **Variables:** Copia `.env.example` a `.env` (cuando esté disponible).
- **Base local:** `cd infra && docker compose up -d` (cuando exista `/infra`).
- **Ejecutar API:** `./gradlew run`
- **Ejecutar Web:** `npm install && npm run dev`

---

## Flujo de trabajo con GitHub (equipo)
- **Ramas:** `main` (protegida), `dev` (integración), `feature/<tarea>`.
- **Flujo:** Crear `feature` → PR a `dev` → Revisión → Merge.
- **Commits:** `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `build`.

---

## Plan de Desarrollo (Gatear → Caminar → Correr)
A continuación se presenta el plan de desarrollo visualizado del proyecto.

![Roadmap SIGA](docs/SIGA_Roadmap_Visual.svg)

---

## Documentación Detallada
La ingeniería del software está definida en los siguientes documentos dentro de la carpeta [`docs/`](./docs/):

### Documentación Principal
- [**ECOSISTEMA_SIGA.md**](./docs/ECOSISTEMA_SIGA.md) - Documentación oficial completa del ecosistema
- [**SIGA.md**](./docs/SIGA.md) - Corazón y visión de SIGA
- [**INSTRUCCIONES_BACKEND.md**](./docs/INSTRUCCIONES_BACKEND.md) - Instrucciones para desarrollar el backend

### Guías de Desarrollo
- [**GUIA_INTELLIJ_IDEA.md**](./docs/GUIA_INTELLIJ_IDEA.md) - Configuración del backend en IntelliJ IDEA
- [**MODULO_POS.md**](./docs/MODULO_POS.md) - Documentación del módulo Point of Sale
- [**SECURITY.md**](./docs/SECURITY.md) - Guía de seguridad y API keys

### Referencias Adicionales
- [**BACKEND_GUIDE.md**](./docs/BACKEND_GUIDE.md) - Opciones de implementación de backend
- [**MIGRACION_BACKEND.md**](./docs/MIGRACION_BACKEND.md) - Guía de migración a backend proxy

Para ver el índice completo, consulta [docs/README.md](./docs/README.md).

---

## Únete a la Visión
SIGA es más que un proyecto; es el inicio de una startup sencilla pero bien estructurada. Buscamos personas que compartan nuestra pasión por resolver problemas reales con tecnología.
- **Docentes guía y mentores:** Experiencia en SaaS, logística o IA.
- **Colaboradores:** Desarrolladores, diseñadores y expertos en negocios con propósito.

Si esto resuena contigo, abre un **Issue** para conversar o contáctanos.