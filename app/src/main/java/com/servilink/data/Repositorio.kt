package com.servilink.data

// ===== REPOSITORIO: funciones fáciles para las pantallas =====
object Repositorio {

    // Guarda aquí dentro los datos del login
    private var tokenActual: String? = null
    var usuarioId: String? = null
    var rolUsuario: String? = null

    // Login con correo y contrasena. Devuelve el token o null si falla.
    suspend fun login(correo: String, contrasena: String): String? {
        val cuerpo = mapOf(
            "email" to correo,
            "password" to contrasena
        )

        val respuesta = ServicioApi.api.iniciarSesion(cuerpo)
        if (respuesta.isSuccessful) {
            val token = respuesta.body()?.access_token
            tokenActual = token
            usuarioId = respuesta.body()?.user?.id
            rolUsuario = respuesta.body()?.user?.user_metadata?.rol
            return token
        }
        return null
    }

    // Lista las solicitudes de este cliente (su id). Vacia si no hay token.
    suspend fun listarSolicitudes(): List<Solicitud> {
        val token = tokenActual ?: return emptyList()
        val id = usuarioId ?: return emptyList()
        return try {
            ServicioApi.api.listarSolicitudes("Bearer $token", "eq.$id")
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Lista todas las solicitudes PENDIENTES (para el tecnico). Vacia si falla.
    suspend fun listarPendientes(): List<Solicitud> {
        val token = tokenActual ?: return emptyList()
        return try {
            ServicioApi.api.listarPendientes("Bearer $token", "eq.PENDIENTE")
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Crea una solicitud nueva para el cliente logueado. Devuelve la creada o null.
    suspend fun crearSolicitud(descripcion: String): Solicitud? {
        val token = tokenActual ?: return null
        val id = usuarioId ?: return null
        val nueva = SolicitudNueva(
            cliente_id = id,
            descripcion = descripcion,
            estado = "PENDIENTE"
        )
        return try {
            val filas = ServicioApi.api.crearSolicitud("Bearer $token", nueva)
            filas.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // Acepta una solicitud PENDIENTE: la deja al tecnico logueado y en EN_CAMINO.
    suspend fun aceptarSolicitud(idSolicitud: String): Boolean {
        val token = tokenActual ?: return false
        val idTecnico = usuarioId ?: return false
        val cambios = mapOf(
            "estado" to "EN_CAMINO",
            "tecnico_id" to idTecnico
        )
        return try {
            val r = ServicioApi.api.aceptarSolicitud("Bearer $token", "eq.$idSolicitud", cambios)
            r.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    // Lista las solicitudes EN_CAMINO de este tecnico.
    suspend fun listarEnCamino(): List<Solicitud> {
        val token = tokenActual ?: return emptyList()
        val id = usuarioId ?: return emptyList()
        return try {
            ServicioApi.api.listarEnCamino("Bearer $token", "eq.$id", "eq.EN_CAMINO")
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Finaliza una solicitud EN_CAMINO (solo el tecnico que la acepto).
    suspend fun finalizarSolicitud(idSolicitud: String): Boolean {
        val token = tokenActual ?: return false
        val cambios = mapOf("estado" to "FINALIZADO")
        return try {
            val r = ServicioApi.api.finalizarSolicitud("Bearer $token", "eq.$idSolicitud", cambios)
            r.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    // Califica al tecnico de una solicitud FINALIZADA. Devuelve true si se guardo.
    suspend fun calificar(solicitudId: String, calificadoId: String, puntuacion: Int, comentario: String): Boolean {
        val token = tokenActual ?: return false
        val autorId = usuarioId ?: return false
        val nueva = CalificacionNueva(
            solicitud_id = solicitudId,
            autor_id = autorId,
            calificado_id = calificadoId,
            puntuacion = puntuacion,
            comentario = comentario
        )
        return try {
            val r = ServicioApi.api.crearCalificacion("Bearer $token", nueva)
            r.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    // Verifica si ya se califico esta solicitud.
    suspend fun tieneCalificacion(solicitudId: String): Boolean {
        val token = tokenActual ?: return false
        return try {
            ServicioApi.api.listarCalificaciones("Bearer $token", "eq.$solicitudId").isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    // Devuelve true si el usuario logueado es un tecnico.
    fun esTecnico(): Boolean = rolUsuario?.uppercase() == "TECNICO"

    // Limpia los datos del login (para cerrar sesion).
    fun cerrarSesion() {
        tokenActual = null
        usuarioId = null
        rolUsuario = null
    }
}