# iUtils — Plugin Modular de Utilidades para Minecraft Paper

**iUtils** (`v0.1.0`) es un plugin de utilidades para servidores **Minecraft Paper 26.2** desarrollado en **Java 25** bajo una arquitectura de **Monolito Modular con Clean Architecture pragmática**. Opera de forma 100% local (sin llamadas HTTP ni servicios externos) y utiliza **Kyori Adventure / MiniMessage** para su capa de presentación.

---

## 1. Resumen de la Versión Actual (`0.1.0`)

En su versión inicial `0.1.0`, el plugin incluye los siguientes módulos y capacidades:

- **Sistema de Bienvenida (`welcome`)**: Envío de mensajes de bienvenida personalizables al conectarse un jugador al servidor.
- **Recarga Automática de Bloques (`blockrefill`)**: Reposición automática en la mano activa cuando un jugador en modo Supervivencia coloca el último bloque de su stack y dispone de más bloques iguales en su inventario.
- **Intercambio Automático de Herramientas (`toolswap`)**: Sustitución preventiva de herramientas (espadas, palas, picos, hachas y azadas) a punto de romperse por la mejor herramienta equivalente disponible en el inventario.
- **Gestión de Configuración y Recarga en Caliente (`bootstrap` / `common`)**: Resumen de estado de módulos al iniciar el servidor y comando administrativo `/iutils reload` con validación defensiva en memoria.

> Para consultar la guía completa de usuario, cómo funciona cada módulo a detalle, cómo activarlos y cómo configurarlos en `config.yml`, revisa el manual en **[documentation.md](documentation.md)**.

---

## 2. Arquitectura y Estructura del Proyecto

El código base sigue el patrón de **Monolito Modular organizado por funcionalidades**, aplicando principios de **Clean Architecture** pragmática e **inyección manual de dependencias por constructor** (sin contenedores de DI ni singletons estáticos).

```text
iUtils/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
├── README.md
├── documentation.md
├── AGENTS.md
├── .gitignore
├── .editorconfig
├── .github/
│   └── workflows/
│       └── build.yml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── io/github/iutils/
    │   │       ├── bootstrap/
    │   │       │   ├── IutilsCommand.java
    │   │       │   ├── IutilsPlugin.java
    │   │       │   └── StartupSummaryLogger.java
    │   │       ├── common/
    │   │       │   ├── config/
    │   │       │   │   ├── ConfigManager.java
    │   │       │   │   ├── ConfigValidationResult.java
    │   │       │   │   ├── ConfigValidator.java
    │   │       │   │   ├── MessageFormatMode.java
    │   │       │   │   └── PluginConfig.java
    │   │       │   └── messaging/
    │   │       │       └── MessageFormatter.java
    │   │       ├── welcome/
    │   │       │   ├── domain/
    │   │       │   │   ├── WelcomeMessage.java
    │   │       │   │   ├── WelcomePlayer.java
    │   │       │   │   └── WelcomeSettings.java
    │   │       │   ├── application/
    │   │       │   │   ├── SendWelcomeMessageUseCase.java
    │   │       │   │   └── WelcomeMessagePresenter.java
    │   │       │   ├── presentation/
    │   │       │   │   └── AdventureWelcomePresenter.java
    │   │       │   └── infrastructure/paper/
    │   │       │       └── PlayerJoinWelcomeListener.java
    │   │       ├── blockrefill/
    │   │       │   ├── domain/
    │   │       │   │   ├── BlockRefillAction.java
    │   │       │   │   ├── BlockRefillRequest.java
    │   │       │   │   ├── BlockRefillSettings.java
    │   │       │   │   ├── HandSlotType.java
    │   │       │   │   └── InventorySlot.java
    │   │       │   ├── application/
    │   │       │   │   ├── BlockRefillExecutor.java
    │   │       │   │   └── RefillPlacedBlockUseCase.java
    │   │       │   └── infrastructure/paper/
    │   │       │       └── BlockPlaceRefillListener.java
    │   │       └── toolswap/
    │   │           ├── domain/
    │   │           │   ├── ToolHand.java
    │   │           │   ├── ToolMaterial.java
    │   │           │   ├── ToolSlot.java
    │   │           │   ├── ToolSwapAction.java
    │   │           │   ├── ToolSwapRequest.java
    │   │           │   ├── ToolSwapSettings.java
    │   │           │   └── ToolType.java
    │   │           ├── application/
    │   │           │   ├── SwapBreakingToolUseCase.java
    │   │           │   └── ToolSwapExecutor.java
    │   │           └── infrastructure/paper/
    │   │               └── ToolBreakSwapListener.java
    │   └── resources/
    │       ├── plugin.yml
    │       └── config.yml
    └── test/
        └── java/
            └── io/github/iutils/
                ├── bootstrap/
                │   ├── IutilsCommandTest.java
                │   └── StartupSummaryLoggerTest.java
                ├── common/
                │   ├── config/
                │   │   ├── ConfigManagerTest.java
                │   │   └── ConfigValidatorTest.java
                │   └── messaging/
                │       └── MessageFormatterTest.java
                ├── welcome/
                │   ├── application/
                │   │   └── SendWelcomeMessageUseCaseTest.java
                │   └── presentation/
                │       └── AdventureWelcomePresenterTest.java
                ├── blockrefill/
                │   └── application/
                │       └── RefillPlacedBlockUseCaseTest.java
                └── toolswap/
                    ├── domain/
                    │   ├── ToolSwapRequestTest.java
                    │   └── ToolTypeTest.java
                    └── application/
                        └── SwapBreakingToolUseCaseTest.java
```

