# MC WebTV

Aplicación Android TV de canales paraguayos, implementada en Kotlin con Android
Leanback. Las fuentes y recursos de la app están en `app/`.

## Requisitos

- Android Studio con Android SDK instalado.
- JDK compatible con Gradle 8.13.

Android Studio crea `local.properties` para la ruta del SDK. Ese archivo es local
y está excluido de Git.

## Compilar y probar

En Windows:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

En macOS/Linux:

```sh
./gradlew testDebugUnitTest assembleDebug
```

El APK de depuración se genera en `app/build/outputs/apk/debug/app-debug.apk`.
Los artefactos de compilación y cachés no se versionan.

## Configuración remota

La app descarga `webtv-config.json` desde la rama `main` de este repositorio. El
archivo permite actualizar canales y eventos sin recompilar la app. Si la descarga
no está disponible, se usa la configuración incluida en
`app/src/main/assets/default_config.json`.

Ejemplo de canal:

```json
{
	"title": "Nombre del canal",
	"videoUrl": "https://ejemplo.com/stream.m3u8",
	"cardImageUrl": "https://ejemplo.com/logo.png",
	"category": "PARAGUAY_EXTRA",
	"referer": "https://ejemplo.com/",
	"userAgent": ""
}
```

Los eventos usan la lista `events` y admiten `title`, `description`, `videoUrl`,
`cardImageUrl`, `referer` y `userAgent`. Las propiedades opcionales se pueden
omitir. Conserva las categorías especiales existentes para que los módulos de
navegación correspondientes sigan funcionando.

## Archivos útiles

- `app/src/main/java/com/webtv/`: lógica de canales, navegación y reproducción.
- `app/src/main/res/`: recursos y layouts.
- `canales_documentacion.txt`: detalle de canales y comportamiento de reproducción.
- `webtv-config.json`: lista de canales y eventos editables remotamente.