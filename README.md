# Game Library / Launcher

Aplicación Android personal para gestionar una biblioteca de juegos heterogénea, con independencia de si cada juego está instalado en el dispositivo o solamente registrado en el catálogo. Funciona como catálogo personal, gestor de instalaciones, launcher cuando existe un runtime compatible, rastreador de sesiones y tiempo de juego, y agregador de metadata procedente de múltiples fuentes externas, con trazabilidad por campo y funcionamiento offline-first.

---

## Estado

- **Fase:** borrador técnico v0.1 de la especificación.
- **Implementación:** no iniciada. Este repositorio contiene únicamente documentación.
- **Base de conocimiento:** `SPECS.md`, 94 secciones, es el documento normativo del proyecto.
- **Modelo de uso:** un solo usuario, en un solo dispositivo.
- **Backend:** ninguno. La app habla directamente con APIs y sitios públicos desde el dispositivo.
- **Licencia:** por definir. Ver §88 para las obligaciones de licencia por componente de terceros.

---

## Pilares de funcionalidad

- **Catálogo completo, no solo lo instalado.** La biblioteca incluye todo juego conocido, esté o no ejecutable en el dispositivo. La vista Playable es derivada, no una base de datos paralela (§2.2, §2.3, §36).
- **Instalaciones como entidad separada.** Un juego puede tener varias instalaciones: APK de Android, carpeta local, juego web, archivo comprimido. Borrar una instalación nunca borra el juego, su metadata ni su historial (§2.2, §5, §32).
- **Scanner de instalaciones.** El usuario concede carpetas mediante Storage Access Framework; la app descubre APKs, estructuras de juegos, directorios y juegos web, y calcula un engine probable con nivel de confianza (§10, §11, §8).
- **Launcher desacoplado del engine.** Un contrato `LaunchAdapter` permite lanzar APKs instalados, contenido web en navegador, juegos web locales y runtimes externos, sin que el launcher dependa de un único engine (§9, §40).
- **Playtime e historial.** Sesiones de juego registradas al lanzar desde la app, con playtime acumulado que sobrevive a borrar y reinstalar el juego (§6).
- **Metadata multi-provider con provenance.** Cada valor externo relevante guarda su fuente, su confianza y si está seleccionado. El sistema responde «¿de dónde salió este título, engine, tag o RJ?» (§2.5, §16, §20).
- **Datos personales protegidos.** Estado personal, rating, notas, tags personales, playtime e historial nunca se sobrescriben por una actualización externa (§2.4, §77).
- **Identificadores externos de primer nivel.** IDs como VNDB, RJ de DLsite, Steam App ID, itch.io o F95 son entidades propias, no notas dentro de un campo de texto (§14, §15).
- **Tags, filtros y colecciones.** Filtrado y orden por engine, plataforma, disponibilidad, estado personal, tags, idioma, disponibilidad de traducción, favorito; y orden por título, última partida, playtime, fecha de alta, rating y developer (§35).
- **Traducciones como información, no como juegos duplicados.** Una traducción se registra como `Localization` con idioma, tipo, versión y traductor, sin crear un `Game` nuevo (§22).
- **Import/export portátil.** Formato JSON con assets opcionales que permite respaldar y restaurar catálogo, metadata, datos personales y playtime en otro dispositivo (§38).
- **Offline-first real.** Abrir la biblioteca, filtrar, consultar metadata almacenada, ver el historial y lanzar instalaciones compatibles funciona sin Internet. La red solo se usa para metadata, imágenes y páginas externas (§2.1).
- **Resultados progresivos.** La pantalla de búsqueda muestra primero lo local y va incorporando cada proveedor a medida que responde, sin bloquear la interfaz (§72, §73).

---

## Pantallas

- **Home.** Resumen de actividad reciente: juegos continuados, tiempo reciente, juegos instalados, juegos pendientes y traducciones nuevas detectadas (§35).
- **Library.** Lista del catálogo con filtros combinables y varios criterios de orden, con indicación visible de qué juegos son jugables en el dispositivo (§35, §36).
- **Game detail.** Ficha completa: portada, título, versión, developer, estado, descripción, engine, tags, rating, notas personales, playtime, sesiones, instalaciones, plataformas, localizaciones, enlaces externos y fuentes de metadata (§35).
- **Scanner.** Selección de carpetas monitorizadas y resultado del escaneo: carpeta de origen, archivos encontrados, engine detectado, asociaciones propuestas, juegos nuevos y juegos ya conocidos (§35, §33).
- **Metadata search.** Búsqueda unificada: primero la base local, luego los proveedores habilitados. Acepta texto, RJ, IDs de Steam o VNDB, `packageName` y URL de proveedor (§29).
- **Metadata editor.** Edición manual de cualquier campo y resolución de conflictos entre fuentes, eligiendo qué valor se aplica y viendo de qué proveedor proviene cada alternativa (§30, §31, §35).
- **Statistics.** Playtime total, juegos más jugados, número de sesiones, actividad semanal y mensual, y estado general de la biblioteca (§35).
- **Settings.** Carpetas de escaneo, frecuencia de escaneo, providers habilitados y su prioridad, modos e intervalos de actualización, TTL de caché, comportamiento de búsqueda externa, seguimiento de playtime, tema y tamaño de portada, y borrado de caché (§66).
- **Diagnostics (opcional).** Estado de salud por provider, resumen del último escaneo y recuentos de la base de datos, para reparar providers sin adivinar (§71).

