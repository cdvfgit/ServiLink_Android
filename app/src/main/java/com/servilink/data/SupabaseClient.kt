package com.servilink.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

// ===== DATOS DE CONEXION A SUPABASE =====
object DatosServiLink {
    const val URL = "https://rthmtpxnvclxeoamojhb.supabase.co/"
    const val CLAVE_PUBLICABLE = "sb_publishable_GNYG_u4WuqU-SgWSJ4s_3g_iVktkMVv"
}

// ===== MODELOS DE DATOS =====
data class Perfil(
    val id: String,
    val rol: String?,
    val correo: String?
)

data class Especialidad(
    val id: String,
    val nombre: String?
)

data class Solicitud(
    val id: String,
    val cliente_id: String?,
    val tecnico_id: String?,
    val descripcion: String?,
    val estado: String?,
    val creado_en: String?
)

// Solo lo que hace falta para CREAR una solicitud (evita enviar el id,
// que es una columna GENERATED ALWAYS y rechaza cualquier valor).
data class SolicitudNueva(
    val cliente_id: String,
    val descripcion: String,
    val estado: String
)

// Calificacion: cliente califica al tecnico cuando el servicio esta FINALIZADO
data class Calificacion(
    val id: String,
    val solicitud_id: String,
    val autor_id: String?,
    val calificado_id: String?,
    val puntuacion: Int?,
    val comentario: String?,
    val creado_en: String?
)

// Solo lo necesario para CREAR una calificacion (sin id ni fecha)
data class CalificacionNueva(
    val solicitud_id: String,
    val autor_id: String,
    val calificado_id: String,
    val puntuacion: Int,
    val comentario: String
)

// ===== CLIENTE RETROFIT =====
// OkHttp agrega la clave publicable en las dos cabeceras que pide Supabase
private val clienteHttp = OkHttpClient.Builder()
    .addInterceptor { cadena ->
        val peticion = cadena.request().newBuilder()
            .header("apikey", DatosServiLink.CLAVE_PUBLICABLE)
            .header("Authorization", "Bearer " + DatosServiLink.CLAVE_PUBLICABLE)
            .header("Content-Type", "application/json")
            .build()
        cadena.proceed(peticion)
    }
    .build()

// Retrofit convierte las llamadas REST en funciones Kotlin
object ServicioApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl(DatosServiLink.URL)
        .client(clienteHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiSupabase = retrofit.create(ApiSupabase::class.java)
}

// ===== LLAMADAS REST A SUPABASE =====
// Nota: los filtros llevan "eq." delante (ej: cliente_id=eq.1111-...).
interface ApiSupabase {

    // Login: pide un token por correo + contrasena
    @POST("auth/v1/token?grant_type=password")
    suspend fun iniciarSesion(
        @Body cuerpoLogin: Map<String, String>
    ): retrofit2.Response<LoginRespuesta>

    // Listar solicitudes del cliente (filtradas por el id del logueado)
    @GET("rest/v1/solicitudes")
    suspend fun listarSolicitudes(
        @Header("Authorization") autorizacion: String,
        @Query("cliente_id") clienteId: String
    ): List<Solicitud>

    // Listar solicitudes PENDIENTES (para el tecnico)
    @GET("rest/v1/solicitudes")
    suspend fun listarPendientes(
        @Header("Authorization") autorizacion: String,
        @Query("estado") estado: String
    ): List<Solicitud>

    // Crear una solicitud nueva. Pide que devuelva la fila creada.
    @Headers("Prefer: return=representation")
    @POST("rest/v1/solicitudes")
    suspend fun crearSolicitud(
        @Header("Authorization") autorizacion: String,
        @Body nuevaSolicitud: SolicitudNueva
    ): List<Solicitud>

    // Aceptar una solicitud: cambia estado a EN_CAMINO y deja el tecnico
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/solicitudes")
    suspend fun aceptarSolicitud(
        @Header("Authorization") autorizacion: String,
        @Query("id") id: String,
        @Body cambios: Map<String, String>
    ): List<Solicitud>

    // Finalizar una solicitud EN_CAMINO: cambia estado a FINALIZADO
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/solicitudes")
    suspend fun finalizarSolicitud(
        @Header("Authorization") autorizacion: String,
        @Query("id") id: String,
        @Body cambios: Map<String, String>
    ): List<Solicitud>

    // Listar solicitudes EN_CAMINO de un tecnico (filtro por tecnico_id y estado)
    @GET("rest/v1/solicitudes")
    suspend fun listarEnCamino(
        @Header("Authorization") autorizacion: String,
        @Query("tecnico_id") tecnicoId: String,
        @Query("estado") estado: String
    ): List<Solicitud>

    // Crear una calificacion (cliente -> tecnico)
    @Headers("Prefer: return=representation")
    @POST("rest/v1/calificaciones")
    suspend fun crearCalificacion(
        @Header("Authorization") autorizacion: String,
        @Body calificacion: CalificacionNueva
    ): List<Calificacion>

    // Verificar si ya existe una calificacion para esta solicitud
    @GET("rest/v1/calificaciones")
    suspend fun listarCalificaciones(
        @Header("Authorization") autorizacion: String,
        @Query("solicitud_id") solicitudId: String
    ): List<Calificacion>
}

// Respuesta del login: token + el usuario (para saber el id y el rol)
data class LoginRespuesta(
    val access_token: String?,
    val user: UsuarioLogin?
)

data class UsuarioLogin(
    val id: String?,
    val user_metadata: RolMeta?
)

data class RolMeta(
    val rol: String?
)