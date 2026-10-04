# SPECS.md

## Game Library / Launcher — Technical Specification

**Estado:** Draft técnico v0.1  
**Plataforma principal:** Android  
**Lenguaje:** Kotlin  
**Uso previsto:** aplicación personal, un solo usuario  
**Backend propio:** no requerido para la primera arquitectura  
**Base local:** Room / SQLite  
**UI:** Jetpack Compose  

---

## 1. Objetivo

Construir una aplicación Android personal para gestionar una biblioteca de juegos heterogénea, independientemente de si cada juego está actualmente instalado o es ejecutable en el dispositivo.

La aplicación debe funcionar como:

- catálogo personal de juegos;
- gestor de instalaciones;
- launcher cuando exista un runtime/instalación compatible;
- rastreador de sesiones y tiempo de juego;
- gestor de metadata local y externa;
- buscador/agregador de múltiples fuentes de metadata;
- organizador por engines, tags, idioma, estado y plataforma;
- gestor de enlaces y referencias externas;
- detector de instalaciones/APKs;
- sistema offline-first.

La aplicación es personal. No se diseña inicialmente para múltiples usuarios, cuentas, autenticación de usuarios, sincronización multiusuario ni un backend público.

---

## 2. Principios arquitectónicos

### 2.1 Offline-first

La biblioteca local es la fuente de verdad para la experiencia de uso diaria.

La aplicación debe poder funcionar sin Internet para:

- abrir la biblioteca;
- consultar metadata ya almacenada;
- filtrar y ordenar juegos;
- consultar historial de juego;
- consultar playtime;
- ver instalaciones conocidas;
- lanzar instalaciones compatibles;
- editar metadata personal.

Internet solo será necesario para operaciones externas como:

- búsqueda de metadata;
- actualización de metadata;
- descarga/consulta de imágenes remotas;
- acceso a páginas externas;
- funciones futuras que explícitamente dependan de red.

Room es apropiado para persistir datos estructurados y permite conservar el contenido local para su uso offline.  
Referencia: https://developer.android.com/training/data-storage/room

### 2.2 El catálogo es independiente de la instalación

`Game` representa la entidad lógica del juego.

`Installation` representa una instalación concreta.

Borrar una instalación jamás debe borrar el juego del catálogo.

Ejemplo:

```text
Game: Summer Example
│
├── Installation: Android APK 1.0
├── Installation: Android APK 1.1
└── Installation: Windows build 1.2
```

Si se elimina el APK Android:

```text
Game permanece en Catalog
Installation Android -> deleted/missing
PlayHistory permanece
Metadata permanece
```

### 2.3 Playable es una vista derivada

`Playable` no es una entidad de juego independiente.

Un juego aparece como jugable cuando existe al menos una instalación cuya combinación de plataforma + runtime + launch adapter es compatible.

```text
Catalog
 ├── Game A -> playable
 ├── Game B -> catalog-only
 ├── Game C -> missing installation
 └── Game D -> playable via external runtime
```

### 2.4 Separación de metadata externa y datos personales

La metadata externa puede actualizarse.

Los datos personales nunca deben ser sobrescritos automáticamente por un proveedor externo.

Datos personales:

- estado personal del juego;
- rating personal;
- notas personales;
- tags personales;
- playtime;
- historial de sesiones;
- orden/favoritos/listas personales.

Datos externos:

- título externo;
- título alternativo;
- developer;
- descripción;
- engine;
- tags externas;
- rating externa;
- fecha de lanzamiento;
- IDs externos;
- portada/screenshot externos;
- plataforma indicada por la fuente;
- idioma/traducción detectada.

### 2.5 Provenance / procedencia

Cada valor externo importante debe poder relacionarse con su fuente.

No tratar una fuente como autoridad absoluta.

El sistema debe poder responder:

> “¿De dónde salió este título/engine/tag/RJ/versión?”

---

## 3. Stack técnico

### 3.1 Android

- Kotlin
- Jetpack Compose
- AndroidX
- Room
- Kotlin Coroutines
- Kotlin Flow
- ViewModel
- Navigation Compose
- DataStore para preferencias simples
- WorkManager para tareas diferibles (actualizaciones de metadata, escaneos programados, limpieza de caché)
- Storage Access Framework para acceso controlado a carpetas/documentos seleccionados por el usuario
- PackageManager / intents para integración con APKs y otras aplicaciones
- HTTP client Kotlin para proveedores web/API
- parser HTML específico por proveedor cuando sea necesario

Android recomienda Room para bases de datos estructuradas locales; Compose es el sistema declarativo de UI elegido para este proyecto.  
Referencias:  
- https://developer.android.com/training/data-storage/room
- https://developer.android.com/develop/ui/compose

### 3.2 Arquitectura lógica

```text
UI / Compose
    ↓
ViewModel
    ↓
Use Cases / Domain
    ↓
Repository
    ├── Room
    ├── File / SAF
    ├── PackageManager
    ├── Metadata Providers
    ├── Launch Adapters
    └── Image/asset cache
```

### 3.3 Estructura propuesta del proyecto

```text
GameLauncher/
├── app/
│   ├── src/main/
│   └── src/test/
│
├── core/
│   ├── common/
│   ├── database/
│   ├── metadata/
│   ├── filesystem/
│   ├── launcher/
│   ├── package-manager/
│   ├── networking/
│   └── images/
│
├── feature/
│   ├── home/
│   ├── library/
│   ├── game-detail/
│   ├── search/
│   ├── scanner/
│   ├── metadata-editor/
│   ├── play-history/
│   ├── statistics/
│   └── settings/
│
└── engine/
    ├── detector/
    ├── classifier/
    └── launch-adapters/
```

La separación `engine/detector` vs `launch-adapters` es intencional: detectar un engine no implica que Android pueda ejecutarlo.

---

## 4. Modelo conceptual

```text
Game
 ├── Metadata
 ├── PersonalData
 ├── ExternalIds
 ├── Tags
 ├── Installations
 ├── PlaySessions
 ├── Localizations
 └── MetadataProvenance
```

### 4.1 Game

Entidad lógica principal.

Campos base:

```text
id
canonicalTitle
version
status
summary/description
engineId
personalRating
personalNotes
coverUri
createdAt
updatedAt
lastMetadataUpdateAt
```

El nombre `canonicalTitle` representa el título elegido como principal por el usuario/sistema. No implica que sea necesariamente el título original.

### 4.2 Estado personal del juego

Debe ser independiente de disponibilidad/playability.

Valores iniciales:

```text
PENDING      = Pendiente
PLAYING      = Jugando
COMPLETED    = Completado
ABANDONED    = Abandonado
ON_HOLD      = En pausa
```

Extensible en el futuro.

### 4.3 Disponibilidad / playable status

Estado derivado:

```text
PLAYABLE
CATALOG_ONLY
INSTALLATION_MISSING
```

No confundir:

```text
Estado personal:     Jugando
Disponibilidad:      Catálogo
```

Eso es válido.

---

## 5. Installation

Representa una instalación física o referencia ejecutable concreta asociada a un `Game`.

### Campos propuestos

