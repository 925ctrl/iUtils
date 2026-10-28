# Contexto y Reglas del Proyecto — Iutils (`AGENTS.md`)

**Iutils** es un plugin para servidores **Minecraft Paper 26.2** desarrollado en **Java 25** bajo una arquitectura de **Monolito Modular con Clean Architecture pragmática**. Opera de forma 100% local (sin llamadas HTTP ni servicios externos) y utiliza **Adventure API / MiniMessage** para el formateo de textos.

---

## 1. Stack Tecnológico y Entorno

- **Lenguaje**: Java 25 (Toolchain configurado en Gradle)
- **Plataforma objetivo**: PaperMC `26.2` (`26.2.build.135-stable`)
- **Build System**: Gradle (Kotlin DSL — `build.gradle.kts`)
- **API de Mensajería**: Kyori Adventure (`Component`, `MiniMessage`, `LegacyComponentSerializer`)
- **Testing**: JUnit 5 (`org.junit:junit-bom:5.12.1`) sin necesidad de levantar un servidor completo para lógica de dominio/aplicación.

---

## 2. Comandos de Desarrollo y Verificación

Ejecutar siempre mediante el Gradle Wrapper en la raíz del proyecto:

- **Windows (PowerShell/CMD)**:
  - Compilar y ejecutar tests: `.\gradlew.bat clean build`
  - Ejecutar solo tests unitarios: `.\gradlew.bat test`
  - Generar JAR (`build/libs/iutils-<version>.jar`): `.\gradlew.bat jar`
- **Linux / macOS**:
  - Compilar y ejecutar tests: `./gradlew clean build`
  - Ejecutar solo tests unitarios: `./gradlew test`

> **Regla para el Agente**: Antes de dar por finalizada cualquier modificación en el código fuente, ejecuta `.\gradlew.bat test` (o `build`) para verificar que la compilación y toda la suite de pruebas pasen sin errores.

---

## 3. Arquitectura y Reglas de Dependencia

El código base se aloja bajo el paquete raíz `io.github.iutils` y se organiza por capacidades/módulos y capas:

```text
io.github.iutils
├── bootstrap/                  # Punto de entrada (IutilsPlugin), composición manual de DI, resumen de inicio y comando /iutils
├── common/
│   ├── config/                 # Carga, validación defensiva (ConfigValidator) y snapshot inmutable (PluginConfig)
│   └── messaging/              # Motor transversal de formateo hacia Adventure Component (MessageFormatter)
└── <modulo>/ (ej. welcome/, blockrefill/, toolswap/)
    ├── domain/                 # Reglas de negocio, records y objetos de valor en Java puro
    ├── application/            # Casos de uso (*UseCase) y puertos de salida (interfaces Presenter/Executor)
    ├── presentation/           # Adaptadores hacia Adventure (Component, Audience)
    └── infrastructure/paper/   # Listeners de eventos de Bukkit/Paper y adaptadores de plataforma
```

### Restricciones Arquitectónicas Estrictas:
1. **Pureza del Dominio (`*.domain`)**:
   - **PROHIBIDO** importar clases de `org.bukkit.*`, `io.papermc.*` o `net.kyori.adventure.*` dentro de la capa `domain`.
   - Usar Java puro (`record`, `UUID`, `String`, colecciones inmutables y validaciones con `Objects.requireNonNull`).
2. **Capa de Aplicación (`*.application`)**:
   - Coordina el flujo entre la configuración/estado (`Supplier<*Settings>`) y los puertos de salida (ej. `WelcomeMessagePresenter`, `BlockRefillExecutor`, `ToolSwapExecutor`).
   - Tampoco debe acoplarse directamente a clases de `org.bukkit.*`.
3. **Inyección de Dependencias Manual**:
   - **No usar** frameworks de inyección de dependencias (Guice, Spring, Dagger) ni Singletons estáticos (`getInstance()`).
   - Toda la composición de objetos se realiza por constructor en `IutilsPlugin#onEnable()` y se limpia en `IutilsPlugin#onDisable()`.
