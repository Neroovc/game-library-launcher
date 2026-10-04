# ROADMAP.md

## Game Library / Launcher — Ruta de trabajo

**Documento:** ruta de ejecución derivada de `SPECS.md` (94 secciones).
**Estado:** planificación. La implementación todavía no ha comenzado.
**Alcance:** aplicación Android personal, un solo usuario, sin backend propio (§1, §64, §65).
**Regla de lectura:** cada ítem de trabajo cita la sección de `SPECS.md` que lo origina (§n). Si una fase y la especificación discrepan, prevalece `SPECS.md` y este documento se corrige.

---

## 0. Convenciones de este documento

- Los ítems son unidades construibles, no objetivos vagos.
- La nomenclatura de clases, tablas y columnas no es cerrada (§94); se cita la nomenclatura conceptual de la especificación.
- Cada fase declara explícitamente qué **no** contiene.
- La línea de corte del MVP se define en la sección «Línea de corte del MVP».

---

## Phase 0 — Fundaciones del proyecto e invariantes

**Objetivo:** proyecto Android Kotlin compilable con capas, instrumentación de tests y logging, sin funcionalidad de usuario.

Trabajo:

- Crear el proyecto Gradle con la estructura de módulos de §3.3 (`app/`, `core/*`, `feature/*`, `engine/*`).
- Configurar Kotlin, Jetpack Compose, AndroidX, Room, Coroutines/Flow, ViewModel, Navigation Compose, DataStore, WorkManager (§3.1).
- Configurar el cliente HTTP base con timeout, cancelación por coroutines, retry limitado y ausencia de bloqueo del hilo UI (§28, §46).
- Definir el modelo de errores de red `NetworkError`/`HttpError`/`RateLimitError`/`ParseError`/`AuthError`/`ProviderUnavailable`/`UnknownError` (§28).
- Implementar el sistema de logging por categorías `DATABASE`, `SCANNER`, `MATCHING`, `METADATA`, `PROVIDER`, `LAUNCHER`, `PLAYTIME`, `UI`, `STORAGE`, con verbosidad distinta en debug y release y sin registrar tokens, contraseñas ni cookies (§70).
- Definir la versión inicial del schema Room y el esqueleto de migraciones explícitas, sin `fallbackToDestructiveMigration()` en producción (§78).
- Montar la pirámide de tests de §60: unitarios, de integración, de UI y de migración.
- Declarar los permisos mínimos de la app y justificar cada uno; no declarar permisos especulativos (§43, §92).
- Preparar el esqueleto de `LaunchAdapter` sin implementarlo todavía (§9, §84).

Secciones cubiertas: §3, §28, §43, §46, §60, §70, §78, §92.

Fuera de esta fase:

- No hay entidades de dominio de usuario.
- No hay scanner, ni launcher, ni providers.
- No hay lógica de matching ni agregación.

Criterios de salida:

- [x] El proyecto compila y las cuatro categorías de test de §60 ejecutan en verde.
- [x] Existe una migración Room probada desde el schema inicial (§78).
- [x] Existe una prueba de integración que demuestra el logging categorizado sin datos sensibles (§70).
- [x] El esqueleto de `LaunchAdapter` compila con `canLaunch()` y `launch()` sin implementaciones reales (§9).