```text
id
GameId
platform
installationType
pathOrUri
packageName
packageVersionName
packageVersionCode
fileVersion
installedAt
lastSeenAt
isInstalled
isPlayable
launchAdapterId
runtimeId
hash
sizeBytes
notes
```

### Tipos de instalación

```text
ANDROID_APK
ANDROID_APP_REFERENCE
LOCAL_FOLDER
LOCAL_WEB_GAME
ARCHIVE
EXTERNAL_RUNTIME
UNKNOWN
```

### Plataforma

```text
ANDROID
WINDOWS
LINUX
MACOS
WEB
OTHER
UNKNOWN
```

La plataforma del contenido y la plataforma de ejecución deben poder modelarse por separado si en el futuro un runtime emula/ejecuta otra plataforma.

---

## 6. Play History / Playtime

El historial pertenece al `Game`, no a la instalación.

Esto garantiza que:

```text
Jugar 10 h
↓
Borrar APK
↓
Reinstalar
↓
Total acumulado = 10 h + nuevas sesiones
```

### PlaySession

```text
id
gameId
installationId nullable
startedAt
endedAt nullable
durationMs
terminationReason
source
```

`installationId` es opcional para conservar el historial incluso cuando una sesión se inicia desde otro medio.

### terminationReason

```text
USER_STOPPED
APP_BACKGROUND
GAME_PROCESS_ENDED
ACTIVITY_CLOSED
CRASH
UNKNOWN
```

### source

```text
LAUNCHED_BY_APP
IMPORTED
MANUAL
```

### Métricas derivadas

- total playtime;
- playtime semanal;
- playtime mensual;
- número de sesiones;
- media por sesión;
- última sesión;
- primera sesión;
- día con más tiempo;
- juego más jugado;
- top por tiempo.

### Exactitud

El sistema registrará **tiempo observado por la aplicación**, no necesariamente el tiempo interno reportado por el propio juego.

Para un APK externo, no se debe prometer exactitud absoluta si el sistema no permite observar con precisión el proceso/estado del juego.

---

## 7. Engines soportados / clasificados

El catálogo debe reconocer como mínimo:

```text
UNITY
UNREAL
GODOT
RPG_MAKER_MV
RPG_MAKER_MZ
RPG_MAKER_VX
RPG_MAKER_VX_ACE
RPG_MAKER_XP
RPG_MAKER_2000
RPG_MAKER_2003
WOLF_RPG_EDITOR
WEBGL
FLASH
OTHER
UNKNOWN
```

### Importante

`engine` es una clasificación de metadata.

No implica que la aplicación sepa ejecutar el juego.

Ejemplo:

```text
Engine: RPG Maker VX Ace
Platform: Windows
Runtime available: No
Playable: false
```

---

## 8. Detección de engine

La detección debe utilizar varias evidencias, de mayor a menor confianza.

### 8.1 Evidencias

```text
1. Manifest / metadata conocida
2. Package / app metadata
3. Nombres de archivos característicos
4. Estructura de directorios
5. Extensiones
6. Librerías internas
7. Metadata del proveedor externo
8. Nombre de archivo / heurística
```

### 8.2 Ejemplos de firmas

No deben considerarse reglas absolutas; deben implementarse como detectores con score.

```text
Unity
- libunity.so
- UnityPlayer
- Unity data folders

Unreal
- libUE*.so / patrones característicos
- UnrealGame / Engine metadata

Godot
- godot markers / project.godot en proyectos no exportados
- patrones de binarios/recursos según plataforma

RPG Maker MV
- package.json
- www/
- www/data/
- www/js/

RPG Maker MZ
- estructura de MV/MZ
- patrones de plugins/archivos propios de MZ

RPG Maker XP/VX/VX Ace
- Game.rgss*
- Game.ini
- Data/
- archivos de recursos característicos

RPG Maker 2000/2003
- RPG_RT.exe
- RTP/data patterns

Wolf RPG Editor
- Game.exe / Game.ini y estructuras conocidas

WebGL/HTML5
- index.html
- js/
- assets/
- build/ structures

Flash
- .swf
```

La lista real de firmas debe evolucionar mediante tests con ejemplos reales.

---

## 9. Launch system

El launcher no debe estar acoplado a un único engine.

Definir una interfaz conceptual:

```kotlin
interface LaunchAdapter {
    suspend fun canLaunch(installation: Installation): Boolean
    suspend fun launch(installation: Installation): LaunchResult
}
```

### Adaptadores posibles

```text
AndroidPackageLaunchAdapter
BrowserLaunchAdapter
WebGameLaunchAdapter
ExternalRuntimeLaunchAdapter
FileLaunchAdapter
UnsupportedLaunchAdapter
```

### Android APK

Para un APK instalado:

```text
Game
 ↓
Installation(ANDROID_APP_REFERENCE)
 ↓
packageName
 ↓
PackageManager / Intent
 ↓
startActivity
```

Android 11+ limita la visibilidad de otros paquetes; si la app necesita consultar explícitamente paquetes concretos, debe declarar la visibilidad adecuada con `<queries>`. Para simplemente intentar iniciar una Activity se puede usar un intent y manejar `ActivityNotFoundException`.  
Referencias:  
- https://developer.android.com/training/package-visibility
- https://developer.android.com/training/package-visibility/declaring

### Playtime de APK

El sistema debe preferir sesiones iniciadas desde la app y registrar el tiempo observado.

No asumir acceso a datos internos del APK externo.

---

## 10. Filesystem / Storage

No depender de acceso indiscriminado al almacenamiento.

Utilizar Storage Access Framework para que el usuario seleccione carpetas/documentos cuando sea necesario. Android documenta `ACTION_OPEN_DOCUMENT`, `ACTION_OPEN_DOCUMENT_TREE` y URIs persistibles para este propósito.  
Referencia: https://developer.android.com/training/data-storage/shared/documents-files

### Directorios monitorizables por el usuario

Ejemplo conceptual:

```text
Games/
├── Android/
├── RPGMaker/
├── Web/
├── Windows/
└── Unknown/
```

El usuario puede seleccionar una o más carpetas raíz.

### Scanner

El scanner debe:

1. enumerar archivos/directorios accesibles;
2. detectar APKs;
3. detectar estructuras de juegos;
4. calcular firmas/hash cuando sea útil;
5. identificar engine probable;
6. intentar asociar a un `Game` existente;
7. crear instalación si la asociación es confirmada;
8. crear juego nuevo solo si no existe candidato confiable.

Nunca borrar metadata por no encontrar temporalmente un archivo.

---

## 11. APK scanning

Para APKs:

```text
packageName
versionName
versionCode
applicationLabel
icon
install source / URI if available
APK path/URI
certificate digest (optional)
```

El `packageName` es un identificador técnico prioritario para instalaciones Android.

### APK instalado vs APK archivo

Separar:

```text
InstalledApp
APKFile
```

Un APK archivado puede existir sin estar instalado.

---

## 12. Game matching / asociación

El matching debe intentar asociar una instalación o búsqueda externa con un `Game` ya existente.

### Orden recomendado

