package com.servilink.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.servilink.data.Repositorio
import com.servilink.data.Solicitud
import kotlinx.coroutines.launch

// ===== Navegacion de la app: login, inicio (cliente) y tecnico =====
@Composable
fun AppServiLink() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "login") {
        composable("login") { PantallaLogin(nav) }
        composable("inicio") { PantallaInicio(nav) }
        composable("tecnico") { PantallaTecnico(nav) }
        composable("calificar/{solicitudId}/{tecnicoId}") { entrada ->
            PantallaCalificar(nav, entrada.arguments?.getString("solicitudId") ?: "", entrada.arguments?.getString("tecnicoId") ?: "")
        }
    }
}

// ===== Pantalla de Login =====
@Composable
fun PantallaLogin(nav: NavHostController) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ServiLink", style = MaterialTheme.typography.headlineLarge)

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                scope.launch {
                    cargando = true
                    error = null
                    val token = Repositorio.login(correo, contrasena)
                    cargando = false
                    if (token != null) {
                        if (Repositorio.esTecnico()) {
                            nav.navigate("tecnico") { popUpTo("login") { inclusive = true } }
                        } else {
                            nav.navigate("inicio") { popUpTo("login") { inclusive = true } }
                        }
                    } else {
                        error = "Correo o contraseña incorrectos"
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Entrando..." else "Entrar")
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        }
    }
}

// ===== Pantalla de Inicio (cliente): publicar y ver sus solicitudes =====
@Composable
fun PantallaInicio(nav: NavHostController) {
    var solicitudes by remember { mutableStateOf(emptyList<Solicitud>()) }
    var descripcion by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val recargar: () -> Unit = {
        scope.launch { solicitudes = Repositorio.listarSolicitudes() }
    }
    LaunchedEffect(Unit) { recargar() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Inicio", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                Repositorio.cerrarSesion()
                nav.navigate("login") { popUpTo(0) { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Publicar una nueva solicitud
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Describe tu problema") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    mensaje = null
                    if (descripcion.isBlank()) {
                        mensaje = "Escribe una descripción primero."
                    } else {
                        val creada = Repositorio.crearSolicitud(descripcion.trim())
                        if (creada != null) {
                            descripcion = ""
                            mensaje = "Solicitud publicada."
                            recargar()
                        } else {
                            mensaje = "No se pudo publicar. Intenta de nuevo."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Publicar solicitud")
        }

        if (mensaje != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensaje!!, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Mis solicitudes", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(4.dp))

        if (solicitudes.isEmpty()) {
            Text(text = "Aún no tienes solicitudes.")
        } else {
            LazyColumn {
                items(solicitudes) { s ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = s.descripcion ?: "Sin descripcion")
                            Text(
                                text = "Estado: " + (s.estado ?: "-"),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (s.estado == "FINALIZADO" && s.tecnico_id != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                var yaCalif by remember { mutableStateOf(false) }
                                LaunchedEffect(s.id) {
                                    yaCalif = Repositorio.tieneCalificacion(s.id)
                                }
                                if (yaCalif) {
                                    Text(
                                        text = "Ya calificado ★",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            nav.navigate("calificar/" + s.id + "/" + s.tecnico_id)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Calificar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===== Pantalla del Tecnico: pendientes, en curso (finalizar) =====
@Composable
fun PantallaTecnico(nav: NavHostController) {
    var pendientes by remember { mutableStateOf(emptyList<Solicitud>()) }
    var enCamino by remember { mutableStateOf(emptyList<Solicitud>()) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val recargar: () -> Unit = {
        scope.launch {
            pendientes = Repositorio.listarPendientes()
            enCamino = Repositorio.listarEnCamino()
        }
    }
    LaunchedEffect(Unit) { recargar() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Panel del Técnico", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                Repositorio.cerrarSesion()
                nav.navigate("login") { popUpTo(0) { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }

        if (mensaje != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensaje!!, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- SECCION: Servicios en curso ---
        Text(text = "Mis servicios en curso", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        if (enCamino.isEmpty()) {
            Text(text = "No tienes servicios en curso.")
        } else {
            LazyColumn {
                items(enCamino) { s ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = s.descripcion ?: "Sin descripcion")
                            Text(
                                text = "Estado: " + (s.estado ?: "-"),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        mensaje = null
                                        val ok = Repositorio.finalizarSolicitud(s.id)
                                        if (ok) {
                                            mensaje = "Servicio finalizado."
                                            recargar()
                                        } else {
                                            mensaje = "No se pudo finalizar."
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Finalizar")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECCION: Pendientes ---
        Text(text = "Solicitudes pendientes", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        if (pendientes.isEmpty()) {
            Text(text = "No hay solicitudes pendientes.")
        } else {
            LazyColumn {
                items(pendientes) { s ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = s.descripcion ?: "Sin descripcion")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        mensaje = null
                                        val ok = Repositorio.aceptarSolicitud(s.id)
                                        if (ok) {
                                            mensaje = "Solicitud aceptada."
                                            recargar()
                                        } else {
                                            mensaje = "No se pudo aceptar."
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Aceptar")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===== Pantalla de Calificar (cliente califica al tecnico) =====
@Composable
fun PantallaCalificar(nav: NavHostController, solicitudId: String, tecnicoId: String) {
    var puntuacion by remember { mutableStateOf(5) }
    var comentario by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var yaCalificado by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        yaCalificado = Repositorio.tieneCalificacion(solicitudId)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Calificar servicio", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        if (yaCalificado) {
            Text(text = "Ya calificaste este servicio.")
        } else {
            Text(text = "Puntuación: $puntuacion", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            // Barra simple de 1 a 5 estrellas con Slider
            Slider(
                value = puntuacion.toFloat(),
                onValueChange = { puntuacion = it.toInt() },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentario (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        cargando = true
                        mensaje = null
                        val ok = Repositorio.calificar(solicitudId, tecnicoId, puntuacion, comentario)
                        cargando = false
                        if (ok) {
                            mensaje = "Gracias por calificar."
                            yaCalificado = true
                        } else {
                            mensaje = "No se pudo guardar la calificación."
                        }
                    }
                },
                enabled = !cargando && !yaCalificado,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (cargando) "Guardando..." else "Enviar calificación")
            }
        }

        if (mensaje != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = mensaje!!, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}