4. **Inmutabilidad y Recarga Segura (`ConfigManager`)**:
   - La configuración en memoria se representa con `record`s inmutables (`PluginConfig`, `WelcomeSettings`, `BlockRefillSettings`, `ToolSwapSettings`).
   - Los casos de uso y presentadores que dependan de la configuración deben recibir un `Supplier<...>` (ej. `this.configManager::getWelcomeSettings`) para reflejar automáticamente los cambios tras `/iutils reload`.
   - Si `/iutils reload` falla en `ConfigValidator`, se debe conservar intacta la última instancia válida de `PluginConfig`.

---

## 4. Convenciones de Código y Seguridad

- **Formato de archivos** (`.editorconfig`):
  - Codificación `UTF-8`, saltos de línea `LF`, sin espacios al final de línea.
  - Indentación de **4 espacios** en archivos `.java` y **2 espacios** en `.yml`, `.yaml`, `.kts`, `.json`.
- **Java Moderno (Java 25)**:
  - Preferir `record` para DTOs, configuraciones y objetos de valor.
  - Usar `switch` expressions exhaustivos (como en `MessageFormatter#deserialize`).
  - Marcar clases concretas como `final` salvo que estén diseñadas explícitamente para herencia.
  - Validar precondiciones de nulidad con `Objects.requireNonNull(..., "mensaje descriptivo")` en constructores y métodos públicos.
- **Seguridad en Mensajes (Adventure / MiniMessage)**:
  - Nunca usar la API legacy `org.bukkit.ChatColor`. Todo mensaje al jugador o consola de comandos debe pasar por `MessageFormatter` y enviarse como `net.kyori.adventure.text.Component`.
  - Al reemplazar placeholders con datos no confiables (como `{player}`), escapar siempre las etiquetas en modo `MINIMESSAGE` (`miniMessage.escapeTags(...)`) para prevenir inyección de formatos/click-events.
- **Idioma**:
  - **Código y Javadoc**: Identificadores de clases, métodos, variables y comentarios Javadoc en **inglés**.
  - **Mensajes de usuario, logs y documentación (`README.md`, `documentation.md`, `config.yml`, `plugin.yml`)**: En **español**.

---

## 5. Guía para Añadir un Nuevo Módulo

Cuando se solicite crear una nueva funcionalidad o módulo (`io.github.iutils.<modulo>`):

1. **Dominio**: Crear los `record`s de configuración y modelos en `<modulo>.domain`.
2. **Configuración**:
   - Integrar el record de ajustes en `PluginConfig` (`common.config`).
   - Añadir las validaciones correspondientes en `ConfigValidator` y el mapeo en `ConfigManager`.
   - Documentar las nuevas claves con sus valores por defecto en `src/main/resources/config.yml`.
3. **Aplicación y Presentación**:
   - Crear el caso de uso (`*UseCase`) y su interfaz de salida en `<modulo>.application`.
   - Implementar el presentador con Adventure y `MessageFormatter` en `<modulo>.presentation` (si aplica).
4. **Infraestructura y Bootstrap**:
   - Crear los listeners o comandos de Paper en `<modulo>.infrastructure.paper`.
   - Instanciar y conectar las dependencias en `IutilsPlugin` y añadir el módulo al resumen de `StartupSummaryLogger`.
   - Si añade comandos o permisos nuevos, declararlos en `src/main/resources/plugin.yml`.
5. **Testing**:
   - Añadir pruebas unitarias en `src/test/java/io/github/iutils/<modulo>/` cubriendo casos felices, bordes y validaciones de configuración.
6. **Versionado, Documentación y Empaquetado**:
   - Actualizar la propiedad `version` en `gradle.properties` siguiendo las reglas de **Semantic Release** (Sección 6).
   - Actualizar obligatoriamente `README.md` y `documentation.md` siguiendo las reglas de la **Sección 7**.
   - Generar el nuevo artefacto JAR (`.\gradlew.bat clean build`).

---

## 6. Versionado con Semantic Release (`MAJOR.MINOR.PATCH`)