```text
1. External ID exacto
2. URL exacta de proveedor
3. RJ code exacto
4. Package name exacto
5. Steam App ID exacto
6. VNDB ID exacto
7. itch slug/ID exacto
8. título exacto normalizado
9. alternate title exacto
10. developer + title
11. fuzzy title matching
12. similitud de metadata
13. candidato manual
```

### No hacer auto-merge con baja confianza

Si existen varios candidatos:

```text
Candidate A - 94%
Candidate B - 91%
Candidate C - 73%
```

la UI debe pedir confirmación.

---

## 13. Normalización de títulos

Debe existir una capa de normalización:

```text
TitleNormalizer
```

Funciones potenciales:

- lowercase/casefold;
- normalización Unicode;
- eliminación de espacios redundantes;
- normalización de puntuación;
- normalización de guiones;
- detección de sufijos de traducción;
- normalización de nombres de versiones;
- extracción de IDs RJ.

No eliminar información del título original de manera destructiva.

Conservar:

```text
originalTitle
normalizedTitle
aliases[]
```

---

## 14. RJ IDs

Los IDs de DLsite tipo `RJxxxxxxxx` deben considerarse identificadores externos de primer nivel.

Ejemplo:

```text
RJ0145678
```

### Formato normalizado

Guardar:

```text
identifierType = DLSITE_RJ
identifierValue = RJ0145678
```

Aceptar variantes de entrada como:

```text
RJ0145678
rj0145678
RJ-0145678
DLsite RJ0145678
```

pero almacenar una representación canónica.

### Uso

Un RJ puede servir para:

- buscar en DLsite;
- buscar en HaitenJP;
- buscar en Kimochi;
- relacionar traducciones;
- enlazar páginas de descubrimiento;
- mejorar matching cross-source.

---

## 15. External IDs

Modelo extensible:

```text
ExternalId
├── id
├── gameId
├── providerId
├── type
├── value
├── url nullable
└── verified
```

Tipos iniciales:

```text
VNDB_ID
DLSITE_RJ
STEAM_APP_ID
ITCH_GAME_ID
ITCH_SLUG
F95_THREAD_ID
KIMOCHI_ID
CUSTOM
```

No limitar el schema a estas fuentes.

---

## 16. Metadata provider system

Todos los proveedores deben implementar una interfaz común.

```kotlin
interface MetadataProvider {
    val id: ProviderId
    val displayName: String

    suspend fun search(query: MetadataQuery): List<MetadataCandidate>
    suspend fun fetch(reference: ProviderReference): MetadataResult
}
```

### Resultado normalizado

```text
MetadataResult
├── title
├── alternateTitles[]
├── version
├── developer
├── description
├── engine
├── tags[]
├── externalIds[]
├── rating
├── releaseDate
├── platforms[]
├── languages[]
├── localizations[]
├── cover
├── screenshots[]
├── downloadLinks[]
└── sourceInfo
```

No todos los proveedores tienen que rellenar todos los campos.

---

## 17. Metadata providers

Proveedores planeados:

```text
VNDB
F95Zone
DLsite
HaitenJP
Kimochi
HentaiBedta
FapForFun
Otomi Games
Steam
ThomasTaihei
OtakuPlan
Erodeus
ZonaHentaiJuegos
Code Arc Traducciones
itch.io
```

### Roles

#### VNDB

Enfoque:

- visual novels;
- títulos/aliases;
- developers;
- tags;
- engines;
- imágenes;
- relaciones externas.

VNDB dispone de API v2 (`Kana`) y limita el uso; actualmente documenta hasta 200 requests por 5 minutos y condiciones específicas de uso/datos.  
Referencia: https://api.vndb.org/kana

#### F95Zone

Prioridad alta para descubrimiento y metadata relacionada con juegos de este ecosistema.

Debe implementarse como provider independiente y respetar:

- términos del sitio;
- robots/controles técnicos aplicables;
- rate limits;
- autenticación si una función la requiere;
- no evasión de bloqueos.

#### DLsite

Enfoque:

- títulos originales;
- RJ IDs;
- developer/circle;
- versión;
- plataformas;
- idiomas;
- links;
- imágenes.

#### HaitenJP

Enfoque:

- descubrimiento por RJ;
- relación de páginas/títulos;
- metadata secundaria;
- localización/traducción si se encuentra.

#### Kimochi

Enfoque:

- descubrimiento;
- RJ;
- versión;
- developer;
- plataforma;
- referencias Android cuando estén presentes.

#### HentaiBedta / FapForFun / Otomi / ThomasTaihei / OtakuPlan

Principalmente:

- descubrimiento;
- aliases;
- enlaces;
- metadata secundaria;
- fuentes de búsqueda/fallback.

#### Steam

Enfoque:

- App ID;
- título;
- developer/publisher;
- descripción;
- tags;
- fecha;
- plataformas;
- screenshots/cover;
- links.

#### Erodeus / ZonaHentaiJuegos / Code Arc Traducciones

Enfoque especial:

- traducciones al español;
- disponibilidad de traducción;
- nombres de traducción;
- versión de traducción cuando esté indicada;
- referencia al proyecto/traductor;
- plataforma;
- enlace fuente.

Estas fuentes no deben reemplazar automáticamente el título original o metadata principal.

#### itch.io

Enfoque:

- juegos indie;
- juegos web/HTML5;
- demos;
- engines declarados cuando estén disponibles;
- tags;
- developer;
- platform;
- página del proyecto.

La API oficial de itch.io está orientada a integraciones autenticadas con cuentas/proyectos y no debe asumirse como una API pública general de búsqueda del catálogo completo. itch.io también documenta RSS para determinadas páginas/browse feeds.  
Referencias:  
- https://itch.io/docs/api/overview
- https://itch.io/docs/api/serverside

---

## 18. Scraping policy

Los scrapers son módulos por proveedor.

Nunca construir un scraper genérico que asuma HTML idéntico entre sitios.

```text
providers/
├── vndb/
├── f95/
├── dlsite/
├── haiten/
├── kimochi/
├── erodeus/
└── ...
```

### Reglas

1. Usar API oficial cuando exista y sea apropiada.
2. Usar endpoints/documentación pública cuando sea posible.
3. Respetar términos y restricciones del sitio.
4. Respetar robots.txt cuando corresponda.
5. Aplicar rate limiting por proveedor.
6. Implementar retries con backoff.
7. Cachear respuestas.
8. No bombardear sitios con búsquedas repetidas.
9. No evadir CAPTCHA, login, bloqueos o mecanismos anti-bot.
10. Registrar errores por proveedor sin romper toda la búsqueda.

### Provider health

Cada provider debe poder devolver:

```text
AVAILABLE
RATE_LIMITED
AUTH_REQUIRED
TEMPORARY_ERROR
BLOCKED_BY_PROVIDER
UNSUPPORTED_QUERY
PARSE_ERROR
DISABLED
```

---

## 19. Metadata aggregator

El agregador combina candidatos de varios proveedores.

```text
Search
  ↓
Parallel provider queries
  ↓
Candidates
  ↓
Normalization
  ↓
ID resolution
  ↓
Scoring
  ↓
Deduplication
  ↓
Merged result
  ↓
User confirmation if ambiguous
```

