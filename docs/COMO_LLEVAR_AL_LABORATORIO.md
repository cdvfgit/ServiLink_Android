# ServiLink · Cómo llevarlo al laboratorio

Tres caminos de transporte, del recomendado al de emergencia.

## Camino 1 — GitHub (recomendado si el laboratorio tiene Studio actualizado)

Repo público: https://github.com/cdvfgit/ServiLink_Android

En la PC del laboratorio:
1. Abrir **Android Studio** → opción **"Get from VCS"**.
2. Pegar la URL `https://github.com/cdvfgit/ServiLink_Android.git` y clonar.
3. Esperar el **Gradle Sync** (descarga dependencias la primera vez; requiere internet).
4. Arrancar un emulador (o conectar un teléfono) y pulsar **Run**.

> También desde terminal: `git clone https://github.com/cdvfgit/ServiLink_Android.git`

## Camino 2 — ZIP (cero dependencia de git, mismo proyecto)

Archivo: `ServiLink_fuentes.zip` (~60 KB, en Desktop/Documents).
1. Copiar el ZIP por USB y extraerlo en la PC.
2. Abrir la carpeta extraída en Android Studio (Open → elegir carpeta).
3. Gradle Sync → Run.

> El ZIP incluye TODO el código y el **Gradle Wrapper**, así que no hace falta tener Gradle instalado: lo descarga solo.

## Camino 3 — APK (emergencia: si NO compila en el laboratorio)

Archivo: `ServiLink_app-debug_respaldo.apk` (~28 MB).
- **No requiere compilar.** Ya es la app funcional.
- Es un build de **debug**, así que no se puede instalar con doble clic en un teléfono normal (los APK debug no siempre se instalan así). Lo más fiable es por **adb**:
  ```
  adb install ServiLink_app-debug_respaldo.apk
  ```
- Requiere **internet** (la app habla con Supabase en la nube).

## ⚠️ Lo importante que hay que saber (versión de Android Studio)

Este proyecto se compiló con:
- **AGP 9.4.1** + **Gradle 9.6.0** + **Kotlin 2.2.10** + **JDK 25**
- **compileSdk / targetSdk 36**, **minSdk 34**

Si la PC del laboratorio tiene un **Android Studio más viejo** (JDK 17 o 21, AGP 8.x, SDK con API ≤ 35), **puede que NO compile pegado tal cual**. En ese caso, el plan es:
- **Bajar la versión del proyecto** (cambiar `agp`, `compileSdk 35`, `targetSdk 35`) — dos líneas, se hace en 1 minuto.
- O directamente usar el **APK (Camino 3)** y mostrar la app funcionando sin compilar.

**Recomendación:** antes del día de la defensa, preguntar al laboratorio qué versión de Android Studio tienen, y avisarme para dejar el proyecto en la versión compatible de antemano.
