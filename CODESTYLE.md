# CODESTYLE.md

## Propósito y alcance

Este documento fija **cómo** se escribe el código de este proyecto. No fija **qué** hace el sistema.

- `SPECS.md` es normativo y define el comportamiento, las invariantes de datos y la separación `Game / Installation / PlaySession / Metadata`.
- `CODESTYLE.md` es normativo y define convenciones de código, Git, CI y entrega.
- Ante un conflicto de comportamiento, prevalece `SPECS.md`. Ante un conflicto de estilo, prevalece este documento.
- Aplica al mantenedor y a cualquier agente de IA que escriba código en este repositorio. Un revisor debe poder ejecutar la lista de las siete reglas sobre un PR sin contexto adicional.

---

## Estado del proyecto

Esta sección existe para que el documento no mienta.

- **Fase:** especificación técnica. No hay implementación.
- **Código:** ninguno. No existen source sets, ni `build.gradle`, ni manifiesto.
- **Repositorio Git:** no inicializado todavía. Sin remote.
- **CI:** no existe `.github/workflows/`.
- **Tests:** no hay runner configurado. La pirámide de `§60` está especificada pero no instantiated.
- **Build:** no hay forma de compilar todavía.

**Consecuencia:** todas las convenciones de este documento son **objetivos que el primer commit debe cumplir**, no descripciones de una práctica existente. Cuando exista código, esta sección se actualiza y pasa a describir hechos.

---

## Las siete reglas

Lista ejecutable. Un revisor la corre de arriba abajo sobre cada PR.

- [ ] **R1 — Todo en inglés.** Código, identificadores, comentarios, KDoc, mensajes de log, `strings.xml`, copy de UI, mensajes de commit, títulos de PR y archivos de workflow están en inglés.
- [ ] **R2 — Debugeable.** Logs categorizados según `§70`, build debug que sigue siendo debugeable, sin ofuscación que destruya la trazabilidad del binario que se entrega, cero secretos o datos personales en logs, superficie de Diagnóstico según `§71`.
- [ ] **R3 — Clean Code.** Responsabilidad única, dirección de dependencias UI → domain ← data, sin tipos de Android en la capa de dominio, sin I/O bloqueante en el hilo principal, errores tipados en vez de excepciones tragadas.
- [ ] **R4 — GitHub propio.** Todo el código vive en el repositorio GitHub de este proyecto. Sin forks, sin mirrors de terceros, sin remotes alternativos. Cuenta del mantenedor: `Neroovc`.
- [ ] **R5 — Las compilaciones usan GitHub Actions.** El build no es un paso manual en la vía de entrega. Un run verde es condición previa para entregar un APK.
- [ ] **R6 — El APK se descarga en la carpeta del proyecto.** Tras un run exitoso, el APK se baja del artifact de Actions y se deja en la carpeta local de artefactos del proyecto.
- [ ] **R7 — Nada de APKs construidos a mano.** Un APK que no venga de un run verde de CI no se entrega. Se registra nombre de archivo y checksum para verificar el binario.

### R1 — El límite del idioma

La regla tiene una tensión aparente y hay que resolverla explícitamente.

- **Artefactos de planificación** — `SPECS.md`, `ROADMAP.md`, `README.md`, este documento — permanecen en español neutro profesional. Es el idioma histórico del proyecto y no se reescriben.
- **Todo lo que entra al repo y shippea** — código, identificadores, comentarios, KDoc, `strings.xml`, copy de UI, mensajes de log, mensajes de commit, títulos de PR, `.github/workflows/`, archivos de configuración, nombres de test — está **en inglés**.

El criterio que decide: *¿esto termina dentro de la app o dentro del tooling?* Si sí, inglés.

```
Mal:   // obtener el tiempo de juego del usuario
Bien:  // Returns the accumulated playtime for this game.
Mal:   val errorAlEscanear = "No se encontró la carpeta"
Bien:  val scanErrorMessage = "Folder not found"
```

---

## Capas y dirección de dependencias

`§3.3` define la estructura de módulos. La dirección de dependencias es **estrictamente** hacia adentro:

```text
Compose UI  ──►  ViewModel / Use Case  ──►  Domain  ◄──  Data (impl)
                                                      │
                                                      └──► Room / SAF / Network
```

Reglas:

- La capa **domain** no importa nada de Android. Ni `Context`, ni `ContextCompat`, ni `Uri`, ni anotaciones deRoom, ni clases de `android.*`. Es lo que permite testearla en JVM sin Robolectric.
- La capa **data** implementa interfaces declaradas en **domain**. La dependencia se invierte con inyección por constructor.
- La capa **UI** no habla con repositorios directamente: pasa por ViewModel y Use Cases.
- Los **Use Cases** son la única puerta de entrada a operaciones de negocio. Un Provider no llama a un DAO.
- Ningún módulo `feature/*` depende de otro `feature/*`. Si dos features necesitan lo mismo, sube a `core/*`.
- Los **providers**, **engine detectors** y **launch adapters** son implementaciones sustituibles: consumen un contrato, nunca el revés. Un Provider no sabe que existe Room.

### Agregar un componente nuevo

Cada tipo de componente tiene su Definition of Done en la spec. Un PR que agrega uno se valida contra ella:

- **Metadata Provider** → `§82`. No está terminado porque "extraiga HTML". Requiere adapter con el contrato común, manejo de rate limit, provenance por campo, política de compliance de `§18` y tests de parsing con fixtures.
- **Engine Detector** → `§83`. Requiere confianza explícita,criteria de desempate y tratamiento del caso "engine detectado ≠ ejecutable" de `§40`.
- **Launch Adapter** → `§84`. Requiere el contrato `LaunchAdapter`, el modelo de fallo de `§63` y verificaciones previas a launch.

---

## Convenciones de nombres

| Constructo | Convención | Ejemplo |
|---|---|---|
| Paquete | lowercase, sin números, por capa y feature | `com.<owner>.app.domain.model` |
| Clase / Interfaz | `PascalCase` | `GameRepository`, `MetadataProvider` |
| Interfaz de repositorio | `I` no se usa; el nombre sin sufijo | `GameRepository` |
| Implementación | sufijo `Impl` | `GameRepositoryImpl` |
| Sealed interface / state | `PascalCase` | `ScanState`, `LaunchResult` |
| Data class | `PascalCase` | `Installation`, `ExternalId` |
| Enum | `PascalCase`, valores `UPPER_SNAKE` | `InstallationStatus.MISSING` |
| Composable | `PascalCase` + sufijo `Screen` si es pantalla completa | `GameDetailScreen`, `LibraryFilterBar` |
| ViewModel | sufijo `ViewModel` | `GameDetailViewModel` |
| Use Case | verbo + `UseCase` | `LaunchGameUseCase`, `ScanFoldersUseCase` |
| Room entity | `PascalCase`, en `data/db/entity` | `GameEntity` |
| DAO | `XxxDao` | `GameDao` |
| Metadata Provider | `XxxProvider` | `VndbProvider`, `DlsiteProvider` |
| Engine Detector | `XxxDetector` | `RenPyDetector`, `NscripterDetector` |
| Launch Adapter | `XxxAdapter` | `AndroidPackageAdapter`, `BrowserAdapter` |
| Función de extensión | `extensionName` en camelCase, no verbosa | `fun Game.isPlayable()` |
| Recurso string | `snake_case`, prefijo de pantalla | `library_empty_title` |
| Drawable | `snake_case` | `ic_engine_generic` |
| Test | `ClaseSubjectTest` / `ClaseSubjectTest` para función | `GameRepositoryImplTest`, `mergeScrapedFieldsTest` |

Los identificadores de dominio ya establecidos en `SPECS.md` se respetan tal cual: `Game`, `Installation`, `PlaySession`, `ExternalId`, `Localization`, `Provenance`, `LaunchAdapter`, `MetadataProvider`, `MergeOutcome`, `MigrationOutcome`, `NetworkError`, `LibraryRefreshResult`. No se renombran por gusto; si cambian, es cambio arquitectónico y actualiza `§94`.

---

## Idiomas de Kotlin

**Obligatorio**

- `sealed interface` para estado de UI y para modelos de error. El compilador obliga a cubrir todos los casos.
- Modelos de error explícitos, no `String` ni `Int` como código de error.
- Inyección de dispatcher en el constructor de todo lo que toque red o disco, para que los tests no dependan del scheduler real.
- `Flow` para streams; `suspend` para una operación. Nunca `Flow` con un `suspend` adentro.
- Funciones cortas. Si un cuerpo pasa de ~20 líneas o tres niveles de indentación, se extrae.
- Nombres que digan qué hace, no qué mecánicamente usa. `isShellTitle`, no `checkTitle2`.
- Sin mutación de estado compartido entre capas.