### Scoring conceptual

```text
exact external ID     +100
exact RJ               +100
exact package name      +100
exact normalized title   +70
alternate title          +60
developer match          +25
engine match             +10
platform match           +10
fuzzy title               +0..50
```

Estos valores son iniciales y deben ajustarse mediante pruebas reales.

### Regla de seguridad

No fusionar automáticamente por un score bajo.

---

## 20. Metadata provenance

Cada campo externo relevante puede guardar procedencia:

```text
MetadataFieldValue
├── gameId
├── fieldName
├── value
├── providerId
├── providerRecordId
├── sourceUrl
├── fetchedAt
├── confidence
└── isSelected
```

Ejemplo:

```text
field = developer
value = Example Studio
provider = DLsite
confidence = 0.98
```

Otro ejemplo:

```text
field = engine
value = RPG Maker MZ
provider = internal_detector
confidence = 0.95
```

La selección final puede ser:

```text
selected = true
```

Los valores alternativos pueden conservarse para comparación/revisión.

---

## 21. Tags

No almacenar tags únicamente como un string separado por comas.

Modelo recomendado:

```text
Tag
├── id
├── name
└── normalizedName

GameTag
├── gameId
├── tagId
├── sourceType
├── providerId nullable
├── confidence
└── isPersonal
```

### Tipos de tag

```text
EXTERNAL
PERSONAL
SYSTEM
```

Ejemplos de tags del sistema:

```text
translation:spanish
platform:android
engine:rpg-maker
availability:playable
```

Preferiblemente las propiedades estructurales como engine/platform/availability deben seguir estando en columnas/entidades propias; los tags no deben sustituir campos estructurados.

---

## 22. Localizations / traducciones

Una traducción no crea automáticamente otro `Game`.

Modelo:

```text
Game
└── Localization
    ├── language
    ├── kind
    ├── title nullable
    ├── version nullable
    ├── translator nullable
    ├── sourceProvider
    ├── sourceUrl
    └── notes
```

### Kind

```text
ORIGINAL
OFFICIAL_TRANSLATION
FAN_TRANSLATION
MACHINE_TRANSLATION
UNKNOWN
```

### Ejemplo

```text
Game: Example Game
Localization:
  language: es
  kind: FAN_TRANSLATION
  translator: Example Team
  sourceProvider: ERODEUS
  version: 1.2
```

---

## 23. Campos de Game

Campos funcionales mínimos:

```text
id
canonicalTitle
version
developer
personalStatus
description
engine
personalRating
personalNotes
cover
createdAt
updatedAt
```

### Campos recomendados adicionales

```text
sortTitle
originalTitle
releaseDate
favorite
hidden
lastPlayedAt
firstPlayedAt
totalPlaytimeMs
```

`totalPlaytimeMs` es un valor cacheado/derivado; la fuente de verdad son `PlaySession`.

---

## 24. Campos técnicos de metadata

```text
GameExternalId
GameMetadataValue
GameTag
Localization
Asset
```

### Asset

```text
id
gameId
type
uri
sourceUrl
providerId
width
height
checksum
createdAt
```

Asset types:

```text
COVER
SCREENSHOT
ICON
BANNER
THUMBNAIL
OTHER
```

---

## 25. Database schema conceptual

```text
Game
 ├──< Installation
 ├──< PlaySession
 ├──< ExternalId
 ├──< GameTag >── Tag
 ├──< Localization
 ├──< Asset
 └──< MetadataFieldValue

Provider
 └──< ProviderConfiguration
```

### Provider

```text
id
name
enabled
priority
supportsSearch
supportsFetch
supportsRj
supportsLocalization
lastSuccessfulRequestAt
lastErrorAt
lastErrorCode
```

### ProviderConfiguration

Configuración local por proveedor.

No almacenar secretos innecesarios.

---

## 26. DAO / Repository layer

DAOs separados por aggregate/feature:

```text
GameDao
InstallationDao
PlaySessionDao
ExternalIdDao
TagDao
LocalizationDao
AssetDao
ProviderDao
MetadataDao
```

Los ViewModels no deben ejecutar SQL directamente.

La capa de Repository expone modelos de dominio y Flows cuando sea apropiado.

Room genera implementaciones de DAO y verifica consultas SQL durante compilación.  
Referencia: https://developer.android.com/training/data-storage/room/accessing-data

---

## 27. Caching

### Metadata cache

Guardar resultados externos localmente para evitar consultas repetidas.

```text
queryHash
providerId
response
fetchedAt
expiresAt
```

TTL inicial conceptual:

```text
normal metadata: 7–30 días
assets: hasta que cambien o se invaliden
search results: horas/días
failed requests: backoff progresivo
```

Los TTL son configurables y no deben quedar como constantes rígidas en toda la aplicación.

---

## 28. Networking

Toda llamada externa debe:

- tener timeout;
- usar HTTPS cuando el proveedor lo soporte;
- manejar HTTP status codes;
- tener retry limitado;
- respetar rate limits;
- tener cancellation mediante coroutines;
- no bloquear el hilo UI.

### Error model

```text
NetworkError
HttpError
RateLimitError
ParseError
AuthError
ProviderUnavailable
UnknownError
```

---

## 29. Search pipeline

### Búsqueda desde usuario

Entrada:

```text
"nombre del juego"
```

Pipeline:

```text
Input
 ↓
Normalize
 ↓
Detect special IDs
 ├── RJ
 ├── Steam App ID
 ├── VNDB ID
 ├── package name
 └── URL
 ↓
Local database lookup
 ↓
External providers if needed
 ↓
Merge
 ↓
Sort by confidence/priority
 ↓
UI candidates
```

### Búsqueda por URL

Si el usuario proporciona una URL de proveedor conocida:

```text
URL
 ↓
ProviderResolver
 ↓
Provider.fetch(url/reference)
```

Preferir ID de la URL frente al título cuando exista.

---

## 30. Import / metadata update

### Primer import

```text
User selects candidate
 ↓
Create/update Game
 ↓
Save ExternalIds
 ↓
Save metadata provenance
 ↓
Save assets
 ↓
Save localization info
 ↓
Leave personal fields untouched
```

### Actualización

```text
Existing Game
 ↓
Fetch provider metadata
 ↓
Compare old/new
 ↓
Apply only external fields
 ↓
Preserve personal data
 ↓
Record provenance
```

### Cambio conflictivo

Ejemplo:

```text
Developer:
VNDB = Studio A
DLsite = Studio A/B
F95 = Studio A
```

No sobrescribir silenciosamente.

UI puede mostrar:

```text
Developer
● Studio A       VNDB / F95
○ Studio A/B     DLsite
```

---

## 31. Manual metadata

Todo juego debe poder editarse manualmente.

Si no existe coincidencia:

```text
Source = MANUAL
```

La metadata manual debe seguir siendo válida aunque no exista ningún provider externo.

---

## 32. Deletion rules

### Borrar instalación

Debe:

- marcar instalación como eliminada/no disponible;
- conservar Game;
- conservar metadata;
- conservar tags;
- conservar playtime;
- conservar historial.

### Borrar Game

Debe requerir confirmación explícita.

