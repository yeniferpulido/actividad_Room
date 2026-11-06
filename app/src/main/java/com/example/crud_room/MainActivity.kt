package com.example.crud_room

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.crud_room.ui.theme.CRUD_RoomTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Esta función se ejecuta cuando abrimos la aplicación.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite utilizar todo el espacio de la pantalla.
        enableEdgeToEdge()

        // Conectamos la aplicación con la base de datos.
        val dao = AppDatabase.getDatabase(this).contactoDao()

        // Mostramos la pantalla de contactos.
        setContent {
            CRUD_RoomTheme {
                PantallaContactos(dao)
            }
        }
    }
}

// VALIDAR NOMBRE
fun validarNombre(nombre: String): String? {

    // Quitamos los espacios sobrantes.
    val trimmed = nombre.trim()

    // Permitimos letras, tildes, ñ y espacios.
    val regex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")

    return when {
        trimmed.isEmpty() -> "El nombre es obligatorio"

        trimmed.length !in 2..50 ->
            "El nombre debe tener entre 2 y 50 caracteres"

        !regex.matches(trimmed) ->
            "Solo se permiten letras y espacios"

        else -> null
    }
}

// VALIDAR APELLIDO
fun validarApellido(apellido: String): String? {

    val trimmed = apellido.trim()

    // Permitimos solamente letras y espacios.
    val regex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")

    return when {
        trimmed.isEmpty() -> "El apellido es obligatorio"

        trimmed.length !in 2..50 ->
            "El apellido debe tener entre 2 y 50 caracteres"

        !regex.matches(trimmed) ->
            "Solo se permiten letras y espacios"

        else -> null
    }
}

// VALIDAR TELÉFONO
fun validarTelefono(telefono: String): String? {

    val trimmed = telefono.trim()

    // Comprobamos que solamente tenga números.
    val regex = Regex("^[0-9]+$")

    return when {
        trimmed.isEmpty() -> "El teléfono es obligatorio"

        !regex.matches(trimmed) ->
            "Solo se permiten números"

        trimmed.length !in 7..15 ->
            "El teléfono debe tener entre 7 y 15 dígitos"

        else -> null
    }
}

// VALIDAR CORREO
fun validarFormatoCorreo(correo: String): String? {

    val trimmed = correo.trim()

    return when {
        trimmed.isEmpty() -> "El correo es obligatorio"

        // Comprobamos que tenga un formato de correo válido.
        !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() ->
            "Formato de correo inválido (ej: usuario@correo.com)"

        else -> null
    }
}