---

## Arquitectura de un vistazo

```text
                    INTERNET
          ┌────────────┼─────────────┐
          │            │             │
        VNDB        DLsite      Steam / itch
          │            │             │
        F95         Kimochi    translation sites
          │            │             │
          └────────────┼─────────────┘
                       │
              Metadata Providers        ← un módulo por fuente
                       │
              Normalizer / Merger        ← scoring, dedup, provenance
                       │
              Metadata Repository
                       │
     ┌─────────────────┴──────────────────┐
     │                                    │
  Room DB                          Local storage / SAF
     │                                    │
     ├── Games                            ├── Game files
     ├── Installations                    ├── APKs
     ├── PlaySessions                     ├── Web games
     ├── ExternalIds                      └── Assets
     ├── Tags
     ├── Localizations
     ├── Assets
     └── Provenance
     │
     ▼
 ViewModels / Use Cases
     │
     ▼
 Jetpack Compose UI
     │
     ├── Library      ├── Scanner    ├── Statistics
     ├── Game detail  ├── Metadata  └── Settings

Launch chain:

  Game  →  Installation  →  LaunchAdapter  →  Android package
                                                  / browser
                                                  / web runtime
                                                  / external runtime
```

Dos capas, dos reglas de acoplamiento: la metadata nunca decide la arquitectura del launcher (§40), y el launcher nunca decide qué es un juego (§9).

---

## Núcleo del modelo de dominio

Cuatro entidades sostienen la arquitectura y dos la articulan. El resto cuelga de ellas.

- **`Game`** — la entidad lógica del juego: título canónico, versión, developer, engine, estado personal, rating personal, notas, portada y timestamps. Es la raíz de todo. Puede existir sin ninguna instalación y seguir siendo un elemento válido del catálogo (§4.1, §2.2).
  - Por qué importa: si `Game` fuera sinónimo de «instalación», borrar un APK destruiría metadata, notas y playtime.
- **`Installation`** — una instalación física o referencia ejecutable concreta: plataforma, tipo, ruta o URI, `packageName`, versiones, estado y adapter asociado. Un juego puede tener varias (§5, §55).
  - Por qué importa: separa «qué es el juego» de «dónde está y cómo se ejecuta». También separa la plataforma del contenido de la plataforma de ejecución (§5).
- **`PlaySession`** — una sesión de juego con inicio, fin opcional, duración, motivo de terminación y origen. Se adjunta al `Game`, nunca a la instalación, y su `installationId` es nullable para conservar el historial entre reinstalaciones (§6).
  - Por qué importa: el playtime es un hecho consumido, no un estado del archivo instalado. Jugar, desinstalar y reinstalar no debe resetear el total.
- **`Metadata` + `Provenance`** — cada valor externo relevante se guarda como `MetadataFieldValue` con su `providerId`, `providerRecordId`, `sourceUrl`, `confidence` e `isSelected`, conservando los valores alternativos para revisión (§20).
  - Por qué importa: ninguna fuente es autoridad absoluta. Sin provenance, el merge entre providers es una pérdida de datos silenciosa.
- **`ExternalId`** — tabla de identificadores externos (`VNDB_ID`, `DLSITE_RJ`, `STEAM_APP_ID`, `ITCH_GAME_ID`, `ITCH_SLUG`, `F95_THREAD_ID`, `KIMOCHI_ID`, `CUSTOM`), con esquema extensible. El RJ de DLsite es de primer nivel y se normaliza a una forma canónica (§14, §15).
  - Por qué importa: es lo que permite matching cross-source fiable y detectar que tres instalaciones son la misma obra.
- **`Localization`** — idioma, tipo de traducción (`ORIGINAL`, `OFFICIAL_TRANSLATION`, `FAN_TRANSLATION`, `MACHINE_TRANSLATION`, `UNKNOWN`), título, versión, traductor y fuente (§22).
  - Por qué importa: una traducción es una propiedad de un juego, no un juego nuevo.

---

## Metadata providers

Orden inicial de implementación, según §80 Milestone 5:

- **VNDB.** Referencia principal para visual novels: títulos y aliases, developers, tags, engines, imágenes e IDs externos. Se implementa contra su API oficial v2 (`Kana`), respetando los límites de uso documentados (§17).
- **DLsite.** Títulos originales, RJ codes, developer/circle, versión, plataformas, idiomas, enlaces e imágenes (§17).
- **Steam.** App ID, título, developer/publisher, descripción, tags, fecha, plataformas, capturas y enlaces (§17).
- **itch.io.** Juegos indie y HTML5, demos, engines declarados, tags, developer y página de proyecto. Su API oficial está orientada a integraciones autenticadas, así que el acceso real debe evaluarse contra endpoints y feeds públicos documentados (§17).
- **F95Zone.** Prioridad alta de descubrimiento para el ecosistema. Provider independiente, sujeto a los términos del sitio, rate limits y autenticación si alguna función la requiere (§17, §57).
- **HaitenJP.** Descubrimiento por RJ, relación entre páginas y títulos, metadata secundaria y localización cuando aparece (§17).
- **Kimochi.** Descubrimiento, RJ, versión, developer, plataforma y referencias Android cuando están presentes (§17).
- **Providers de traducción.** Erodeus, ZonaHentaiJuegos y Code Arc Traducciones: disponibilidad de traducción al español, nombre y versión de la traducción, referencia al traductor y plataforma. Alimentan `Localization` y no reemplazan el título original (§17, §22).
- **Providers de fallback.** HentaiBedta, FapForFun, Otomi Games, ThomasTaihei y OtakuPlan: aliases, enlaces, metadata secundaria y fuentes de búsqueda alternativas (§17, §57).

**Nota de cumplimiento.** El scraping es específico por proveedor, con rate limiting y respeto a los términos de cada sitio. Se prioriza la API oficial cuando existe y es apropiada; se respetan robots y restricciones técnicas; se aplican retries con backoff y caché; no se evaden CAPTCHAs, logins, bloqueos ni mecanismos anti-bot. Ningún provider se considera terminado solo por extraer HTML: debe cumplir la Definition of Done de §82, que incluye error mapping, rate limiting, caché, tests de parser y comportamiento ante páginas modificadas. La lista de fuentes es extensible (§17, §18, §59, §82, §87, §88).

---

## Stack técnico

- **Lenguaje:** Kotlin.
- **UI:** Jetpack Compose con Navigation Compose.
- **Persistencia:** Room (SQLite), con DAOs separados por aggregate y migraciones explícitas.
- **Acceso a archivos:** Storage Access Framework, sin acceso indiscriminado al almacenamiento.
- **Integración con apps instaladas:** PackageManager e intents.
- **Concurrencia:** Kotlin Coroutines y Flow.
- **Configuración:** DataStore para preferencias simples.
- **Trabajo diferible:** WorkManager para actualización de metadata, refresh de assets, limpieza de caché y rescaneo programado. Sin servicios foreground permanentes.
- **Red:** cliente HTTP Kotlin con timeout, retry limitado, cancelación por coroutines y rate limiting por provider.
- **Backend:** ninguno en el MVP. La app accede directamente a APIs y sitios públicos (§3.1, §64).

---

## No-objetivos

No forman parte del MVP, por decisión explícita de la especificación:

- Backend público o servidor propio obligatorio (§64, §86).
- Cuentas de usuario, autenticación o multiusuario (§1, §89).
- Sincronización en nube o entre dispositivos (§89).
- Servicio, daemon o APK adicional instalado en el teléfono; todo lo necesario vive en la app principal (§65).
- Recomendaciones sociales, comunidad o storefront propio (§89).
- Hosting o redistribución de juegos (§89).
- DRM bypass (§89).
- Evasión de CAPTCHA, login o mecanismos anti-bot (§18, §89).
- Soporte universal de todos los engines (§89).
- Ejecución nativa de juegos Windows en Android sin un runtime compatible (§89).
- Ejecución nativa de Flash, que se registra como clasificación de catálogo con `Playable = false` (§42).

El backend no se descarta: se considera opcional a futuro, y los providers están diseñados para poder migrarse a un servicio externo sin cambiar el modelo de dominio (§64, §85).

---

## Documentos del proyecto

- [`SPECS.md`](SPECS.md) — especificación técnica normativa, 94 secciones. Define arquitectura, entidades, providers, contratos, invariantes de datos, criterios de aceptación y definiciones de Done.
- [`ROADMAP.md`](ROADMAP.md) — ruta de trabajo por fases (Phase 0 a Phase 8), con dependencias, línea de corte del MVP, trabajo transversal, registro de riesgos y preguntas abiertas.
- [`README.md`](README.md) — este documento.

La especificación prevalece sobre cualquier documento derivado. Los cambios que afecten a las invariantes de datos o a la separación `Game / Installation / PlaySession / Metadata` se consideran cambios arquitectónicos y obligan a actualizar `SPECS.md` (§94).

---

## Referencias

- Room: https://developer.android.com/training/data-storage/room
- Storage Access Framework: https://developer.android.com/training/data-storage/shared/documents-files
- Package visibility: https://developer.android.com/training/package-visibility
- Jetpack Compose: https://developer.android.com/develop/ui/compose
- App architecture: https://developer.android.com/topic/architecture
- VNDB API v2 (Kana): https://api.vndb.org/kana