Opción recomendada:

```text
Delete game and linked installations
```

y no ejecutar una limpieza irreversible de datos personales sin confirmación.

### Rescan

Un rescan no debe borrar juegos no encontrados.

Solo actualiza el estado de instalaciones.

---

## 33. Scanner lifecycle

```text
Scan started
 ↓
Discover
 ↓
Inspect
 ↓
Detect engine
 ↓
Extract technical metadata
 ↓
Match existing Game
 ↓
Create/update Installation
 ↓
Update availability
 ↓
Persist
```

Estado de instalación durante scan:

```text
DISCOVERED
INSPECTING
MATCHED
UNMATCHED
UPDATED
ERROR
```

---

## 34. Automatic metadata discovery after scan

Cuando el scanner encuentre un juego nuevo:

```text
1. Detect engine
2. Detect packageName / file identifiers
3. Extract title if possible
4. Search local DB
5. Try provider search
6. Score candidates
7. Ask user if ambiguous
8. Save selected metadata
```

No realizar auto-merge destructivo.

---

## 35. UI screens

### Home

Resumen:

- juegos recientes;
- continuados;
- tiempo reciente;
- juegos instalados;
- juegos pendientes;
- traducciones nuevas detectadas.

### Library

Filtros:

```text
Engine
Platform
Availability
Personal status
Tags
Language
Translation available
Installed
Playable
Favorite
```

Orden:

```text
Title
Last played
Playtime
Date added
Rating
Developer
```

### Game detail

Mostrar:

```text
Cover
Title
Version
Developer
Status
Description
Engine
Tags
Rating
Personal notes
Playtime
Sessions
Installations
Platforms
Localizations
External links
Metadata sources
```

### Metadata editor

Permitir editar campos personales y resolver conflictos externos.

### Scanner

Mostrar:

- fuente/carpeta;
- archivos encontrados;
- engine detectado;
- asociaciones;
- juegos nuevos;
- juegos ya conocidos.

### Statistics

- total playtime;
- top juegos;
- sesiones;
- actividad semanal/mensual;
- estado de biblioteca.

---

## 36. Biblioteca: Catalog vs Playable

La UI no debe crear dos bases de datos separadas.

Debe usar consultas/vistas:

```text
Catalog = all Game
Playable = Game where exists Installation(playable = true)
```

Ejemplo:

```text
Catalog: 154 games
Playable: 42 games
Installed but currently incompatible: 8
Missing installations: 104
```

---

## 37. Favourites / collections

Feature futura pero compatible desde el schema:

```text
Collection
CollectionItem
```

Ejemplos:

```text
Favorites
To Play
Finished
Spanish Translation
RPG Maker
```

No usar tags para representar obligatoriamente colecciones manuales.

---

## 38. Import/export

Debe existir un formato portable para backup futuro.

Formato recomendado: JSON + assets opcionales.

```text
backup/
├── manifest.json
├── games.json
├── installations.json
├── sessions.json
├── tags.json
├── external_ids.json
├── metadata.json
└── assets/
```

El backup debe permitir restaurar:

- catálogo;
- metadata;
- datos personales;
- playtime;
- referencias externas.

Las instalaciones físicas no necesariamente deben incluirse dentro del backup; deben poder quedar referenciadas como `missing` al restaurar en otro dispositivo.

---

## 39. Data portability / IDs internos

Usar UUID o IDs estables internos.

Los IDs internos nunca deben depender exclusivamente de:

- título;
- ruta del archivo;
- package name;
- RJ.

Esos son identificadores auxiliares.

---

## 40. Engine detection vs runtime support

Definición estricta:

```text
Detected engine != playable
```

Ejemplo:

```text
Game
engine = UNITY
installation = WINDOWS
runtime = none
playable = false
```

Más adelante:

```text
Game
engine = UNITY
installation = WINDOWS
runtime = Android-compatible external runtime
playable = true
```

Esta separación evita que la metadata condicione artificialmente la arquitectura del launcher.

---

## 41. WebGL / HTML5 games

Deben modelarse como `LOCAL_WEB_GAME` o `WEB`.

Requisitos posibles:

```text
index.html
assets/
js/
css/
```

Launch adapter:

```text
WebGameLaunchAdapter
```

Debe poder:

- abrir URL externa en navegador;
- abrir contenido local soportado;
- usar WebView solo cuando sea necesario y técnicamente seguro.

No asumir que cualquier HTML5 game funcionará offline; puede depender de:

- service worker;
- backend;
- CORS;
- recursos remotos;
- APIs del navegador.

---

## 42. Flash

`FLASH` debe existir como clasificación del catálogo aunque no haya soporte de ejecución nativo.

A corto plazo:

```text
Engine = FLASH
Playable = false
Reason = NO_SUPPORTED_RUNTIME
```

En el futuro se puede conectar a un runtime externo si el usuario lo dispone.

---

## 43. Seguridad

### Principios

- No almacenar secretos innecesarios.
- No incrustar API keys privadas en código fuente si no son imprescindibles.
- No guardar credenciales de terceros salvo que una función explícita lo requiera.
- Validar URLs externas.
- Sanitizar contenido HTML antes de mostrarlo como texto enriquecido.
- No ejecutar HTML/JS externo sin un contexto controlado.
- Tratar imágenes y archivos descargados como contenido no confiable.
- Evitar permisos Android excesivos.

### Datos sensibles

La app es personal, pero el historial de juego y las rutas de archivos siguen siendo datos locales que no deben enviarse a terceros automáticamente.

---

## 44. Privacy

Por defecto:

```text
Game metadata -> puede consultarse a providers externos
Personal notes -> solo local
Personal rating -> solo local
PlayHistory -> solo local
File paths -> solo local
Library structure -> solo local
```

No enviar automáticamente el catálogo completo a ningún servicio externo.

Cada búsqueda externa debe ser iniciada por:

- acción del usuario; o
- tarea que el usuario haya habilitado explícitamente.

---

## 45. Background work

Usar WorkManager para tareas que puedan ejecutarse de manera diferida:

```text
- metadata refresh
- asset refresh
- cleanup cache
- scheduled rescan
```

Evitar loops permanentes o servicios foreground salvo necesidad real.

---

## 46. Concurrency

Los providers pueden ejecutarse en paralelo con límites.

Ejemplo conceptual:

```text
max global provider concurrency = N
max per-provider concurrency = configurable
```

No iniciar 15 scrapers simultáneos contra el mismo dominio.

Cada provider debe tener su propio rate limiter.

---

## 47. Search cancellation

Si el usuario abandona la pantalla de búsqueda:

```text
cancel coroutine
cancel outstanding provider requests
```

No dejar requests innecesarios en background.

---

## 48. Asset storage

Las imágenes descargadas deben tener cache local.

No duplicar la misma imagen si varios providers la devuelven.

Deduplicación opcional por:

```text
SHA-256
normalized URL
provider + asset ID
```

La UI debe poder seguir mostrando metadata aunque el asset remoto ya no esté disponible.

---

## 49. External links

Guardar como referencias:

```text
ExternalLink
├── providerId
├── url
├── label
└── type
```

Tipos:

