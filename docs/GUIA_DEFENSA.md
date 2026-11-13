# ServiLink · Guía de defensa (S5)

> Conceptos clave para presentar/defender el proyecto. Resumen breve y al grano.

## 1. Arquitectura (MVVM sencilla)

El proyecto sigue **MVVM simple** (sin over-engineering):
- **UI (presentación):** Jetpack Compose. Archivo `ui/Pantallas.kt` con pantallas: Login, Inicio (cliente), Técnico y Calificar.
- **Datos (data):** `data/SupabaseClient.kt` (Retrofit + modelos) y `data/Repositorio.kt` (funciones que usan las pantallas).
- **Modelos:** `Solicitud`, `Calificacion`, `Perfil`, etc., mapeados a JSON con Gson.

Flujo: **Pantalla → Repositorio → Retrofit → Supabase (REST)**. El token e id del usuario se guardan en memoria (simplicidad para la demo).

## 2. REST / JSON (conexión con Supabase)

- **Auth:** `POST auth/v1/token?grant_type=password` con correo+contraseña → devuelve `access_token` + `user` (id y rol).
- **Tablas:** rutas con prefijo **`rest/v1/`** (ej. `rest/v1/solicitudes`, `rest/v1/calificaciones`).
- **Filtros PostgREST:** operador `eq.` (ej. `?estado=eq.PENDIENTE`, `?cliente_id=eq.<id>`).
- **Métodos:** GET (listar), POST (crear), PATCH (cambiar estado).
- **Cabecera `Prefer: return=representation`** para que POST/PATCH devuelvan la fila creada/actualizada.
- **Cabeceras `apikey` + `Authorization: Bearer`** (clave publicable / token del usuario).
- **JSON:** Retrofit + Gson serializan/deserializan los modelos automáticamente.

## 3. Flujo de pantallas (recorrido de punta a punta)

1. **Login** (cliente/técnico) → según rol navega a Inicio (cliente) o Panel (técnico).
2. **Cliente publica** solicitud (estado PENDIENTE).
3. **Técnico** ve pendientes y **Acepta** (PATCH → EN_CAMINO + tecnico_id).
4. **Técnico finaliza** (PATCH → FINALIZADO).
5. **Cliente** ve FINALIZADO + botón **Calificar** (1-5 + comentario) → guarda en `calificaciones`.

Navegación con **Navigation Compose**: rutas `login`, `inicio`, `tecnico`, `calificar/{solicitudId}/{tecnicoId}`.

## Datos demo
- Cliente: `cliente@demo.com` / `ServiLink2026!`
- Técnico: `tecnico@demo.com` / `ServiLink2026!`