### Responsabilidades por Capa y Reglas de Dependencia

1. **`bootstrap`**: Punto de entrada del plugin (`IutilsPlugin`), composición manual de dependencias en `onEnable()`, registro de resumen de arranque (`StartupSummaryLogger`) y comando administrativo (`IutilsCommand`).
2. **`common`**: Componentes transversales de infraestructura interna: carga e inmutabilidad de configuración (`ConfigManager`, `PluginConfig`), validación defensiva (`ConfigValidator`) y formateo de componentes Adventure (`MessageFormatter`).
3. **`<modulo>.domain`**: Reglas de negocio, `record`s, enums y objetos de valor en **Java puro**. Está estrictamente prohibido importar clases de `org.bukkit.*`, `io.papermc.*` o `net.kyori.adventure.*` en esta capa.
4. **`<modulo>.application`**: Casos de uso (`*UseCase`) y puertos de salida (interfaces como `WelcomeMessagePresenter`, `BlockRefillExecutor`, `ToolSwapExecutor`). Coordina el flujo recibiendo configuraciones mediante `Supplier<*Settings>` sin acoplarse a Bukkit/Paper.
5. **`<modulo>.presentation`**: Adaptadores de presentación basados en Kyori Adventure (`Component`, `Audience`) y `MessageFormatter`.
6. **`<modulo>.infrastructure.paper`**: Adaptadores de plataforma (`Listener` de eventos de Bukkit/Paper, schedulers y acceso a inventarios) que convierten eventos del servidor en solicitudes de dominio e invocan los casos de uso.

---

## 3. Requisitos e Infraestructura de Desarrollo

### Requisitos Previos

- **Java Development Kit (JDK) 25**: Configurado mediante Gradle Java Toolchains (`foojay-resolver-convention` descarga automáticamente el JDK 25 si no está instalado localmente).
- **Git**: Para clonar el repositorio y gestionar ramas/commits.
- **Servidor Minecraft Paper `26.2`** (`26.2.build.135-stable`): Necesario únicamente para pruebas manuales en un entorno de servidor real; la suite de tests unitarios se ejecuta con JUnit 5 sin levantar un servidor.

> No es necesario instalar Gradle globalmente; el proyecto incluye el **Gradle Wrapper** (`gradlew` y `gradlew.bat`).

### Clonar y Configurar el Proyecto

1. Clona el repositorio desde GitHub y entra al directorio:
   ```bash
   git clone https://github.com/925ctrl/iUtils.git
   cd iUtils
   ```
2. Verifica las propiedades de compilación en `gradle.properties`:
   ```properties
   group=io.github.iutils
   version=0.1.0
   minecraftVersion=26.2
   paperApiVersion=26.2.build.135-stable
   javaVersion=25
   ```
3. Importa el proyecto en tu IDE (IntelliJ IDEA, VS Code o Eclipse) como proyecto **Gradle (Kotlin DSL)**.

---

## 4. Compilación, Pruebas e Instalación de Infraestructura

### Comandos con Gradle Wrapper

#### En Windows (PowerShell / CMD)
```powershell
# Compilar todo el proyecto, ejecutar tests unitarios y empaquetar el JAR
.\gradlew.bat clean build

# Ejecutar únicamente la suite de pruebas unitarias (JUnit 5)
.\gradlew.bat test

# Generar únicamente el artefacto JAR del plugin
.\gradlew.bat jar
```