```text
SOURCE
DOWNLOAD
STORE
TRANSLATION
DOCUMENTATION
HOMEPAGE
OTHER
```

La aplicación no debe asumir que todos los links son descargas directas.

---

## 50. Download field

El campo `Download` debe evitar ambigüedad.

Preferible:

```text
downloadLinks[]
```

en lugar de un solo `downloadUrl`.

Cada link:

```text
url
sourceProvider
label
platform
version nullable
language nullable
isPrimary
```

Así se pueden representar múltiples fuentes/versiones sin destruir información.

---

## 51. Ratings

Separar:

```text
ExternalRating
PersonalRating
```

Nunca reemplazar el rating personal con un rating externo.

Ejemplo:

```text
External: 8.3
Personal: 9.5
```

---

## 52. Versioning

No asumir que `version` es comparable semánticamente entre todos los proveedores.

Guardar opcionalmente:

```text
versionRaw
versionNormalized
versionSource
```

Ejemplo:

```text
versionRaw = "v1.04 + Spanish patch 2"
```

No destruir el valor original.

---

## 53. Developer normalization

Los developers/circles/studios pueden tener aliases.

Futuro:

```text
Developer
├── canonicalName
├── aliases[]
└── externalIds[]
```

No implementar como requisito de MVP si no es necesario.

---

## 54. Data consistency

### Invariantes

1. Un `Game` puede existir sin `Installation`.
2. Una `Installation` debe apuntar a un `Game`.
3. Una `PlaySession` debe apuntar a un `Game`.
4. Borrar instalación no borra historial.
5. Eliminar la ruta física no elimina automáticamente el Game.
6. Una metadata externa no puede sobrescribir campos personales.
7. Un engine detectado no garantiza playable.
8. Un juego con varias instalaciones sigue siendo una única entidad lógica si el matching lo determina.
9. Una traducción no crea otro Game por defecto.
10. Un provider fallando no debe impedir consultar metadata cacheada.

---

## 55. Matching multi-installation

Ejemplo:

```text
Game: Example
│
├── Android APK
│   └── package = com.example.game
│
├── Windows folder
│   └── Game.exe
│
└── Web version
    └── index.html
```

Todas pueden pertenecer al mismo `Game` si los IDs/metadata demuestran que son la misma obra.

---

## 56. User confirmation UX

En una asociación ambigua:

```text
Found 3 matches

[1] Example Game
    VNDB: v1234
    DLsite: RJ0123456

[2] Example Game Another Version
    DLsite: RJ0765432

[3] Example Game Remake
    Steam: 123456

[ Use #1 ] [ Manual ]
```

El usuario debe tener siempre una vía de override manual.

---

## 57. Provider priority

La prioridad será configurable y no un hardcode absoluto.

Ejemplo inicial:

```text
High
- F95Zone
- VNDB
- DLsite
- Steam
- itch.io

Medium
- HaitenJP
- Kimochi
- HentaiBedta
- FapForFun
- Otomi Games

Localization-focused
- Erodeus
- ZonaHentaiJuegos
- Code Arc Traducciones

Fallback
- ThomasTaihei
- OtakuPlan
```

La prioridad de una fuente puede cambiar según el tipo de dato.

Ejemplo:

```text
Developer priority:
    DLsite > F95 > others

Spanish translation priority:
    translation providers > generic providers

Visual novel engine:
    VNDB > generic source
```

---

## 58. Field-level provider priority

No tener una sola lista universal de prioridades.

Modelo:

```text
ProviderPriorityRule
├── field
├── providerId
└── priority
```

Esto permite:

```text
field = engine
provider = VNDB
priority = 100

field = spanishLocalization
provider = ERODEUS
priority = 100
```

---

## 59. Provider adapters y tests

Cada provider debe tener tests de:

- search success;
- empty result;
- multiple results;
- parse failure;
- rate limit;
- malformed HTML;
- network timeout;
- provider unavailable;
- changed HTML fixture.

Guardar fixtures anonimizadas/estáticas de páginas permitidas para pruebas cuando legal y técnicamente sea apropiado.

---

## 60. Test strategy

### Unit tests

- title normalization;
- RJ extraction;
- matching score;
- engine detection;
- availability calculation;
- playtime aggregation;
- metadata merge;
- localization merge.

### Integration tests

- Room DAO;
- repository;
- scanner;
- provider parsing;
- launcher adapters.

### UI tests

- library filtering;
- game detail;
- metadata selection;
- scanner result confirmation;
- launcher failure handling.

### Migration tests

Cada cambio de schema de Room debe tener migration test.

---

## 61. Example domain model

```kotlin
data class Game(
    val id: String,
    val canonicalTitle: String,
    val version: String?,
    val developer: String?,
    val personalStatus: GameStatus,
    val description: String?,
    val engine: EngineType,
    val personalRating: Float?,
    val personalNotes: String?,
    val coverUri: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

```kotlin
data class Installation(
    val id: String,
    val gameId: String,
    val platform: Platform,
    val type: InstallationType,
    val pathOrUri: String?,
    val packageName: String?,
    val versionName: String?,
    val versionCode: Long?,
    val isInstalled: Boolean,
    val isPlayable: Boolean,
    val launchAdapterId: String?,
    val runtimeId: String?
)
```

```kotlin
data class PlaySession(
    val id: String,
    val gameId: String,
    val installationId: String?,
    val startedAt: Instant,
    val endedAt: Instant?,
    val durationMs: Long,
    val terminationReason: TerminationReason
)
```

Los nombres exactos pueden cambiar durante implementación; el contrato conceptual no.

---

## 62. Estados de instalación

```text
UNKNOWN
DISCOVERED
INSTALLED
MISSING
UNSUPPORTED
BROKEN
REMOVED
```

`isPlayable` puede ser una propiedad derivada en lugar de un estado persistido, pero mantener un snapshot persistido puede ser útil para rendimiento. Si se persiste, debe poder recalcularse.

---

## 63. Launch failure model

```text
LaunchResult
├── Success
├── InstallationMissing
├── RuntimeMissing
├── UnsupportedPlatform
├── PackageNotInstalled
├── PermissionDenied
├── ActivityNotFound
├── ExternalRuntimeError
└── UnknownError
```

La UI debe explicar el motivo en lenguaje humano.

---

## 64. No backend en MVP

Arquitectura inicial:

```text
                INTERNET
                   │
        ┌──────────┴───────────┐
        ↓                      ↓
   Metadata APIs          Public web sources
        │                      │
        └──────────┬───────────┘
                   ↓
        ┌──────────────────────┐
        │ Android App          │
        │                      │
        │ Providers            │
        │ Aggregator            │
        │ Repository            │
        │ Room                 │
        │ Scanner              │
        │ Launcher             │
        └──────────────────────┘
```

No servidor propio obligatorio.

### Arquitectura futura opcional

Si en el futuro se desea:

- más usuarios;
- scrapers centralizados;
- actualización de parsers sin actualizar APK;
- cache central;
- sincronización multidispositivo;

se puede introducir:

```text
Android App
    ↓
Metadata Server
    ↓