**Prohibido**

- `!!` sin invariante documentado en el KDoc inmediatamente anterior.
- `lateinit` para algo que se puede inicializar en el constructor o con valor por defecto.
- `GlobalScope`, `CoroutineScope(Dispatchers.Default)` creado dentro de una clase de dominio. El scope lo posee quien lo crea: `viewModelScope` en UI, un scope inyectado en data.
- `catch (e: Exception) {}` vacío. Un error tragado es un bug futuro. Si se maneja, se registra y se traduce a un tipo de error.
- `runBlocking` fuera de un entry point de test.
- I/O bloqueante (`File.readText()`, `SharedPreferences`, `Thread.sleep`) en el hilo principal.
- `object` singletons con estado mutable. Si hay estado, es una dependencia inyectada con ciclo de vida explícito.
- Cadenas mágicas. Constantes con nombre o recursos.

```kotlin
// Prohibido: traga el error, el llamador no sabe qué pasó
try { dao.insert(game) } catch (e: Exception) { }

// Bien: error tipado, logueado, traducible a estado de UI
try {
    dao.insert(game)
} catch (e: SQLiteConstraintException) {
    Log.w(TAG, "Game already present: ${game.id}", e)
    throw RepositoryError.DuplicateGame(game.id)
}
```

---

## Git, commits y pull requests

### Ramas

- `main` — siempre desplegable.
- `feature/<slice>-<descripcion-corta>` y `fix/<slice>-<descripcion-corta>`.
- `chore/<slice>-<descripcion-corta>` para trabajo que no toca comportamiento.
- El slice va primero. Si un cambio tiene partes independientes, cada parte es una rama.

### Commits

Conventional Commits. Los scopes son los del dominio, no de la capa:

```text
feat(library):    novelita el filtro de disponibilidad
feat(scanner):    detecta estructura Ren'Py en la carpeta
fix(aggregator):  no sobrescribe el campo de traduccion personal
refactor(providers): extrae el scraper base de HTML
perf(search):     cancela la busqueda anterior al escribir
test(aggregator): cubre el caso de empate de scoring
docs(spec):       precisa la regla de precedencia de provider
chore(ci):        sube la retencion de artifacts a 14 dias
build(deps):      actualiza AGP a la version fijada
```

| Bien | Mal |
|---|---|
| `fix(scanner): no sobreescribe installation al reescanear` | `arreglado` |
| `feat(metadata): agrega provenance por campo` | `nuevos cambios` |
| `refactor(providers): unifica rate limit de §18` | `update` |
| Un commit = una decisión | Cinco archivos sin contexto |

### Pull requests

- **Título:** el mismo Conventional Commit, sin cuerpo.
- **Cuerpo:** qué cambia, por qué, qué sección de `SPECS.md` implementa, cómo se verificó, qué queda fuera.
- **Obligatorio:** referenciar las secciones de `SPECS.md` que el PR implementa. Si no implementa ninguna y no es `chore`/`docs`, la pregunta es por qué existe.
- **Tamaño:** un PR por encima de ~400 líneas modificadas se parte en slices revisables. Este es el límite de carga de revisión del mantenedor, no una preferencia.

```text
## Qué
Implementa §12 (matching de Game ↔ Installation) sobre el scanner existente.

## Por qué
El scanner detecta el APK pero no lo asocia a un Game, así que toda
instalación nueva aparecía como juego suelto.

## Verificación
- Unit tests del scoring en app/src/test/...
- Escaneo manual sobre 2 carpetas: 1 match correcto, 1 sin match (queda UNMATCHED)

## Fuera de alcance
- Multi-instalación del mismo Game (§55) — próximo slice.
- Confirmación UX (§56) — depende de este PR.
```

---

## CI y entrega del APK

### Workflow

- Ubicación: `.github/workflows/`.
- Es la **fuente de verdad** de cómo se compila la app. Si el build local difiere del CI, el CI gana.
- Gatillos: pull request, push a `main`, y `workflow_dispatch` manual.
- Jobs obligatorios, en este orden:
  1. **build** — `./gradlew assemble<Variant>`
  2. **test** — `./gradlew test<Variant>`
  3. **lint** — `./gradlew lint<Variant>`
  4. **upload artifact** — el APK con nombre versionado, no `app.apk`
