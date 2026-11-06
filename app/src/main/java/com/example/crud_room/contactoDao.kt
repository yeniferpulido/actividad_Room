package com.example.crud_room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    @Insert
    suspend fun insertar(contacto: Contacto)
    // CREATE: Inserta un nuevo contacto en la base de datos.

    @Query("SELECT * FROM contactos ORDER BY nombre ASC, apellido ASC")
    fun obtenerTodos(): Flow<List<Contacto>>
    // READ: Obtiene todos los contactos y los organiza alfabéticamente por nombre y apellido.
    // Flow permite actualizar la lista automáticamente cuando hay cambios.

    @Query("SELECT * FROM contactos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): Contacto?
    // Busca un contacto por su ID. Si no existe, devuelve null.

    @Query("SELECT COUNT(*) FROM contactos WHERE LOWER(correo) = LOWER(:correo) AND id != :id")
    suspend fun contarCorreoEnOtros(correo: String, id: Int): Int
    // Cuenta cuántos contactos tienen el mismo correo, ignorando mayúsculas y minúsculas.
    // Excluye el contacto actual para evitar conflictos al actualizar.

    @Update
    suspend fun actualizar(contacto: Contacto)
    // UPDATE: Actualiza la información de un contacto existente.

    @Delete
    suspend fun eliminar(contacto: Contacto)
    // DELETE: Elimina un contacto de la base de datos.
}