Providers / Scrapers
```

Los providers deben diseñarse desde el inicio para que puedan migrarse después a un backend sin cambiar el modelo conceptual.

---

## 65. No instalar un servicio adicional en el teléfono

El MVP no debe requerir una segunda APK/servicio independiente.

Todo lo necesario para la gestión local vive en la app principal.

---

## 66. Settings

Configuraciones mínimas:

```text
Library
- scan folders
- auto scan
- scan frequency

Metadata
- enabled providers
- provider priority
- auto update
- update interval
- ask before replacing
- cache TTL

Playback
- track playtime
- background tolerance
- auto session close

Appearance
- theme
- grid/list
- cover size

Privacy
- external metadata lookup behavior
- clear cache
```

---

## 67. Metadata update modes

```text
MANUAL
ON_IMPORT
ON_OPEN_IF_STALE
SCHEDULED
OFF
```

Por defecto para MVP:

```text
ON_IMPORT = enabled
SCHEDULED = disabled
```

---

## 68. Cache invalidation

Invalidar metadata cuando:

- el usuario fuerza refresh;
- TTL expira;
- provider version cambia;
- source record ID cambia;
- parser version incompatible.

Guardar opcionalmente:

```text
providerParserVersion
```

para poder invalidar resultados antiguos tras corregir un scraper/parser.

---

## 69. Parser versioning

Cada provider puede exponer:

```text
parserVersion
```

Ejemplo:

```text
F95 parser = 3
```

Al actualizar:

```text
F95 parser 3 -> 4
```

se puede marcar metadata cacheada como potencialmente obsoleta.

---

## 70. Logging

Logs deben usar categorías:

```text
DATABASE
SCANNER
MATCHING
METADATA
PROVIDER
LAUNCHER
PLAYTIME
UI
STORAGE
```

No loggear:

- tokens;
- contraseñas;
- cookies;
- contenido privado innecesario.

Modo debug más verboso; release reducido.

---

## 71. Observabilidad local

Pantalla opcional de diagnóstico:

```text
Providers
    VNDB          OK
    F95           Rate limited
    DLsite        OK

Scanner
    Last scan: ...
    Files: 1243
    Games found: 17

Database
    Games: 154
    Installations: 89
    Sessions: 233
```

Útil para una aplicación personal porque facilita reparar providers sin adivinar qué falló.

---

## 72. Performance targets

Objetivos iniciales, no garantías absolutas:

```text
Launch app to library: fast local render
Library browsing: no network blocking
Search local catalog: < 100 ms target for normal library sizes
Metadata UI: stale/local data shown immediately
Provider search: async with progressive results
Scanner: incremental/progressive
```

La UI nunca debe quedarse esperando a un provider externo si ya dispone de metadata local.

---

## 73. Progressive metadata results

Ejemplo:

```text
User searches Game X

Local DB           -> immediate
VNDB               -> result
DLsite             -> result
F95                -> result
Translation source -> result
```

La UI puede actualizarse progresivamente en lugar de esperar a todos los providers.

---

## 74. Rate limiting local

Cada provider debe tener límites configurables.

Ejemplo conceptual:

```text
ProviderRateLimiter
├── minDelay
├── requestsPerWindow
├── burst
└── cooldown
```

Los límites reales deben venir de la documentación/observación responsable de cada proveedor; nunca asumir que todos son iguales.

---

## 75. Network fallback

Cuando un provider falla:

```text
Provider failed
 ↓
Use cached result
 ↓
If no cache:
  continue other providers
 ↓
Return partial metadata
```

La ausencia temporal de un provider nunca debe impedir consultar los demás.

---

## 76. Metadata merge examples

### Example A

```text
VNDB:
Title = Example
Developer = Studio A
Engine = RPG Maker MZ

F95:
Title = Example [v1.2]
Developer = Studio A

DLsite:
RJ = RJ0123456
```

Merged:

```text
Title = Example
Developer = Studio A
Engine = RPG Maker MZ
RJ = RJ0123456
Version = 1.2
```

### Example B — conflicting developer

No silencioso overwrite.

```text
Developer candidates:
A -> VNDB
A/B -> DLsite
```

Mostrar provenance y permitir seleccionar.

---

## 77. Personal fields never auto-overwrite

Regla crítica:

```text
External update
        ↓
[personalStatus]    KEEP
[personalRating]    KEEP
[personalNotes]     KEEP
[personalTags]      KEEP
[playHistory]       KEEP
        ↓
[external metadata] UPDATE
```

---

## 78. Versioned database

Room schema version debe incrementarse con cualquier cambio persistente.

Las migraciones deben ser explícitas y testeadas.

No usar `fallbackToDestructiveMigration()` en producción si eso puede destruir el catálogo.

---

## 79. Backup safety

El backup debe poder ejecutarse antes de migraciones importantes.

Opcionalmente:

```text
Settings > Export backup
```

Antes de migraciones críticas se puede ofrecer export manual.

---

## 80. Roadmap de implementación

### Milestone 1 — Core

- proyecto Android Kotlin;
- Compose;
- Room;
- Game;
- tags;
- personal status;
- detail screen;
- library.

### Milestone 2 — Installations

- Installation entity;
- SAF;
- scanner;
- APK detection;
- package detection;
- availability/playable calculation.

### Milestone 3 — Launcher

- Android package launcher;
- browser/web launcher;
- launch result handling.

### Milestone 4 — Play history

- PlaySession;
- timer;
- total playtime;
- statistics.

### Milestone 5 — Metadata providers

Orden inicial sugerido:

```text
1. VNDB
2. DLsite
3. Steam
4. itch.io
5. F95
6. HaitenJP
7. Kimochi
8. translation providers
9. remaining providers
```

El orden puede cambiar según las necesidades reales durante implementación.

### Milestone 6 — Aggregator

- normalizer;
- external IDs;
- scoring;
- deduplication;
- provenance;
- conflict resolution.

### Milestone 7 — Advanced

- scheduled updates;
- backup/export;
- advanced statistics;
- collections;
- advanced runtime adapters.

---

## 81. MVP acceptance criteria

El MVP se considera funcional cuando pueda:

- crear un Game manualmente;
- mostrarlo en Catalog;
- mostrar metadata y tags;
- registrar personal status;
- editar rating/notas personales;
- detectar una instalación;
- asociarla a un Game;
- distinguir Catalog de Playable;
- lanzar un APK instalado compatible;
- registrar una PlaySession;
- acumular playtime;
- borrar una Installation sin borrar Game;
- conservar historial tras reinstalación;
- buscar metadata en al menos un provider;
- importar external IDs;
- actualizar metadata sin borrar campos personales;
- funcionar con la biblioteca sin Internet usando metadata local.

---

## 82. Definition of Done para un Metadata Provider

Un provider no se considera terminado solo porque “extraiga HTML”.

Debe tener:

```text
[ ] Provider ID
[ ] Search
[ ] Fetch por referencia/ID cuando sea posible
[ ] Normalizer
[ ] Error mapping
[ ] Rate limiting
[ ] Cache
[ ] Parser tests
[ ] Empty-result behavior
[ ] Changed-page behavior
[ ] Source URL
[ ] Provenance
[ ] Disable switch
```

---

## 83. Definition of Done para un Engine Detector

```text
[ ] detect()
[ ] confidence score
[ ] evidence list
[ ] false positive tests
[ ] false negative tests
[ ] unknown fallback
[ ] human-readable reason
```

Ejemplo:

```text
Detected: RPG Maker MZ
Confidence: 0.91
Evidence:
- package.json
- www/data/
- MZ-compatible structure
```

---

## 84. Definition of Done para un Launch Adapter

```text
[ ] canLaunch()
[ ] launch()
[ ] graceful failure
[ ] installation validation
[ ] runtime validation
[ ] logs
[ ] unit tests
```

---

## 85. Future server migration

Aunque el MVP no use servidor propio, el código debe evitar mezclar:

```text
HTTP scraping logic
```

con:

```text
Game domain model
```

Correcto:

```text
F95Provider
   ↓