- Un run rojo bloquea la entrega. No hay excepción.
- La cuenta del mantenedor tiene los scopes `repo` y `workflow`, así que el workflow se puede pushear y los artifacts se pueden subir.
- **Los secretos van en GitHub Secrets, nunca en el repo.** Ni keystore, ni tokens de API, ni `.env`.

### Carpeta de artefactos

- El APK descargado vive en la carpeta local de artefactos del proyecto (`apk/` — nombre propuesto, el mantenedor puede renombrarlo).
- Solo se entrega el artifact verde. Un APK compilado a mano no se entrega, ni para probar.
- Cada entrega registra **nombre de archivo + md5 o sha256**, para que el binario sea verificable.

```text
PR abierto
    │
    ▼
CI verde  ──── rojo ──► se corrige, no se entrega
    │
    ▼
merge a main
    │
    ▼
download artifact desde Actions
    │
    ▼
verificar checksum
    │
    ▼
copiar a <proyecto>/apk/
    │
    ▼
reportar ruta + checksum
```

---

## Definition of Done para un cambio

- [ ] Tests escritos según `§60`, o una excepción justificada por escrito en el PR.
- [ ] Log agregado en cada camino de fallo nuevo, con la categoría correcta de `§70`.
- [ ] Sin datos personales, tokens ni cookies en los logs (`§44`, `§70`).
- [ ] Sin permisos nuevos, o justificación escrita de cada uno (`§43`, `§92`).
- [ ] Sin `fallbackToDestructiveMigration()`; si cambia el schema, hay migración explícita y testada (`§78`).
- [ ] Los campos personales siguen sin sobrescribirse ante una actualización externa (`§77`).
- [ ] Funciona offline para lo que la app promete offline (`§2.1`).
- [ ] `SPECS.md` actualizado si el cambio toca una invariante de datos o la separación `Game / Installation / PlaySession / Metadata` (`§94`).
- [ ] `ROADMAP.md` actualizado si el cambio mueve el alcance o cierra una tarea.
- [ ] PR por debajo de ~400 líneas, o partido en slices con un orden explícito.
- [ ] CI verde.

---

## Anti-patrones

| ❌ No | ✅ Sí |
|---|---|
| `catch (e: Exception) {}` | Error tipado, logueado, propagado como estado |
| `File.readText()` en el hilo principal | `withContext(Dispatchers.IO)` con dispatcher inyectado |
| `Context` o `Uri` en la capa de domain | Interfaz de dominio neutra, implementación en data |
| `"Ren'Py"` hardcodeado en tres lugares | Constante o recurso; un solo origen |
| Lógica de scraping copiada por provider | Adapter base con el contrato común (`§82`) |
| `Log.d("respuesta: $respuestaCompleta")` | Log del código y el conteo, nunca del cuerpo entero |
| PR de 1200 líneas | Slices encadenados, cada uno verde y instalable |
| APK compilado a mano y entregado | Artifact de un run verde, con checksum |
| `keystore.jks` versionado | Keystore fuera del repo; GitHub Secrets para credenciales |
| `android.permission.READ_EXTERNAL_STORAGE` "por las dudas" | Solo el permiso que la función necesita, justificado (`§92`) |
| `fallbackToDestructiveMigration()` | `Migration` explícita con su test (`§78`) |
| Un `Game` por traducción | `Localization` con idioma, tipo y versión (`§22`) |
| `isPlayable` como booleano guardado | Valor derivado de las instalaciones (`§4.3`, `§36`) |

---

## Gaps conocidos

Decisiones pendientes antes del primer commit:

- **Signing de release.** No hay keystore ni estrategia de firma definida. Mientras tanto solo existe `debug`.
- **Runner de tests.** `§60` define la pirámide; falta elegir e instanciar el stack concreto (JUnit, mocking,_coroutines_, Espresso/Compose UI test).
- **Versiones.** Gradle, AGP, Kotlin y Compose no están fijadas. `§94` las deja abiertas a propósito.
- **Retención de artifacts.** Cuánto tiempo GitHub Actions conserva el APK.
- **Nombre del repositorio.** La regla está documentada; el nombre exacto bajo `Neroovc` no está decidido.
- **Nombre de la carpeta de artefactos.** Propuesta `apk/`; el mantenedor puede renombrarla.
- **Queries de Room sin índice.** `§25` no define índices; con bibliotecas grandes el impacto en `§72` hay que medirlo.

---

 Cuando exista código, la sección **Estado del proyecto** se reescribe como descripción de hechos y los **Gaps conocidos** se vacían o se convierten en deuda documentada.