El proyecto inició en la versión **`0.1.0`** y gestiona su versión en `gradle.properties` (`version=X.Y.Z`) siguiendo las reglas de **Semantic Release / Semantic Versioning** en función del tipo de cambio realizado:

- **MAJOR (`X.0.0` — Breaking Changes)**: Cambios incompatibles en la estructura de `config.yml`, permisos, comandos o requisitos de plataforma.
- **MINOR (`x.Y.0` — Features / `feat`)**: Nuevas funcionalidades o módulos retrocompatibles (ej. de `0.1.0` a `0.2.0`).
- **PATCH (`x.y.Z` — Fixes / `fix`, `perf`, `refactor`)**: Correcciones de errores, mejoras internas o ajustes sin añadir nuevas funcionalidades de usuario (ej. de `0.1.0` a `0.1.1`).

> **Regla para el Agente**: Cada vez que se complete un cambio funcional, corrección o cambio incompatible en el plugin, incrementa la versión correspondiente en `gradle.properties` según Semantic Release, actualiza `README.md` y `documentation.md` (Sección 7) y ejecuta `.\gradlew.bat clean build` (o `jar`) para generar el nuevo archivo `build/libs/iutils-<version>.jar`.

---

## 7. Mantenimiento Obligatorio de Documentación (`README.md` y `documentation.md`)

Cada vez que se genere una nueva versión del plugin (ya sea un `fix` / PATCH, un `feat` / MINOR o un `breaking change` / MAJOR), el Agente **DEBE** actualizar los archivos de documentación respetando estrictamente su propósito y estructura:

### 7.1. Reglas para `README.md` (Resumen y Arquitectura)
- **Propósito**: `README.md` está dedicado a la arquitectura del proyecto, cómo clonarlo, cómo compilarlo, cómo instalar la infraestructura, cómo generar módulos y cómo contribuir. **PROHIBIDO** explicar en `README.md` cómo funciona internamente cada funcionalidad para el usuario final o cómo configurarla en `config.yml`.
- **Al añadir una nueva funcionalidad (`feat`)**:
  1. Agregar la nueva funcionalidad **únicamente en la sección de resumen inicial** (a modo de listado resumido de qué hace el plugin actualmente).
  2. Actualizar el árbol de estructura de paquetes en `README.md` con las nuevas clases creadas.
- **En cada cambio de versión (`PATCH`, `MINOR` o `MAJOR`)**:
  - Actualizar la versión actual de **iUtils** en todas las referencias dentro de `README.md` (título/introducción, encabezado de la sección de resumen, bloque de `gradle.properties` y ruta del artefacto `build/libs/iutils-<version>.jar`).

### 7.2. Reglas para `documentation.md` (Manual de Usuario y Notas de Versión)
El archivo `documentation.md` debe conservar siempre su estructura dividida en dos partes y en este orden exacto:

1. **Parte 1 (Al inicio) — Funcionalidades del Plugin, Uso y Configuración**:
   - Muestra todas las funcionalidades del plugin y explica, funcionalidad por funcionalidad, qué hace, cómo funciona, cómo se activa/desactiva y cómo se configura al detalle en `config.yml`.
   - **Cuando se cree una nueva funcionalidad (`feat`) o se modifique una existente**: Agregar o actualizar su sección detallada en esta Parte 1 (incluyendo la tabla resumen del inicio y la referencia completa de `config.yml`) para que el usuario sepa exactamente cómo utilizarla y configurarla.
2. **Parte 2 (Al final) — Notas de la Versión (Historial de Lanzamientos)**:
   - Documenta qué cambió en cada versión publicada (`fix`, `feat` o `breaking change`) con todos los detalles relacionados a esa versión.
   - **Orden estricto (última versión primero)**: Cada vez que salga una nueva versión, su sección `## Versión X.Y.Z` se debe insertar **arriba de la versión anterior** (por ejemplo, `0.2.0` o `0.1.1` se coloca justo encima de `0.1.0`), de modo que el lector siempre vea primero la versión más reciente.