MetadataResult
   ↓
MetadataRepository
   ↓
Game
```

No correcto:

```text
GameRepository
   └── contiene HTML parsing de F95
```

Esto permite en el futuro mover el provider a un servicio externo.

---

## 86. Potential future architecture with backend

Solo cuando sea necesario:

```text
┌──────────────────┐
│ Android App      │
└────────┬─────────┘
         │ HTTPS
         ▼
┌──────────────────┐
│ Metadata API     │
└────────┬─────────┘
         ▼
┌──────────────────┐
│ Provider Workers │
├──────────────────┤
│ VNDB             │
│ F95              │
│ DLsite           │
│ etc.             │
└────────┬─────────┘
         ▼
┌──────────────────┐
│ Metadata DB      │
└──────────────────┘
```

No implementar en MVP salvo necesidad.

---

## 87. Source list / scope

Fuentes actualmente contempladas:

- VNDB — https://vndb.org/
- F95Zone — https://f95zone.to/
- DLsite — https://www.dlsite.com/
- HaitenJP — https://haitenjp.com/
- HentaiBedta — https://hentaibedta.net/
- FapForFun — https://fapforfun.net/post
- Otomi Games — https://otomi-games.com/
- Steam — https://store.steampowered.com/
- ThomasTaihei H-Games — https://thomastaihei-hgames.net/posts/
- OtakuPlan — https://otaku-plan.net/
- Kimochi — https://kimochi.info/
- Erodeus — https://erodeus.xyz/
- ZonaHentaiJuegos2 — https://zonahentaijuegos2.com/
- Code Arc Traducciones — https://codearctraducciones.com/
- itch.io — https://itch.io/

Esta lista es extensible.

---

## 88. Licensing / compliance

La aplicación debe mantener separadas:

1. implementación propia;
2. APIs oficiales;
3. scraping/parsing de contenido de terceros;
4. metadata recibida;
5. assets descargados;
6. código con licencias externas.

No copiar código de un proyecto externo sin revisar su licencia y obligaciones.

Si se reutiliza código GPL u otra licencia copyleft, revisar las obligaciones de distribución correspondientes antes de incorporarlo.

La aplicación personal no convierte automáticamente en autorizados todos los métodos de extracción o descarga de terceros; cada provider debe respetar las condiciones aplicables.

---

## 89. Non-goals del MVP

No son requisitos iniciales:

- backend público;
- cuentas de usuarios;
- multiusuario;
- sincronización en nube;
- recomendaciones sociales;
- comunidad;
- storefront propio;
- hosting de juegos;
- redistribución de juegos;
- DRM bypass;
- evasión de CAPTCHA/anti-bot;
- soporte universal de todos los engines;
- ejecución nativa de juegos Windows en Android sin runtime compatible.

---

## 90. Architecture summary

La arquitectura final propuesta para el MVP es:

```text
                       INTERNET
             ┌────────────┼─────────────┐
             │            │             │
           VNDB        DLsite       Steam/itch
             │            │             │
           F95         Kimochi      translation sites
             │            │             │
             └────────────┼─────────────┘
                          │
                   Metadata Providers
                          │
                   Normalizer/Merger
                          │
                   Metadata Repository
                          │
      ┌───────────────────┴────────────────────┐
      │                                        │
   Room DB                              Local storage / SAF
      │                                        │
      ├── Games                                ├── Game files
      ├── Installations                        ├── APKs
      ├── PlaySessions                         ├── Web games
      ├── ExternalIds                          └── Assets
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
      ├── Library
      ├── Game detail
      ├── Scanner
      ├── Metadata
      ├── Statistics
      └── Settings

Launcher side:

Game
 ↓
Installation
 ↓
LaunchAdapter
 ↓
Android package / browser / web runtime / external runtime
```

---

## 91. Key decisions

| Decision | Requirement |
|---|---|
| App type | Personal Android app |
| Backend | None required for MVP |
| Local DB | Room |
| UI | Jetpack Compose |
| Catalog | All known games, playable or not |
| Playable | Derived from installations/runtime |
| Installation | Separate from Game |
| Play history | Attached to Game |
| Personal data | Never overwritten by provider updates |
| Metadata | Multi-provider |
| Provider architecture | Modular per source |
| IDs | External IDs are first-class |
| RJ | First-class cross-source identifier |
| Translations | Localization entity, not separate Game by default |
| Engine | Classification, not runtime guarantee |
| Scraping | Provider-specific, rate-limited, compliant |
| Offline | Core library must work offline |
| Delete APK | Does not delete Game/history |
| Multiple installations | Supported |
| Future server | Possible without redesigning domain layer |

---

## 92. Implementation rule of thumb

Cuando exista una duda de diseño, priorizar en este orden:

```text
1. No perder datos del usuario
2. Mantener Game independiente de Installation
3. Mantener datos personales independientes de metadata externa
4. Mantener providers reemplazables
5. Mantener la aplicación funcional offline
6. Evitar permisos innecesarios
7. Evitar acoplamiento a un único provider o engine
8. Permitir evolución futura sin migraciones destructivas
```

---

## 93. Reference documentation

Android:

- Room: https://developer.android.com/training/data-storage/room
- Storage: https://developer.android.com/training/data-storage
- Storage Access Framework: https://developer.android.com/training/data-storage/shared/documents-files
- Package visibility: https://developer.android.com/training/package-visibility
- Compose: https://developer.android.com/develop/ui/compose
- App architecture: https://developer.android.com/topic/architecture

External:

- VNDB API v2: https://api.vndb.org/kana
- itch.io API overview: https://itch.io/docs/api/overview
- itch.io server-side API: https://itch.io/docs/api/serverside

---

## 94. Status of this specification

Este documento describe la arquitectura y los contratos principales. Los detalles que pueden variar durante implementación son:

- nombres exactos de clases;
- nombres exactos de tablas/columnas;
- versiones concretas de dependencias;
- firmas definitivas de detectores;
- reglas de scoring calibradas con datos reales;
- límites específicos de cada provider;
- APIs concretas de sitios de terceros;
- compatibilidad real de runtimes específicos.

Los cambios que afecten a las invariantes de datos o a la separación `Game / Installation / PlaySession / Metadata` deben tratarse como cambios arquitectónicos y actualizar este archivo.
