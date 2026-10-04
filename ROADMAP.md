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

---

## Phase 1 — Núcleo de dominio: Game, tags, estado personal

**Objetivo:** catálogo funcional con creación manual, metadata personal y Library/Detail navegables sin red.

Trabajo:

- Implementar la entidad `Game` con los campos funcionales mínimos y los recomendados (§23).
- Usar identificadores internos estables (UUID), nunca derivados solo de título, ruta, `packageName` o RJ (§39).
- Modelar `GameStatus` (`PENDING`, `PLAYING`, `COMPLETED`, `ABANDONED`, `ON_HOLD`) independiente de la disponibilidad (§4.2).
- Modelar la disponibilidad derivada `PLAYABLE`, `CATALOG_ONLY`, `INSTALLATION_MISSING` como resultado de consulta, no como columna de `Game` (§4.3, §36).
- Implementar `Tag`, `GameTag` con `sourceType` (`EXTERNAL`, `PERSONAL`, `SYSTEM`), `providerId`, `confidence`, `isPersonal` (§21).
- Mantener engine, plataforma y disponibilidad en columnas/entidades propias; los tags no sustituyen campos estructurados (§21).
- Implementar `Rating` personal separado de cualquier rating externo (§51).
- Implementar `Asset` con `type`, `uri`, `sourceUrl`, `providerId`, dimensiones y checksum (§24).
- Crear DAOs separados por aggregate: `GameDao`, `TagDao`, `AssetDao` (§26).
- Crear `GameRepository` que exponga modelos de dominio y `Flow`; ningún ViewModel ejecuta SQL (§26).
- Implementar la pantalla Library con filtros y orden de §35, y la pantalla Game Detail con los bloques de §35.
- Implementar el editor de metadata manual con `source = MANUAL`, válido aunque no exista ningún provider (§31).
- Implementar el campo `favorite` y `hidden` como datos personales (§2.4, §23).
- Derivar `totalPlaytimeMs` como caché; la fuente de verdad es `PlaySession` (§23).

Secciones cubiertas: §2.2, §2.4, §4.1, §4.2, §4.3, §21, §23, §24, §26, §31, §35, §36, §39, §51.

Fuera de esta fase:

- No existe todavía `Installation`; por tanto `Playable` siempre resuelve a `CATALOG_ONLY` (§4.3).
- No hay `PlaySession` ni estadísticas de juego (§6).
- No hay provenance: todavía no hay fuentes externas (§20).
- No hay identificación de engines más allá del valor manual (§7, §8).

Criterios de salida:

- [ ] Se puede crear un `Game` manualmente, verlo en Catalog y editar sus campos personales (§81).
- [ ] Los datos personales sobreviven a cualquier escritura posterior sin intervención externa (§2.4, §77).
- [ ] Library filtra y ordena con los criterios de §35 sin acceso a red (§2.1).
- [ ] Un test de integración cubre que `totalPlaytimeMs` es derivado y no la fuente de verdad (§23).
- [ ] La pantalla Library no ejecuta SQL desde el ViewModel (§26).
