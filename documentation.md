# Documentación de Usuario y Configuración — iUtils

Bienvenido a la guía oficial de uso, configuración y notas de versión de **iUtils** para servidores **Minecraft Paper 26.2**. Este documento se divide en dos partes principales:

1. **[Funcionalidades del Plugin, Uso y Configuración](#parte-1--funcionalidades-del-plugin-uso-y-configuración)**: Explicación detallada de cada funcionalidad del plugin, cómo funciona internamente, cómo se activa o desactiva y cómo se configura paso a paso.
2. **[Notas de la Versión (Historial de Lanzamientos)](#parte-2--notas-de-la-versión-historial-de-lanzamientos)**: Registro de cambios versión por versión (ordenado de la versión más reciente a la más antigua).

---

# Parte 1 — Funcionalidades del Plugin, Uso y Configuración

## Tabla de Contenidos de Funcionalidades

- [1.1. Resumen de Módulos e Instalación](#11-resumen-de-módulos-e-instalación)
- [1.2. Configuración General y Formatos de Texto (`general`)](#12-configuración-general-y-formatos-de-texto-general)
- [1.3. Funcionalidad 1: Mensaje de Bienvenida (`welcome`)](#13-funcionalidad-1-mensaje-de-bienvenida-welcome)
- [1.4. Funcionalidad 2: Recarga Automática de Bloques (`block-refill`)](#14-funcionalidad-2-recarga-automática-de-bloques-block-refill)
- [1.5. Funcionalidad 3: Intercambio Automático de Herramientas (`tool-swap`)](#15-funcionalidad-3-intercambio-automático-de-herramientas-tool-swap)
- [1.6. Comandos, Permisos y Recarga en Caliente (`/iutils reload`)](#16-comandos-permisos-y-recarga-en-caliente-iutils-reload)
- [1.7. Referencia Completa de `config.yml`](#17-referencia-completa-de-configyml)
- [1.8. Preguntas Frecuentes y Solución de Problemas](#18-preguntas-frecuentes-y-solución-de-problemas)

---

## 1.1. Resumen de Módulos e Instalación

**iUtils** opera de forma **100% local** (sin llamadas a servicios externos ni impacto en el rendimiento del servidor) e incluye las siguientes capacidades:

| Módulo / Función | Clave en `config.yml` | Estado por defecto | Descripción rápida |
| :--- | :--- | :---: | :--- |
| **Mensaje de Bienvenida** | `welcome` | Activo (`true`) | Envía un mensaje personalizable al jugador cuando entra al servidor. |
| **BlockRefill (Recarga de bloques)** | `block-refill` | Activo (`true`) | Repone automáticamente en la mano el bloque que estabas construyendo cuando se agota tu stack actual. |
| **ToolSwap (Intercambio de herramientas)** | `tool-swap` | Activo (`true`) | Evita que tu herramienta se rompa intercambiándola en el último instante por otra del mismo tipo en tu inventario. |
| **Recarga y Validación Defensiva** | Comando `/iutils reload` | Integrado | Aplica cambios de `config.yml` sin reiniciar el servidor y protege contra errores de sintaxis o valores inválidos. |

### Instalación y Primer Arranque

1. Asegúrate de que tu servidor utilice **Paper `26.2`** ejecutándose sobre **Java 25**.
2. Coloca el archivo `iutils-<version>.jar` (por ejemplo, `iutils-0.1.0.jar`) dentro de la carpeta `plugins/` de tu servidor.
3. Inicia el servidor.
4. Durante el arranque, **iUtils** creará automáticamente la carpeta `plugins/Iutils/` con el archivo `config.yml` predeterminado y mostrará en la consola un resumen con el estado de todos los módulos:

```text
==================================================
Plugin: Iutils v0.1.0
Creador: 925ctrl (Emerlive)
Resumen de módulos:
 - Mensaje de bienvenida: Activo
 - BlockRefill (Recarga de bloques): Activo
 - ToolSwap (Intercambio de herramientas): Activo
==================================================
```

---

## 1.2. Configuración General y Formatos de Texto (`general`)

En la parte superior de `plugins/Iutils/config.yml` se encuentra la sección `general`, que define el idioma, el prefijo global del plugin y el modo en que se procesan los colores y estilos:

```yaml
general:
  language: "es_ES"
  prefix: "<gradient:#00d2ff:#3a7bd5><bold>[Iutils]</bold></gradient> "
  format: "MINIMESSAGE"
```

### Parámetros de `general`

- **`general.language`** *(Texto, obligatorio)*: Define el identificador de idioma del plugin (por defecto `"es_ES"`). No puede estar vacío.
- **`general.prefix`** *(Texto, obligatorio)*: Prefijo que se añade delante de los mensajes del plugin (como el mensaje de bienvenida cuando `welcome.use-prefix` es `true` y los mensajes administrativos de `/iutils`).
- **`general.format`** *(Texto, obligatorio)*: Determina cómo interpreta el plugin los colores y estilos del mensaje de bienvenida y del prefijo.

### Modos de formato disponibles (`general.format`)

| Modo | Cómo funciona | Ejemplo en `config.yml` |
| :--- | :--- | :--- |
| **`MINIMESSAGE`** *(Recomendado)* | Utiliza la sintaxis moderna de Kyori Adventure MiniMessage. Permite colores estándar (`<green>`, `<yellow>`), colores hexadecimales (`<#00ff88>`), degradados (`<gradient:#00d2ff:#3a7bd5>`) y estilos (`<bold>`, `<italic>`, `<underlined>`). | `"<green>¡Hola, <yellow>{player}</yellow>!</green>"` |
| **`LEGACY`** | Utiliza los códigos de color clásicos de Minecraft con el símbolo `&` (`&a`, `&e`, `&l`, `&r`) y colores hexadecimales estilo `&#RRGGBB`. | `"&a¡Hola, &e{player}&a!"` |
| **`PLAIN`** | Muestra el texto exactamente como está escrito, sin procesar colores ni etiquetas. | `"¡Hola, {player}!"` |

> **Nota importante**: Si cambias `general.format` a `LEGACY`, recuerda actualizar también `general.prefix` y `welcome.message` para usar códigos `&` en lugar de etiquetas `<...>`.

---

## 1.3. Funcionalidad 1: Mensaje de Bienvenida (`welcome`)

### ¿Qué hace y cómo funciona?
Cada vez que un jugador se conecta al servidor (`PlayerJoinEvent`), el módulo **Welcome** construye un mensaje personalizado y se lo envía directamente a ese jugador en su chat personal:
- Si la opción `use-prefix` está activada, antepone automáticamente el `general.prefix` al mensaje.
- Sustituye todas las apariciones del marcador dinámico `{player}` por el nombre del jugador que acaba de entrar.
- **Seguridad integrada**: En modo `MINIMESSAGE`, el nombre del jugador se escapa de forma segura antes de insertarse para impedir que un nombre con etiquetas maliciosas altere el formato del mensaje.

### ¿Cómo se activa o desactiva?
En `plugins/Iutils/config.yml`, busca la sección `welcome` y modifica `enabled`:
- **Activar**: `enabled: true`
- **Desactivar**: `enabled: false`

### ¿Cómo se configura?

```yaml
welcome:
  # Activa (true) o desactiva (false) el mensaje de bienvenida.
  enabled: true

  # Si es true, añade el prefijo de "general.prefix" antes del mensaje.
  # Si es false, envía únicamente el contenido de "welcome.message".
  use-prefix: true

  # Texto que verá el jugador al entrar. Usa {player} para mostrar su nombre.
  message: "<green>¡Bienvenido al servidor, <yellow>{player}</yellow>! Esperamos que disfrutes tu estancia.</green>"
```

#### Ejemplos de personalización
- **Con degradado en MiniMessage (`general.format: "MINIMESSAGE"`)**:
  ```yaml
  welcome:
    enabled: true
    use-prefix: true
    message: "<gradient:#55ff55:#00aa00>¡Bienvenido de nuevo, <bold>{player}</bold>!</gradient>"
  ```
- **Estilo clásico con códigos `&` (`general.format: "LEGACY"`)**:
  ```yaml
  general:
    language: "es_ES"
    prefix: "&b&l[Iutils] &r"
    format: "LEGACY"

  welcome:
    enabled: true
    use-prefix: true
    message: "&a¡Bienvenido al servidor, &e{player}&a!"
  ```

---

## 1.4. Funcionalidad 2: Recarga Automática de Bloques (`block-refill`)

### ¿Qué hace y cómo funciona?
El módulo **BlockRefill** agiliza la construcción en el servidor reponiendo automáticamente el bloque en la mano del jugador cuando coloca el último bloque de su stack:

1. **Condiciones de activación al construir**:
   - El jugador debe estar en modo **Supervivencia (`SURVIVAL`)** (no interviene en modo Creativo, Aventura ni Espectador).
   - El jugador coloca el **último bloque** del stack que tiene en la mano (`cantidad == 1`), dejando esa ranura vacía.
   - Funciona tanto si construye con la **mano principal (`MAIN_HAND`)** como con la **mano secundaria (`OFF_HAND`)**.
2. **Búsqueda inteligente en el siguiente tick**:
   - En el siguiente tick del servidor (tras confirmar que la ranura de la mano quedó vacía), el plugin inspecciona las 36 ranuras de almacenamiento del inventario del jugador (`slots 0 a 35`).
   - Busca otro stack de bloques que sea **exactamente compatible** (`isSimilar`): mismo tipo de material, mismo nombre personalizado y mismos metadatos/NBT.
3. **Orden de prioridad de dónde toma el bloque de reemplazo**:
   - **1.º Mochila / Inventario interno (slots `9` al `35`)**: Prioriza los stacks guardados dentro del inventario principal (de menor a mayor índice de slot) para conservar intacta tu barra rápida (*hotbar*).
   - **2.º Barra rápida / Hotbar (slots `0` al `8`)**: Solo si no hay bloques iguales en la mochila, toma el primer stack compatible que encuentre en otra ranura de la barra rápida.
4. **Transferencia**:
   - El stack encontrado se mueve automáticamente a la mano (principal o secundaria) donde se agotó el bloque, permitiendo continuar construyendo sin abrir el inventario.

### ¿Cómo se activa, desactiva y configura?
En `plugins/Iutils/config.yml`, ubica la sección `block-refill`:

```yaml
block-refill:
  # Activa (true) o desactiva (false) la recarga automática de bloques en la mano
  # cuando un jugador en modo Supervivencia coloca el último bloque de su stack.
  enabled: true
```

- Para **activarlo**, deja `enabled: true` y ejecuta `/iutils reload`.
- Para **desactivarlo**, cambia a `enabled: false` y ejecuta `/iutils reload`.

---

## 1.5. Funcionalidad 3: Intercambio Automático de Herramientas (`tool-swap`)

### ¿Qué hace y cómo funciona?
El módulo **ToolSwap** evita que las herramientas del jugador se destruyan por accidente mientras mina, tala, cava, ara o combate, intercambiándolas automáticamente por un repuesto disponible en su inventario justo antes de romperse:

1. **Condiciones en las que interviene**:
   - El jugador debe estar en modo **Supervivencia (`SURVIVAL`)** o **Aventura (`ADVENTURE`)**.
   - El objeto que sufre desgaste (`PlayerItemDamageEvent`) debe estar en la **mano principal (`MAIN_HAND`)** o en la **mano secundaria (`OFF_HAND`)**.
   - El uso actual causaría la rotura definitiva de la herramienta (`daño acumulado + daño del uso actual >= durabilidad máxima`).
2. **Herramientas soportadas**:
   - **Espadas** (`_SWORD`)
   - **Palas** (`_SHOVEL`)
   - **Picos** (`_PICKAXE`)
   - **Hachas** (`_AXE`)
   - **Azadas** (`_HOE`)
3. **Reglas de selección del mejor reemplazo**:
   El plugin busca en el inventario del jugador (`slots 0 a 35`) otra herramienta que cumpla **todas** estas condiciones:
   - Sea exactamente de la **misma categoría** (un pico solo se reemplaza por otro pico, una espada por otra espada, etc.).
   - Tenga al menos **`2` puntos de durabilidad restante** (nunca te colocará en la mano otra herramienta que también esté a 1 uso de romperse).
   - Si hay varias herramientas del mismo tipo disponibles, elige la mejor siguiendo este **orden de prioridad de material**:
     1. **Netherita** (`NETHERITE`) — *Máxima prioridad*
     2. **Diamante** (`DIAMOND`)
     3. **Oro** (`GOLDEN`)
     4. **Hierro** (`IRON`)
     5. **Cobre** (`COPPER`)
     6. **Piedra** (`STONE`)
     7. **Madera** (`WOODEN`) — *Menor prioridad*
   - **Desempate por durabilidad**: Si tienes dos herramientas del mismo tipo y mismo material (por ejemplo, dos picos de diamante), seleccionará el que tenga **mayor durabilidad restante** (y en caso de empate exacto, el de menor índice de ranura).
4. **Qué ocurre al realizar el intercambio**:
   - Se **cancela el daño final** que iba a destruir tu herramienta actual, conservándola con `1` punto de durabilidad en tu inventario.
   - La herramienta a punto de romperse y la herramienta de reemplazo **intercambian sus posiciones** entre tu mano activa y la ranura del inventario.
   - Si el jugador **no tiene ninguna herramienta de repuesto válida** en su inventario, el plugin no cancela el evento y la herramienta se comporta de forma normal en Minecraft.

### ¿Cómo se activa, desactiva y configura?
En `plugins/Iutils/config.yml`, ubica la sección `tool-swap`:

```yaml
tool-swap:
  # Activa (true) o desactiva (false) el intercambio automático de herramientas: cuando la
  # herramienta en uso está a punto de romperse, se intercambia por otra del mismo tipo
  # que tengas en el inventario (solo espadas, palas, picos, azadas y hachas).
  # Prioridad: netherita > diamante > oro > hierro > cobre > piedra > madera.
  enabled: true
```

- Para **activarlo**, establece `enabled: true` y ejecuta `/iutils reload`.
- Para **desactivarlo**, establece `enabled: false` y ejecuta `/iutils reload`.

---

## 1.6. Comandos, Permisos y Recarga en Caliente (`/iutils reload`)

Puedes aplicar cualquier cambio realizado en `plugins/Iutils/config.yml` en tiempo real sin reiniciar el servidor mediante el comando administrativo de **iUtils** (con autocompletado `Tab`):

```text
/iutils reload
```

### Permisos

| Permiso | Descripción | Quién lo tiene por defecto |
| :--- | :--- | :--- |
| `iutils.admin` | Permite utilizar el comando `/iutils reload` y su autocompletado con `Tab`. | Operadores del servidor (`op`) |

- Si un jugador sin el permiso `iutils.admin` intenta ejecutar `/iutils reload`, verá el mensaje:
  `No tienes permiso para ejecutar este comando.`

### ¿Cómo funciona la Validación Defensiva al recargar?

Al ejecutar `/iutils reload` (desde el chat o la consola), **iUtils** valida todas las secciones de `config.yml` antes de aplicarlas:

1. **Si la configuración es válida**:
   - Actualiza instantáneamente en memoria los ajustes de `general`, `welcome`, `block-refill` y `tool-swap`.
   - Muestra el mensaje de confirmación:
     `La configuración de Iutils se ha recargado y validado correctamente.`
2. **Si existe algún error en `config.yml`** (por ejemplo, un formato desconocido en `general.format` o un texto donde se espera `true`/`false`):
   - **El plugin conserva intacta la última configuración válida en memoria** para no interrumpir el servidor.
   - Informa en el chat y en la consola la lista detallada de errores para corregirlos fácilmente:
     ```text
     [Iutils] Se encontraron errores al validar config.yml. Se mantuvo la configuración anterior:
      - El valor de 'block-refill.enabled' debe ser booleano (true o false).
     ```

---

## 1.7. Referencia Completa de `config.yml`

```yaml
# ============================================================================
# Configuración principal de Iutils
# ============================================================================
# Este archivo funciona de forma 100% local, sin servicios externos.
# Tras realizar cambios, utiliza el comando "/iutils reload" (permiso: iutils.admin)
# para validar y aplicar la nueva configuración sin reiniciar el servidor.
# ============================================================================

general:
  # Idioma predeterminado del plugin (por ejemplo: es_ES).
  language: "es_ES"

  # Prefijo global del plugin que se antepone a los mensajes cuando está habilitado.
  prefix: "<gradient:#00d2ff:#3a7bd5><bold>[Iutils]</bold></gradient> "

  # Formato utilizado para interpretar los mensajes y el prefijo.
  # Valores admitidos:
  #   - MINIMESSAGE : Etiquetas modernas de Adventure MiniMessage (<green>, <bold>, <gradient:...>)
  #   - LEGACY      : Códigos de color clásicos con ampersand (&a, &e, &l, &#RRGGBB)
  #   - PLAIN       : Texto plano sin procesamiento de colores ni estilos
  format: "MINIMESSAGE"

welcome:
  # Activa (true) o desactiva (false) el envío del mensaje de bienvenida al entrar al servidor.
  enabled: true

  # Define si el mensaje de bienvenida debe incluir el prefijo configurado en "general.prefix".
  use-prefix: true

  # Mensaje de bienvenida que recibirá el jugador al conectarse.
  # Marcadores disponibles:
  #   - {player} : Nombre del jugador que acaba de entrar al servidor.
  message: "<green>¡Bienvenido al servidor, <yellow>{player}</yellow>! Esperamos que disfrutes tu estancia.</green>"

block-refill:
  # Activa (true) o desactiva (false) la recarga automática de bloques en la mano
  # cuando un jugador en modo Supervivencia coloca el último bloque de su stack.
  enabled: true

tool-swap:
  # Activa (true) o desactiva (false) el intercambio automático de herramientas: cuando la
  # herramienta en uso está a punto de romperse, se intercambia por otra del mismo tipo
  # que tengas en el inventario (solo espadas, palas, picos, azadas y hachas).
  # Prioridad: netherita > diamante > oro > hierro > cobre > piedra > madera.
  enabled: true
```

---

## 1.8. Preguntas Frecuentes y Solución de Problemas

### 1. Coloco el último bloque de mi mano pero `block-refill` no me pone otro bloque
- **Verifica tu modo de juego**: `block-refill` solo actúa cuando estás en modo **Supervivencia (`SURVIVAL`)**.
- **Verifica que tengas bloques idénticos en tu inventario**: Deben ser del mismo tipo y tener los mismos metadatos (si colocaste un bloque con nombre personalizado en yunque, solo se recargará con otro bloque que tenga ese mismo nombre).
- **Verifica `config.yml`**: Comprueba que `block-refill.enabled` esté en `true`.

### 2. Mi herramienta se rompió y `tool-swap` no la intercambió
- **¿Tenías otra herramienta del mismo tipo?**: Un pico solo se intercambia por otro pico, una espada por otra espada, etc.
- **¿Qué durabilidad tenía la herramienta de repuesto?**: La herramienta de repuesto en tu inventario debe tener al menos **2 puntos de durabilidad restante**. Si todas tus herramientas de repuesto están a 1 punto de romperse, serán ignoradas para evitar roturas en cadena.
- **¿Es una herramienta soportada?**: El módulo cubre espadas, palas, picos, hachas y azadas de madera, piedra, cobre, hierro, oro, diamante y netherita (no aplica a tijeras, cañas de pescar, arcos o tridentes).

### 3. Las etiquetas `<green>` o `&a` se ven escritas literalmente en el mensaje de bienvenida
- Asegúrate de que `general.format` coincida con el estilo que usaste:
  - Si usas `<green>` o `<gradient:...>`, pon `format: "MINIMESSAGE"`.
  - Si usas `&a` o `&e`, pon `format: "LEGACY"`.
- Ejecuta `/iutils reload` después de guardar el archivo.

---

# Parte 2 — Notas de la Versión (Historial de Lanzamientos)

> En esta sección se documentan todas las versiones publicadas de **iUtils** (nuevas funcionalidades, correcciones de errores y cambios incompatibles). Las versiones se muestran en **orden cronológico inverso**: la versión más reciente aparece siempre arriba de la anterior.

---

## Versión `0.1.0` — Lanzamiento Inicial

Primera versión oficial de **iUtils** para servidores **Minecraft Paper 26.2** sobre **Java 25**.

### Nuevas Funcionalidades (`feat`)

- **Sistema de Bienvenida (`welcome`)**:
  - Envío automático de un mensaje de bienvenida local al jugador cuando ingresa al servidor (`PlayerJoinEvent`).
  - Soporte para los modos de formato `MINIMESSAGE`, `LEGACY` (`&`) y `PLAIN` mediante Kyori Adventure.
  - Inclusión opcional del prefijo global (`welcome.use-prefix`) y reemplazo seguro del marcador `{player}` con protección contra inyección de etiquetas.
- **Recarga Automática de Bloques (`block-refill`)**:
  - Reposición automática en la mano principal (`MAIN_HAND`) o secundaria (`OFF_HAND`) cuando un jugador en modo Supervivencia (`SURVIVAL`) coloca el último bloque de su stack.
  - Búsqueda de stacks compatibles (`isSimilar`) priorizando la mochila (`slots 9-35`) antes que el resto de la barra rápida (`slots 0-8`).
- **Intercambio Automático de Herramientas (`tool-swap`)**:
  - Protección contra rotura accidental de espadas, palas, picos, hachas y azadas en modos Supervivencia (`SURVIVAL`) y Aventura (`ADVENTURE`).
  - Cancelación del evento de rotura e intercambio automático por otra herramienta del mismo tipo con al menos `2` puntos de durabilidad restante.
  - Selección inteligente según prioridad de material (**Netherita > Diamante > Oro > Hierro > Cobre > Piedra > Madera**) y desempate por mayor durabilidad restante.
- **Administración, Recarga Segura y Resumen de Arranque (`bootstrap` / `common`)**:
  - Comando `/iutils reload` con autocompletado `Tab` protegido por el permiso `iutils.admin` (por defecto `op`).
  - Validación defensiva mediante `ConfigValidator`: si `config.yml` presenta errores al recargar, el plugin informa los fallos detalladamente y mantiene activa la última configuración válida en memoria.
  - Resumen informativo en la consola al iniciar el servidor (`StartupSummaryLogger`) mostrando versión, creador y estado (`Activo` / `Inactivo`) de cada módulo.