#### En Linux / macOS
```bash
# Compilar todo el proyecto, ejecutar tests unitarios y empaquetar el JAR
./gradlew clean build

# Ejecutar únicamente la suite de pruebas unitarias (JUnit 5)
./gradlew test

# Generar únicamente el artefacto JAR del plugin
./gradlew jar
```

El artefacto compilado se genera en:
```text
build/libs/iutils-0.1.0.jar
```

### Despliegue en Infraestructura Local (Paper 26.2)

1. Descarga o compila un servidor **Paper 26.2** y ejecútalo con **Java 25**.
2. Genera el artefacto del plugin ejecutando `./gradlew clean build`.
3. Copia `build/libs/iutils-0.1.0.jar` dentro del directorio `plugins/` de tu servidor Paper.
4. Inicia o reinicia el servidor para que el plugin registre sus módulos y genere el directorio `plugins/Iutils/`.

### Integración Continua (CI)

El repositorio cuenta con un flujo de GitHub Actions en `.github/workflows/build.yml` que se dispara en cada `push` o `pull_request` hacia `main`, `master` o `develop`:
- Configura **Eclipse Temurin JDK 25**.
- Ejecuta `./gradlew clean build --no-daemon`.
- Publica el artefacto `build/libs/*.jar` como `iutils-plugin-jar`.

---

## 5. Cómo Contribuir y Generar Nuevos Módulos

### Paso a Paso para Crear un Módulo (`io.github.iutils.<modulo>`)

1. **Capa de Dominio (`<modulo>.domain`)**:
   - Crea los `record`s de configuración (ej. `<Modulo>Settings`), entidades y objetos de valor usando exclusivamente Java 25 puro.
   - Valida precondiciones con `Objects.requireNonNull(..., "mensaje")` en constructores compactos y métodos públicos.
2. **Integración con la Configuración Tipada (`common.config`)**:
   - Añade el record `<Modulo>Settings` al snapshot inmutable `PluginConfig`.
   - Implementa las reglas de validación defensiva en `ConfigValidator` y el mapeo de lectura en `ConfigManager`.
   - Expón un método `get<Modulo>Settings()` en `ConfigManager` para inyectarlo como `Supplier` en la capa de aplicación.
   - Añade las claves por defecto en `src/main/resources/config.yml`.
3. **Capa de Aplicación y Presentación (`<modulo>.application` / `<modulo>.presentation`)**:
   - Define el caso de uso (`*UseCase`) y las interfaces de salida necesarias.
   - Recibe la configuración a través de `Supplier<<Modulo>Settings>` para que cualquier recarga en memoria se refleje sin recrear instancias.
   - Si el módulo envía mensajes al jugador, implementa el presentador en `<modulo>.presentation` utilizando `MessageFormatter` y componentes de Kyori Adventure.
4. **Capa de Infraestructura y Bootstrap (`<modulo>.infrastructure.paper` / `bootstrap`)**:
   - Implementa los `Listener`s o adaptadores de Paper en `<modulo>.infrastructure.paper`.
   - Instancia y conecta manualmente las dependencias por constructor dentro de `IutilsPlugin#onEnable()` y limpia referencias en `IutilsPlugin#onDisable()`.
   - Si el módulo añade comandos o permisos, regístralos en `src/main/resources/plugin.yml` y documenta el módulo en `StartupSummaryLogger` y en `documentation.md`.
5. **Pruebas Unitarias (`src/test/java/io/github/iutils/<modulo>/`)**:
   - Añade pruebas unitarias con JUnit 5 para la lógica de dominio, casos de uso y validaciones de configuración en `ConfigValidatorTest` / `ConfigManagerTest`.
   - Verifica que `./gradlew clean build` pase sin errores antes de abrir un Pull Request.

### Convenciones de Código y Versionado (Semantic Release)

- **Formato**: Codificación `UTF-8`, saltos de línea `LF`, indentación de `4` espacios en `.java` y `2` espacios en `.yml`/`.kts` (definido en `.editorconfig`).
- **Idioma**: Identificadores de código y comentarios Javadoc en **inglés**; mensajes al usuario, logs y documentación en **español**.
- **Versionado (`gradle.properties`)**: A partir de la versión base `0.1.0`, el proyecto sigue **Semantic Versioning (`MAJOR.MINOR.PATCH`)**:
  - **MAJOR (`X.0.0`)**: Cambios incompatibles en la estructura de configuración, permisos, comandos o plataforma.
  - **MINOR (`0.Y.0`)**: Nuevos módulos o funcionalidades retrocompatibles.
  - **PATCH (`0.1.Z`)**: Correcciones de errores, refactorizaciones internas o mejoras de rendimiento sin cambios funcionales para el usuario.
