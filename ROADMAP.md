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

- [ ] El proyecto compila y las cuatro categorías de test de §60 ejecutan en verde.
- [ ] Existe una migración Room probada desde el schema inicial (§78).
- [ ] Existe una prueba de integración que demuestra el logging categorizado sin datos sensibles (§70).
- [ ] El esqueleto de `LaunchAdapter` compila con `canLaunch()` y `launch()` sin implementaciones reales (§9).

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

---

## Phase 2 — Installation, SAF, scanner y disponibilidad

**Objetivo:** descubrir instalaciones reales en el dispositivo y derivar la vista Playable sin perder el catálogo.

Trabajo:

- Implementar la entidad `Installation` con los campos de §5, incluidos `launchAdapterId`, `runtimeId`, `hash`, `sizeBytes`.
- Implementar `InstallationType` (`ANDROID_APK`, `ANDROID_APP_REFERENCE`, `LOCAL_FOLDER`, `LOCAL_WEB_GAME`, `ARCHIVE`, `EXTERNAL_RUNTIME`, `UNKNOWN`) y `Platform` (`ANDROID`, `WINDOWS`, `LINUX`, `MACOS`, `WEB`, `OTHER`, `UNKNOWN`) como campos independientes (§5).
- Implementar la selección de carpetas raíz mediante Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`) y persistencia de permisos de URI (§10).
- Implementar el scanner con el ciclo de vida completo de §33: `DISCOVERED` → `INSPECTING` → `MATCHED`/`UNMATCHED` → `UPDATED`/`ERROR`.
- Implementar los ocho pasos funcionales del scanner de §10, incluida la regla «nunca borrar metadata por no encontrar temporalmente un archivo» (§10, §32).
- Implementar el escaneo de APKs: `packageName`, `versionName`, `versionCode`, `applicationLabel`, icono, URI, digest de certificado opcional (§11).
- Separar `InstalledApp` de `APKFile`: un APK archivado puede existir sin estar instalado (§11).
- Tratar `packageName` como identificador técnico prioritario para instalaciones Android (§11).
- Implementar `InstallationState` (`UNKNOWN`, `DISCOVERED`, `INSTALLED`, `MISSING`, `UNSUPPORTED`, `BROKEN`, `REMOVED`) (§62).
- Implementar `TitleNormalizer` conservando `originalTitle`, `normalizedTitle` y `aliases[]`, sin transformación destructiva (§13).
- Implementar la extracción y canonicalización de RJ (`RJ0145678`, `rj0145678`, `RJ-0145678`, `DLsite RJ0145678` → `identifierType = DLSITE_RJ`, `identifierValue = RJ0145678`) (§14).
- Implementar la tabla `ExternalId` y la resolución de IDs en el matching local, con los tipos iniciales de §15 y esquema extensible (§15).
- Implementar el cálculo de disponibilidad `PLAYABLE`/`CATALOG_ONLY`/`INSTALLATION_MISSING` como consulta derivada sobre instalaciones (§2.3, §36).
- Implementar las reglas de borrado: borrar una instalación marca la instalación como no disponible y conserva `Game`, metadata, tags, playtime e historial; un rescan nunca borra juegos no encontrados (§2.2, §32, §54).
- Exigir confirmación explícita al borrar un `Game`, con la opción de borrarlo junto con sus instalaciones (§32).

Secciones cubiertas: §2.2, §2.3, §5, §10, §11, §12 (orden de matching local), §13, §14, §15, §32, §33, §36, §39, §54, §55, §62.

Fuera de esta fase:

- No hay ejecución de juegos todavía: la detección de engine no implica capacidad de lanzamiento (§40).
- No hay `PlaySession`; borrar y reinstalar una instalación aún no tiene historial que preservar (§6).
- No hay consulta a providers; el autoservicio de metadata posterior al scan queda pendiente (§34).
- No hay `Collection`/`CollectionItem` todavía (§37).

Criterios de salida:

- [ ] Se detecta una instalación y se la asocia a un `Game` existente (§81).
- [ ] Library distingue Catalog de Playable con cifras consistentes (§36, §81).
- [ ] Borrar una `Installation` no borra el `Game` (§32, §54, §81).
- [ ] Un rescan sobre una carpeta sin el archivo previo no borra metadata (§10, §32).
- [ ] `TitleNormalizer` tiene tests de entradas preservadas y variantes de RJ equivalentes (§13, §14, §60).
- [ ] `Playable` se deriva por consulta, no se persiste como verdad única (§4.3).

---

## Phase 3 — Launch system

**Objetivo:** lanzar las instalaciones compatibles y explicar los fallos en lenguaje humano.

Trabajo:

- Implementar la interfaz `LaunchAdapter` con `canLaunch(installation)` y `launch(installation): LaunchResult` (§9).
- Implementar `AndroidPackageLaunchAdapter` vía `PackageManager` e intent, manejando `ActivityNotFoundException` (§9).
- Declarar `<queries>` solo para los paquetes que la app necesite consultar de forma explícita, en lugar de ampliar la visibilidad global (§9, §43).
- Implementar `BrowserLaunchAdapter` para contenido web remoto (§9, §41).
- Implementar `WebGameLaunchAdapter` para `LOCAL_WEB_GAME` con `index.html`, `assets/`, `js/`, `css/`; usar WebView solo cuando sea necesario y técnicamente seguro (§41).
- Implementar `FileLaunchAdapter` y `UnsupportedLaunchAdapter` como resultado de fallo controlado, no como excepción no capturada (§9, §63).
- Implementar `LaunchResult` completo: `Success`, `InstallationMissing`, `RuntimeMissing`, `UnsupportedPlatform`, `PackageNotInstalled`, `PermissionDenied`, `ActivityNotFound`, `ExternalRuntimeError`, `UnknownError` (§63).
- Registrar en logs las categorías `LAUNCHER` y `STORAGE` sin volcar rutas sensibles ni contenido de archivos (§43, §70).
- Verificar que un engine detectado no produce disponibilidad artificial: engine clasifica, runtime habilita (§7, §40).
- Tratar `FLASH` como clasificación de catálogo con `Playable = false` y motivo `NO_SUPPORTED_RUNTIME` (§42).

Secciones cubiertas: §7, §9, §40, §41, §42, §43, §63, §84, §92.

Fuera de esta fase:

- No hay `ExternalRuntimeLaunchAdapter` funcional; el runtime externo queda para la fase avanzada (§5, §80 Milestone 7).
- No hay medición de sesión ni playtime todavía (§6).
- No se asume soporte de ejecución nativa para juegos Windows sin runtime compatible (§89).
- No se intenta sortear CAPTCHA, login, bloqueos ni mecanismos anti-bot (§18, §89).

Criterios de salida:

- [ ] Se lanza un APK instalado compatible y se obtiene `Success` (§81).
- [ ] Cada fallo de launch produce un motivo explicable en la UI, con test de UI (§60, §63).
- [ ] Cada adapter cumple la Definition of Done de §84, incluida la validación de instalación y de runtime.
- [ ] Ningún adapter requiere ampliar permisos más allá de lo justificado (§43).

---

## Phase 4 — PlaySession, playtime y estadísticas

**Objetivo:** historial de juego persistente y acumulativo, independiente de la instalación.

Trabajo:

- Implementar `PlaySession` con `gameId`, `installationId` nullable, `startedAt`, `endedAt` nullable, `durationMs`, `terminationReason`, `source` (§6).
- Mantener el historial ligado al `Game`, nunca a la instalación (§6, §54).
- Modelar `terminationReason` (`USER_STOPPED`, `APP_BACKGROUND`, `GAME_PROCESS_ENDED`, `ACTIVITY_CLOSED`, `CRASH`, `UNKNOWN`) y `source` (`LAUNCHED_BY_APP`, `IMPORTED`, `MANUAL`) (§6).
- Implementar el cronómetro de sesión con tolerancia de background configurable y cierre automático de sesión configurable (§66).
- Preferir sesiones iniciadas desde la app y registrar tiempo observado por la aplicación, sin asumir acceso a datos internos del APK externo (§6, §9).
- Implementar las métricas derivadas de §6: total, semanal, mensual, número de sesiones, media por sesión, última y primera sesión, día con más tiempo, juego más jugado, top por tiempo.
- Reflejar `lastPlayedAt` y `firstPlayedAt` en `Game` como caché derivada (§23).
- Recalcular `totalPlaytimeMs` desde `PlaySession` y mantener la caché coherente (§23).
- Implementar la pantalla Statistics con totales, top de juegos, sesiones, actividad semanal/mensual y estado de biblioteca (§35).
- Añadir a Library los criterios de orden `Last played`, `Playtime` y el filtro de estado (§35).

Secciones cubiertas: §6, §9, §23, §35, §54, §60, §66, §81.

Fuera de esta fase:

- No se promete exactitud absoluta del tiempo de juego para APKs externos (§6).
- No hay estadísticas avanzadas ni comparativas entre fuentes; eso es fase avanzada (§80 Milestone 7).
- No hay correlación con eventos de tienda de juegos ni recompensas (§89).

Criterios de salida:

- [ ] Se registra una `PlaySession` desde un launch de la app (§81).
- [ ] El playtime se acumula y sobrevive a borrar y reinstalar la instalación (§6, §81).
- [ ] Existe un test unitario de agregación de playtime con sesiones de duración variable y `installationId` nulo (§60).
- [ ] La UI expone las métricas derivadas sin recalcular en el hilo principal (§72).

---

## Phase 5 — Metadata: contratos, normalización y primer provider

**Objetivo:** un único provider completo de punta a punta, cumpliendo la Definition of Done de §82.

Trabajo:

- Definir la interfaz `MetadataProvider` con `id`, `displayName`, `search(query)` y `fetch(reference)` (§16).
- Definir `MetadataResult` normalizado con todos los campos de §16; ningún provider está obligado a rellenarlos todos.
- Definir `MetadataCandidate`, `MetadataQuery` y `ProviderReference` como tipos del dominio de búsqueda (§16, §29).
- Implementar la estructura modular por proveedor `providers/<id>/`; nunca un scraper genérico que asuma HTML idéntico entre sitios (§18).
- Implementar `Provider` y `ProviderConfiguration` con `enabled`, `priority`, capacidades y marcas de salud (§25).
- Implementar `ProviderHealth` (`AVAILABLE`, `RATE_LIMITED`, `AUTH_REQUIRED`, `TEMPORARY_ERROR`, `BLOCKED_BY_PROVIDER`, `UNSUPPORTED_QUERY`, `PARSE_ERROR`, `DISABLED`) (§18).
- Implementar `ProviderRateLimiter` con `minDelay`, `requestsPerWindow`, `burst` y `cooldown`, configurables por proveedor (§74, §46).
- Implementar el pipeline de búsqueda de §29: normalizar → detectar IDs especiales (RJ, Steam App ID, VNDB ID, `packageName`, URL) → lookup local → providers externos.
- Implementar `ProviderResolver` para búsqueda por URL, prefiriendo el ID de la URL frente al título (§29).
- Implementar la capa de caché de metadata con `queryHash`, `providerId`, `response`, `fetchedAt`, `expiresAt` y TTL configurables, no constantes rígidas (§27).
- Implementar `parserVersion` por provider y la invalidación de caché asociada (§68, §69).
- Implementar el primer provider siguiendo el orden de §80: VNDB, usando su API oficial y respetando los límites documentados (§17, §18, §80).
- Aplicar la política de §18 completa: API oficial cuando exista, respeto a términos y robots, rate limiting, retries con backoff, caché, sin bombardear con búsquedas repetidas, sin evadir bloqueos.
- Registrar fallos por provider sin romper la búsqueda completa (§18).
- Mostrar resultados progresivamente: la UI se actualiza según llega cada provider, sin esperar al conjunto (§73).
- Implementar el fallback de red: provider fallido → caché disponible → continuar con el resto → devolver metadata parcial (§75).

Secciones cubiertas: §15, §16, §17, §18, §19 (contrato), §25, §27, §28, §29, §46, §68, §69, §72, §73, §74, §75, §80 Milestone 5, §82.

Fuera de esta fase:

- Solo un provider activo. Los demás están planificados pero no implementados (§17, §80).
- No hay agregación multi-provider ni scoring; la búsqueda devuelve candidatos de un único origen (§19).
- No hay provenance persistida ni resolución de conflictos entre fuentes (§20).
- No hay `Developer` como entidad con aliases; no es requisito de MVP (§53).

Criterios de salida:

- [ ] El provider cumple la Definition of Done completa de §82, sin omitir ningún punto.
- [ ] Se busca metadata en al menos un provider y se importan sus external IDs (§81).
- [ ] El provider respeta su rate limit configurado bajo carga y el cacheo evita consultas repetidas (§18, §27, §74).
- [ ] Un provider caído no impide consultar el resultado cacheado (§54.10, §75).
- [ ] Al iniciar un parser nuevo, la caché anterior queda marcada como potencialmente obsoleta (§68, §69).
- [ ] La pantalla de búsqueda cancela las peticiones pendientes al abandonarse (§47).

---

## Phase 6 — Agregador, provenance y resolución de conflictos

**Objetivo:** combinar candidatos de varios providers con trazabilidad por campo y sin sobrescribir datos personales.

Trabajo:

- Implementar el pipeline completo de §19: queries paralelas → candidatos → normalización → resolución de IDs → scoring → deduplicación → resultado mergeado → confirmación si es ambiguo.
- Implementar el scoring inicial de §19: ID externo exacto +100, RJ exacto +100, `packageName` exacto +100, título normalizado exacto +70, título alternativo +60, developer +25, engine +10, plataforma +10, fuzzy +0..50, con los valores marcados como iniciales y sujetos a calibración (§19, §94).
- Implementar la regla de seguridad: no fusionar automáticamente con score bajo (§19).
- Implementar `MetadataFieldValue` con `gameId`, `fieldName`, `value`, `providerId`, `providerRecordId`, `sourceUrl`, `fetchedAt`, `confidence`, `isSelected` (§20).
- Conservar valores alternativos para comparación y revisión, con `isSelected` como selección final (§20).
- Registrar provenance también para el detector interno de engine, no solo para providers externos (§20).
- Implementar la acción del importador: crear/actualizar `Game`, guardar external IDs, guardar provenance, guardar assets, guardar localizations, dejar intactos los campos personales (§30).
- Implementar la actualización de metadata: comparar antiguo/nuevo, aplicar solo campos externos, preservar datos personales, registrar provenance (§30).
- Implementar la resolución de conflicto con UI de selección por campo, tal como en el ejemplo de `developer` de §30 y §76.
- Implementar el guardado de Localization con `language`, `kind` (`ORIGINAL`, `OFFICIAL_TRANSLATION`, `FAN_TRANSLATION`, `MACHINE_TRANSLATION`, `UNKNOWN`), `title`, `version`, `translator`, `sourceProvider`, `sourceUrl`, `notes`; una traducción no crea otro `Game` (§22, §54.9).
- Implementar `ProviderPriorityRule` por campo (`field`, `providerId`, `priority`) en lugar de una lista universal (§58, §57).
- Implementar `downloadLinks[]` con `url`, `sourceProvider`, `label`, `platform`, `version`, `language`, `isPrimary`, en lugar de un único `downloadUrl` (§50).
- Implementar `ExternalLink` con `providerId`, `url`, `label`, `type` (`SOURCE`, `DOWNLOAD`, `STORE`, `TRANSLATION`, `DOCUMENTATION`, `HOMEPAGE`, `OTHER`) (§49).
- Implementar `versionRaw`, `versionNormalized`, `versionSource` sin destruir el valor original (§52).
- Implementar el autoservicio de metadata tras el scan de §34, con confirmación del usuario cuando sea ambiguo y sin auto-merge destructivo.
- Implementar la UX de confirmación de §56: lista de candidatos con sus IDs, opción de usar uno o asociación manual.
- Añadir al Game Detail los bloques `Metadata sources`, `Localizations` y `External links` (§35).

Secciones cubiertas: §2.4, §2.5, §12, §19, §20, §22, §29, §30, §34, §35, §49, §50, §52, §54, §56, §57, §58, §76, §77, §85.

Fuera de esta fase:

- No se implementan providers adicionales más allá del primero (§17).
- No se normaliza `Developer` como entidad con aliases; sigue siendo texto con provenance (§53).
- No hay edición por lotes de metadata entre múltiples juegos (§35).

Criterios de salida:

- [ ] Actualizar metadata nunca borra ni sobrescribe campos personales (§77, §81, §92).
- [ ] Un conflicto entre providers se resuelve por selección explícita del usuario, nunca por overwrite silencioso (§30, §76).
- [ ] Existe un test de integración de merge de metadata y de merge de localizations (§60).
- [ ] La UI responde «¿de dónde salió este valor?» para título, engine, tag, RJ y versión (§2.5).
- [ ] Un candidato ambiguo nunca se auto-mergea y siempre ofrece override manual (§12, §19, §56).
- [ ] El HTML parsing de providers no aparece en `GameRepository` (§85).

---

## Phase 7 — Cobertura de providers y modos de actualización

**Objetivo:** ampliar la cobertura de fuentes y activar la actualización automática de forma controlada.

Trabajo:

- Implementar DLsite, Steam e itch.io, en el orden de §80 Milestone 5, aplicando la Definition of Done de §82 a cada uno.
- Implementar itch.io teniendo en cuenta que su API oficial está orientada a integraciones autenticadas y no es una API pública de búsqueda; evaluar endpoints públicos y RSS antes de asumir acceso (§17).
- Implementar F95 como provider independiente, con respeto a términos, controles técnicos, rate limits y autenticación si alguna función la requiere; sin evasión de bloqueos (§17, §18).
- Implementar HaitenJP y Kimochi, con foco en descubrimiento por RJ y referencias Android cuando estén presentes (§14, §17).
- Implementar los providers de traducción (Erodeus, ZonaHentaiJuegos, Code Arc Traducciones) como fuente de Localization, sin reemplazar automáticamente el título original ni la metadata principal (§17).
- Implementar los providers de fallback y descubrimiento secundario (HentaiBedta, FapForFun, Otomi, ThomasTaihei, OtakuPlan) como fuentes de aliases, enlaces y búsqueda secundaria (§17, §57).
- Implementar los modos de actualización de metadata `MANUAL`, `ON_IMPORT`, `ON_OPEN_IF_STALE`, `SCHEDULED`, `OFF` (§67).
- Dejar los defaults de MVP en `ON_IMPORT = enabled` y `SCHEDULED = disabled` (§67).
- Implementar las tareas de WorkManager para metadata refresh y asset refresh, con `ExistingWorkPolicy` coherente y sin loops permanentes (§45).
- Implementar la caché de assets en disco con deduplicación por SHA-256, URL normalizada o `provider + asset ID` (§48).
- Mantener la metadata visible aunque el asset remoto ya no esté disponible (§48).
- Implementar las opciones de Settings de metadata: providers habilitados, prioridad, auto update, intervalo, preguntar antes de reemplazar, TTL de caché (§66).
- Implementar la pantalla de observabilidad local con el estado de providers, del scanner y de la base de datos (§71).

Secciones cubiertas: §14, §17, §18, §27, §45, §48, §57, §58, §66, §67, §68, §71, §74, §80 Milestone 5, §82, §87, §88.

Fuera de esta fase:

- No hay backend ni scrapers centralizados; cada provider corre en el dispositivo (§64, §65, §86).
- No se amplían los providers más allá de la lista de §17 y §87 sin añadir la fuente a la especificación.
- No se implementa todavía el backup/export ni las colecciones (§37, §38).
- No se ejecutan tareas en background sin que el usuario las haya habilitado explícitamente (§44).

Criterios de salida:

- [ ] Cada provider nuevo cumple la Definition of Done de §82, con sus nueve escenarios de test de §59.
- [ ] Los providers de traducción alimentan `Localization` y no pisan el `canonicalTitle` (§17, §22).
- [ ] `SCHEDULED` permanece desactivado por defecto y solo se activa por decisión explícita del usuario (§67).
- [ ] Ninguna búsqueda externa se dispara sin acción del usuario o tarea habilitada por él (§44).
- [ ] La pantalla de observabilidad permite diagnosticar un provider roto sin adivinar (§71).

---

## Phase 8 — Avanzado: backup, colecciones y runtimes externos

**Objetivo:** portabilidad, organización y cobertura de ejecución que exceden el MVP.

Trabajo:

- Implementar export/import en el formato portable de §38 (`manifest.json`, `games.json`, `installations.json`, `sessions.json`, `tags.json`, `external_ids.json`, `metadata.json`, `assets/`).
- Permitir restaurar catálogo, metadata, datos personales, playtime y referencias externas, dejando las instalaciones físicas como `missing` al restaurar en otro dispositivo (§38).
- Definir la política de versionado del formato de backup para futuras incompatibilidades.
- Ofrecer export antes de migraciones críticas de schema (§79).
- Implementar `Collection` y `CollectionItem` como entidades propias, sin reutilizar tags para colecciones manuales obligatorias (§37).
- Implementar estadísticas avanzadas: tendencias, comparativas por engine y por estado (§35, §80 Milestone 7).
- Implementar `ExternalRuntimeLaunchAdapter` para runtimes externos que el usuario ya tenga instalados (§5, §40).
- Evaluar `installationType = ARCHIVE` con lectura de contenido comprimido sin extraerlo necesariamente.
- Implementar normalización de `Developer` como entidad con `canonicalName`, `aliases[]` y `externalIds[]`, si aparece la necesidad real (§53).

Secciones cubiertas: §5, §35, §37, §38, §40, §48, §53, §62, §65, §79, §80 Milestone 7.

Fuera de esta fase:

- No hay backend, ni cuentas, ni sincronización, ni servicio adicional instalado en el teléfono (§64, §65, §89).
- No se soporta de forma nativa la ejecución de juegos Windows sin runtime compatible (§89).
- No se promete soporte universal de todos los engines (§89).

Criterios de salida:

- [ ] Un backup exportado en un dispositivo restaura catálogo, metadata, datos personales y playtime (§38).
- [ ] Las instalaciones físicas ausentes se restauran como `missing`, sin romper la integridad del catálogo (§38, §62).
- [ ] `Collection` no se representa obligatoriamente con tags (§37).
- [ ] `ExternalRuntimeLaunchAdapter` cumple la Definition of Done de §84 y separa runtime de engine (§40).

---

## Dependencia de trabajo

Orden de bloqueo explícito:

- `Game` bloquea a todo lo demás. Sin entidad `Game` no hay catálogo, ni instalaciones, ni historial (§2.2, §4.1).
- `Game` antes que `Installation`. La instalación apunta a un juego; nunca al revés (§5, §54.2).
- `Game` antes que `PlaySession`. El historial pertenece al juego, no a la instalación (§6).
- `ExternalId` antes que el `Aggregator`. La resolución de IDs y el scoring de §19 dependen de la tabla de IDs (§15).
- `TitleNormalizer` antes del `Aggregator` y antes del scanner que extrae títulos (§13, §19).
- RJ canónico antes del matching cross-source: DLsite, HaitenJP y Kimochi se consultan por RJ (§14).
- `Installation` antes de `LaunchAdapter`. Un adapter valida y lanza instalaciones (§9, §84).
- Scanner antes del cálculo de disponibilidad: `Playable` se deriva de instalaciones reales (§2.3, §36).
- Detector de engine antes de la clasificación de engine, pero el detector no habilita el lanzamiento (§7, §8, §40).
- Contrato `MetadataProvider` antes que cualquier provider; el normalizador y el error mapping son parte del contrato, no del provider (§16, §82).
- Primer provider antes del `Aggregator`. El agregador necesita al menos dos fuentes reales para tener sentido (§19).
- `ProviderRateLimiter` antes que las llamadas reales de red, no después (§74).
- Caché y `parserVersion` antes de activar actualizaciones periódicas; sin eso, un parser corregido deja datos obsoletos sin invalidar (§27, §68, §69).
- `MetadataFieldValue` (provenance) antes de la actualización automática de metadata; sin provenance no hay resolución de conflictos (§20, §30).
- Modos de actualización antes de `WorkManager` programado (§67, §45).
- Backup antes de la primera migración de schema que altere datos personales (§79).
- Collections después de `Tag`, para no confundirlas ni reutilizar tags como colecciones (§21, §37).
- `ExternalRuntimeLaunchAdapter` después de los adapters nativos, para no acoplar el launcher a un único runtime (§9, §92).

Paralelizable sin bloqueo:

- `PlaySession` (Phase 4) y el contrato de providers (Phase 5) son independientes una vez estable `Game`.
- Pantalla Statistics y pantallas de metadata pueden avanzar en paralelo.
- Provider tests de §59 pueden escribirse junto con el normalizador.

---

## Línea de corte del MVP

MVP = Phase 0 a Phase 6, con un único provider completo (VNDB) y el pipeline de agregación preparado para recibir más.

Incluido en el MVP:

- Phase 0 completa: proyecto, capas, tests, logging, migraciones (§3, §60, §70, §78).
- Phase 1 completa: `Game`, tags, estado personal, Library, Game Detail, editor manual (§4, §21, §23, §35).
- Phase 2 completa: `Installation`, SAF, scanner, APK detection, disponibilidad Catalog/Playable (§5, §10, §11, §36).
- Phase 3 completa: launch de APK instalado y de contenido web, con manejo de fallos (§9, §63, §84).
- Phase 4 completa: `PlaySession`, playtime acumulado, Statistics básica (§6, §35).
- Phase 5 completa con un solo provider: interfaz `MetadataProvider`, normalizador, rate limiting, caché, `parserVersion` (§16, §27, §69, §82).
- Phase 6 completa: scoring, deduplicación, provenance, resolución de conflictos, Localization (§19, §20, §22, §30, §58).

Diferido explícitamente, fuera del MVP:

- Phase 7 completa: el resto de providers de §17 y §87, la actualización `SCHEDULED`, la caché de assets y la pantalla de observabilidad. La caché de assets y la observabilidad pueden adelantarse parcialmente si son baratas.
- Phase 8 completa: backup/export, `Collection`, estadísticas avanzadas, `ExternalRuntimeLaunchAdapter`, entidad `Developer` (§37, §38, §53, §80).
- Cualquier backend, scraper centralizado o sincronización (§64, §86).
- Cualquier servicio adicional instalado en el teléfono (§65).

Condiciones de MVP según §81:

- [ ] Crear un `Game` manualmente.
- [ ] Mostrarlo en Catalog con metadata y tags.
- [ ] Registrar personal status y editar rating y notas personales.
- [ ] Detectar una instalación y asociarla a un `Game`.
- [ ] Distinguir Catalog de Playable.
- [ ] Lanzar un APK instalado compatible.
- [ ] Registrar una `PlaySession` y acumular playtime.
- [ ] Borrar una `Installation` sin borrar el `Game`.
- [ ] Conservar el historial tras reinstalación.
- [ ] Buscar metadata en al menos un provider e importar external IDs.
- [ ] Actualizar metadata sin borrar campos personales.
- [ ] Funcionar sin Internet usando la metadata local.

---

## Trabajo transversal

- **Offline-first.** La biblioteca local es la fuente de verdad diaria; la UI nunca espera a un provider externo si ya hay metadata local (§2.1, §72). Cada pantalla debe renderizar desde Room antes de cualquier llamada externa.
- **Invariante de campos personales.** `personalStatus`, `personalRating`, `personalNotes`, tags personales, playtime e historial nunca se sobrescriben por una actualización externa (§2.4, §54.6, §77). Debe existir un test de regresión que falle si esa regla se rompe.
- **Separación catálogo/instalación.** Borrar una instalación o perder la ruta física nunca elimina el juego, su metadata ni su historial (§2.2, §32, §54).
- **Migraciones de base de datos.** Cada cambio persistente incrementa la versión de schema, con migración explícita y migration test; nunca `fallbackToDestructiveMigration()` en producción (§78, §60).
- **Seguridad del backup.** El backup debe poder ejecutarse antes de migraciones importantes, y el export manual puede ofrecerse en Settings (§79).
- **Versionado de parser.** Cada provider expone `parserVersion`; al incrementarlo, la caché afectada se marca como potencialmente obsoleta (§68, §69).
- **Logging y observabilidad.** Categorías fijas, verbosidad diferenciada debug/release, sin datos sensibles, más una pantalla local de diagnóstico de providers, scanner y base de datos (§70, §71).
- **Estrategia de tests.** Unitarios para normalización, RJ, scoring, detección de engine, disponibilidad, agregación de playtime, merge de metadata y de localizations; de integración para DAO, repository, scanner, parsing de providers y adapters; de UI para filtros, detail, selección de metadata, confirmación de scan y fallo de launch; migration tests para cada cambio de schema (§60).
- **Seguridad.** Sin secretos innecesarios en código ni en almacenamiento, validación de URLs, sanitización de HTML antes de mostrarlo, contenido descargado tratado como no confiable, WebView solo en contexto controlado (§43).
- **Privacidad.** Notas, rating, historial, rutas y estructura de biblioteca solo locales; el catálogo completo nunca se envía a un servicio externo; toda consulta remota nace de una acción del usuario o de una tarea habilitada por él (§44).
- **Minimización de permisos.** Sin permisos especulativos; `MANAGE_EXTERNAL_STORAGE` no es el mecanismo previsto, el acceso es vía SAF y permisos persistibles (§10, §43, §92).
- **Sin servicio adicional.** Todo lo necesario para la gestión local vive en la app principal; no se requiere ni se instala una segunda APK o servicio (§65).
- **Sin backend.** No hay servidor propio obligatorio; los providers se diseñan desde el inicio para poder migrarse a un backend sin cambiar el modelo conceptual (§64, §85).
- **Sin acoplamiento.** El launcher no depende de un único engine ni de un único provider; la metadata nunca condiciona la arquitectura del launcher (§7, §40, §92).
- **Cumplimiento por proveedor.** Implementación propia, APIs oficiales, scraping de terceros, metadata recibida y assets descargados permanecen separados y revisados; el uso personal no autoriza automáticamente ningún método de extracción (§18, §88).

---

## Registro de riesgos

- **Fragilidad del scraping y cumplimiento de los términos de los proveedores.** Mitigación: un módulo por proveedor, API oficial cuando exista, respeto a robots y rate limits, retries con backoff, caché, sin evadir CAPTCHA ni bloqueos, y un switch de desactivación por provider. Si un provider deja de ser utilizable, el sistema sigue operando con los demás y con la caché. Referencias: §18, §59, §82, §88.
- **Restricción de fuentes de metadata por la ausencia de backend.** El app scrape desde el dispositivo, lo que expone la IP del usuario y limita volumen, consistencia y actualización de parsers sin release de la app. Mitigación: mantener el modelo de dominio y los providers desacoplados, de modo que un futuro `Metadata Server` solo cambie dónde se ejecuta el scraping. Referencias: §64, §85, §86.
- **Ambigüedad entre engine detectado y runtime disponible.** Confundir clasificación con capacidad de ejecución produce falsos positivos de disponibilidad. Mitigación: `Playable` se deriva de plataforma + runtime + adapter, nunca del engine; `Detected engine != playable` es invariante con test. Referencias: §7, §40, §54.7.
- **Limitaciones de SAF y almacenamiento con ámbito.** Sin acceso indiscriminado al almacenamiento, el scanner depende de carpetas que el usuario conceda y los permisos persistibles se pueden perder. Mitigación: UI de estado de permisos persistentes, re-solicitud explícita, y reglas que prohíben borrar metadata ante un archivo no encontrado. Referencias: §10, §32, §92.
- **Ambigüedad en el matching multi-instalación.** Varias instalaciones del mismo juego pueden fusionarse o separarse incorrectamente, especialmente sin IDs compartidos. Mitigación: orden de matching explícito, umbral de confianza, confirmación del usuario con override manual y prohibición de auto-merge con score bajo. Referencias: §12, §19, §55, §56.
- **Rendimiento objetivo sobre bibliotecas grandes.** Miles de juegos e instalaciones pueden degradar la biblioteca y el scanner. Mitigación: índices sobre columnas de filtro y orden, consultas reactivas con `Flow`, render progresivo del scanner, búsqueda local por debajo del objetivo de 100 ms, y mediciones sobre el volumen real de la biblioteca. Referencias: §72, §33, §36.
- **Degradación de parsers por cambios de HTML.** Un cambio en un sitio rompe el parsing silenciosamente. Mitigación: `parserVersion` por provider, invalidación de caché, health reporting por provider, y test de fixture de página modificada. Referencias: §68, §69, §59.
- **Conflicto de propiedad de los campos entre fuentes.** Dos fuentes legítimas con valores distintos pueden provocar overwrite silencioso o pérdida de información. Mitigación: provenance por campo, valor seleccionado explícito, valores alternativos conservados y prioridad configurable por campo. Referencias: §20, §30, §57, §58, §76.
- **Pérdida del catálogo por migración de schema.** Un cambio de schema destructivo borraría datos personales. Mitigación: migraciones explícitas con test, sin migración destructiva en producción, y export de backup antes de migraciones críticas. Referencias: §78, §79.
- **Pérdida de permisos de SAF.** Un URI concedido puede dejar de ser accesible tras un restore o un cambio de dispositivo. Mitigación: modelar las instalaciones como `missing` en lugar de borrarlas, y pedir re-otorgamiento desde la UI de scanner. Referencias: §10, §62.
- **Falsos positivos en la detección de engine.** Firmas ambiguas entre RPG Maker MV y MZ, o entre Unity y Godot, clasifican mal. Mitigación: score de confianza con lista de evidencias y motivo legible, tests de falsos positivos y falsos negativos, y fallback a `UNKNOWN`. Referencias: §8, §83.
- **Inexactitud del playtime en APKs externos.** El sistema solo puede observar lo que Android permite. Mitigación: documentar que se registra tiempo observado por la app, preferir sesiones iniciadas desde la app y no afirmar exactitud. Referencias: §6.
- **Imposibilidad de sortear restricciones de plataforma.** Flash y juegos Windows sin runtime compatible no serán ejecutables en el dispositivo. Mitigación: representarlos como catálogo con `Playable = false` y motivo explícito, en lugar de prometer soporte. Referencias: §40, §42, §89.

---

## Preguntas abiertas y decisiones pendientes

`SPECS.md` §94 declara explícitamente qué queda abierto. Cada punto se convierte en una pregunta a responder durante la implementación, con el lugar donde debe decidirse:

- **Nombres exactos de clases.** ¿Cuál es el nombre final de la entidad de disponibilidad, del resultado de búsqueda y de la configuración de un detector? Decidir en Phase 0/1, al definir el schema Room, porque renombrar después obliga a migración.
- **Nombres exactos de tablas y columnas.** ¿`Game.personalStatus` se materializa como columna o como tabla de estado? ¿`totalPlaytimeMs` se cachea en `Game` o en una tabla de métricas? Decidir en Phase 1, con migration test desde el schema inicial (§78, §23).
- **Versiones concretas de dependencias.** ¿Qué versiones de Kotlin, Compose, Room, WorkManager y del cliente HTTP se fijan, y cuál es la política de actualización? Decidir en Phase 0 y dejar el resultado registrado.
- **Firmas definitivas de los detectores.** ¿Qué score tiene cada evidencia de §8.1 y cuál es el umbral entre `UNKNOWN` y una clasificación? ¿Qué reglas resuelven la ambigüedad MV frente a MZ? Decidir en Phase 2, con test de falsos positivos y falsos negativos (§83).
- **Calibración del scoring de matching.** Los valores de §19 son conceptuales. ¿Cuál es el umbral real de confianza para pedir confirmación al usuario y cuál para fusionar? Decidir en Phase 6, midiendo sobre la biblioteca real, no sobre ejemplos inventados (§12, §19, §56).
- **Límites concretos por provider.** ¿Qué `requestsPerWindow`, `burst` y `cooldown` se aplican a cada fuente, y de dónde sale cada dato? Decidir en Phase 5 por provider, con la fuente del límite documentada junto al adapter (§74).
- **APIs concretas de terceros.** ¿Qué endpoint o documento público se usa por provider, y qué parte queda sin implementar por falta de acceso legítimo? ¿Cómo se maneja la autenticación cuando una función la requiere, sin almacenar secretos innecesarios? Decidir en Phase 5 y Phase 7, provider por provider (§17, §18, §43).
- **Compatibilidad real de runtimes.** ¿Qué `ExternalRuntimeLaunchAdapter` son realmente viables en el dispositivo objetivo, y cómo se detecta su presencia? Decidir en Phase 8, sin comprometer el launcher (§5, §40, §84).
- **Alcance real del WebView para juegos web.** ¿Qué juegos HTML5 funcionan con recursos locales y cuáles requieren backend? ¿Cuándo es técnicamente seguro usar WebView y cuándo solo abrir el navegador externo? Decidir en Phase 3, con criterio explícito y documentado (§41).
- **Granularity de `isPlayable`.** ¿Se persiste un snapshot para rendimiento o se deriva siempre por consulta? Si se persiste, ¿cuál es el procedimiento de recálculo y quién lo dispara? Decidir en Phase 2 (§62).
- **Retención de assets y de caché de red.** ¿Cuánto se conserva una imagen en caché y qué política de borrado aplica? ¿El TTL de metadata es 7 días, 30 o configurable por provider desde el primer momento? Decidir en Phase 5, con los valores fuera de constantes rígidas (§27, §48).
- **Confirmación de borrado de `Game`.** ¿La opción recomendada de §32 se implementa siempre o solo cuando existen instalaciones vinculadas? Decidir en Phase 1/2, con test de UI de confirmación (§32, §60).
- **Orden de backup respecto a migraciones.** ¿El export previo a migración es una pantalla opcional, un recordatorio automático o un diálogo bloqueante? Decidir antes de la primera migración de schema (§79).
- **Proveedor de asset por defecto.** ¿De dónde sale la portada cuando el provider no la aporta, y el usuario puede fijar una imagen local? Decidir en Phase 1 y Phase 6 (§24, §48).
- **Significado de `hidden`.** ¿Ocultar un juego lo excluye de Statistics o solo de la vista de biblioteca? Decidir en Phase 1, porque afecta a las consultas derivadas (§23).
- **Taxonomía de `ProviderConfiguration`.** ¿Qué configuración por provider se expone en Settings y qué se queda interna? Decidir en Phase 5/7, sabiendo que no deben almacenarse secretos innecesarios (§25, §43).

---

## Referencias

- Especificación técnica completa: `SPECS.md`.
- Definiciones de Done: `SPECS.md` §82 (provider), §83 (engine detector), §84 (launch adapter).
- Criterios de aceptación del MVP: `SPECS.md` §81.
- Orden de implementación de providers: `SPECS.md` §80 Milestone 5.
- No-objetivos del MVP: `SPECS.md` §89.