// ESTA FUNCIÓN CONTIENE TODA LA PANTALLA DEL CRUD.
@Composable
fun PantallaContactos(dao: ContactoDao) {

    // Permite ejecutar operaciones de la base de datos.
    val scope = rememberCoroutineScope()

    // Permite mostrar mensajes en la pantalla.
    val snackbarHostState = remember { SnackbarHostState() }

    // Obtenemos todos los contactos guardados.
    val contactos by dao.obtenerTodos().collectAsState(initial = emptyList())

    // Variables donde guardamos lo que escribe el usuario.
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    // Variables donde guardamos los mensajes de error.
    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorApellido by remember { mutableStateOf<String?>(null) }
    var errorTelefono by remember { mutableStateOf<String?>(null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }

    // Guardamos el contacto que estamos editando o eliminando.
    var contactoEditando by remember { mutableStateOf<Contacto?>(null) }
    var contactoAEliminar by remember { mutableStateOf<Contacto?>(null) }

    // Guarda el texto que escribimos en el buscador.
    var busqueda by remember { mutableStateOf("") }

    // Esta función deja todos los campos vacíos.
    fun limpiarFormulario() {
        nombre = ""
        apellido = ""
        telefono = ""
        correo = ""

        errorNombre = null
        errorApellido = null
        errorTelefono = null
        errorCorreo = null

        contactoEditando = null
    }

    // Filtramos los contactos según lo que escribimos en el buscador.
    val contactosFiltrados = remember(contactos, busqueda) {

        if (busqueda.isBlank()) {
            // Si el buscador está vacío, mostramos todos.
            contactos
        } else {
            // Buscamos por nombre o apellido.
            val query = busqueda.trim()

            contactos.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.apellido.contains(query, ignoreCase = true)
            }
        }
    }

    // Scaffold organiza los elementos principales de la pantalla.
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        // LazyColumn permite mostrar una lista desplazable.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            item {
                Text(
                    text = "Gestión de Contactos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // Cambia el texto dependiendo de si estamos editando.
                Text(
                    text = if (contactoEditando == null)
                        "Registrar nuevo contacto"
                    else
                        "Modificando contacto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // CAMPO PARA ESCRIBIR EL NOMBRE
            item {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it

                        // Quitamos el error cuando el usuario escribe.
                        if (errorNombre != null) errorNombre = null
                    },
                    label = { Text("Nombre *") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    isError = errorNombre != null,
                    supportingText = {
                        errorNombre?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // CAMPO PARA ESCRIBIR EL APELLIDO
            item {
                OutlinedTextField(
                    value = apellido,
                    onValueChange = {
                        apellido = it
                        if (errorApellido != null) errorApellido = null
                    },
                    label = { Text("Apellido *") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    isError = errorApellido != null,
                    supportingText = {
                        errorApellido?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // CAMPO PARA ESCRIBIR EL TELÉFONO
            item {
                OutlinedTextField(
                    value = telefono,
                    onValueChange = {
                        telefono = it
                        if (errorTelefono != null) errorTelefono = null
                    },
                    label = { Text("Teléfono *") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    isError = errorTelefono != null,
                    supportingText = {
                        errorTelefono?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // CAMPO PARA ESCRIBIR EL CORREO
            item {
                OutlinedTextField(
                    value = correo,
                    onValueChange = {
                        correo = it
                        if (errorCorreo != null) errorCorreo = null
                    },
                    label = { Text("Correo electrónico *") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    isError = errorCorreo != null,
                    supportingText = {
                        errorCorreo?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // BOTONES PARA GUARDAR O CANCELAR
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // El botón cancelar aparece cuando estamos editando.
                    if (contactoEditando != null) {
                        OutlinedButton(
                            onClick = { limpiarFormulario() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                    }

                    // BOTÓN PARA AGREGAR O ACTUALIZAR
                    Button(
                        onClick = {

                            // Revisamos que todos los campos estén correctos.
                            val errNom = validarNombre(nombre)
                            val errApe = validarApellido(apellido)
                            val errTel = validarTelefono(telefono)
                            val errCor = validarFormatoCorreo(correo)

                            // Guardamos los errores para mostrarlos.
                            errorNombre = errNom
                            errorApellido = errApe
                            errorTelefono = errTel
                            errorCorreo = errCor

                            // Si no hay errores, continuamos.
                            if (errNom == null && errApe == null &&
                                errTel == null && errCor == null
                            ) {

                                // Ejecutamos la operación sin bloquear la pantalla.
                                scope.launch {

                                    // Obtenemos el ID del contacto.
                                    // Si es nuevo, el ID será 0.
                                    val idActual = contactoEditando?.id ?: 0

                                    // Quitamos espacios y convertimos el correo a minúsculas.
                                    val correoNormalizado = correo.trim().lowercase()

                                    // Revisamos si otro contacto ya tiene ese correo.
                                    val existeRepetido =
                                        dao.contarCorreoEnOtros(
                                            correoNormalizado,
                                            idActual
                                        ) > 0

                                    if (existeRepetido) {
                                        errorCorreo =
                                            "El correo ya está registrado por otro contacto"

                                        snackbarHostState.showSnackbar(
                                            "Error: Correo ya registrado"
                                        )

                                        // Detenemos el proceso si el correo está repetido.
                                        return@launch
                                    }

                                    // SI ES UN CONTACTO NUEVO, LO REGISTRAMOS.
                                    if (contactoEditando == null) {

                                        val nuevo = Contacto(
                                            nombre = nombre.trim(),
                                            apellido = apellido.trim(),
                                            telefono = telefono.trim(),
                                            correo = correo.trim()
                                        )

                                        // Guardamos el contacto.
                                        dao.insertar(nuevo)

                                        snackbarHostState.showSnackbar(
                                            "Contacto registrado exitosamente"
                                        )

                                    } else {

                                        // SI ESTAMOS EDITANDO, BUSCAMOS EL CONTACTO.
                                        val contactoExistente =
                                            dao.obtenerPorId(contactoEditando!!.id)

                                        // Comprobamos que todavía exista.
                                        if (contactoExistente == null) {
                                            snackbarHostState.showSnackbar(
                                                "Error: El contacto no existe o fue eliminado"
                                            )

                                            limpiarFormulario()
                                            return@launch
                                        }

                                        // Actualizamos los datos del contacto.
                                        dao.actualizar(
                                            contactoExistente.copy(
                                                nombre = nombre.trim(),
                                                apellido = apellido.trim(),
                                                telefono = telefono.trim(),
                                                correo = correo.trim()
                                            )
                                        )

                                        snackbarHostState.showSnackbar(
                                            "Contacto actualizado exitosamente"
                                        )
                                    }

                                    // Dejamos el formulario vacío al terminar.
                                    limpiarFormulario()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        // El texto cambia dependiendo de la operación.
                        Text(
                            if (contactoEditando == null)
                                "Agregar contacto"
                            else
                                "Guardar cambios"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }

            // SECCIÓN DE BÚSQUEDA
            item {
                Text(
                    text = "Lista de Contactos (${contactos.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = busqueda,

                    // Guarda lo que escribimos en el buscador.
                    onValueChange = { busqueda = it },

                    label = { Text("Buscar por nombre o apellido") },

                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },

                    // Muestra una X para borrar la búsqueda.
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Limpiar"
                                )
                            }
                        }
                    },

                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // MOSTRAR MENSAJE SI NO HAY CONTACTOS
            if (contactos.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "No hay contactos registrados todavía.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    }
                }

                // MOSTRAR MENSAJE SI NO SE ENCUENTRAN RESULTADOS
            } else if (contactosFiltrados.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "No se encontraron contactos que coincidan con \"$busqueda\".",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    }
                }

            } else {

                // MOSTRAR LA LISTA DE CONTACTOS
                items(contactosFiltrados, key = { it.id }) { contacto ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // Mostramos los datos del contacto.
                            Column(modifier = Modifier.weight(1f)) {

                                Text(
                                    text = "${contacto.nombre} ${contacto.apellido}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Tel: ${contacto.telefono}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "Correo: ${contacto.correo}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // BOTÓN PARA EDITAR
                            IconButton(
                                onClick = {

                                    // Guardamos cuál contacto vamos a modificar.
                                    contactoEditando = contacto

                                    // Colocamos sus datos en los campos.
                                    nombre = contacto.nombre
                                    apellido = contacto.apellido
                                    telefono = contacto.telefono
                                    correo = contacto.correo

                                    // Limpiamos los mensajes de error.
                                    errorNombre = null
                                    errorApellido = null
                                    errorTelefono = null
                                    errorCorreo = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // BOTÓN PARA ELIMINAR
                            IconButton(
                                onClick = {

                                    // Guardamos el contacto que queremos eliminar.
                                    contactoAEliminar = contacto
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // VENTANA PARA CONFIRMAR LA ELIMINACIÓN
    if (contactoAEliminar != null) {

        // Obtenemos el contacto que queremos eliminar.
        val contacto = contactoAEliminar!!

        AlertDialog(

            // Si cerramos la ventana, no eliminamos el contacto.
            onDismissRequest = {
                contactoAEliminar = null
            },

            title = {
                Text("Confirmar eliminación")
            },

            // Preguntamos si realmente queremos eliminar el contacto.
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar a ${contacto.nombre} ${contacto.apellido}?"
                )
            },

            // BOTÓN PARA CONFIRMAR LA ELIMINACIÓN
            confirmButton = {
                Button(
                    onClick = {

                        // Cerramos la ventana.
                        contactoAEliminar = null

                        scope.launch {

                            // Comprobamos que el contacto todavía exista.
                            val existe = dao.obtenerPorId(contacto.id)

                            if (existe != null) {

                                // Eliminamos el contacto de la base de datos.
                                dao.eliminar(contacto)

                                // Si estábamos editando ese contacto,
                                // limpiamos también el formulario.
                                if (contactoEditando?.id == contacto.id) {
                                    limpiarFormulario()
                                }

                                snackbarHostState.showSnackbar(
                                    "Contacto eliminado exitosamente"
                                )

                            } else {
                                snackbarHostState.showSnackbar(
                                    "Error: El contacto ya no existe"
                                )
                            }
                        }
                    },

                    // El botón aparece de color rojo.
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },

            // BOTÓN PARA CANCELAR
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        contactoAEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
