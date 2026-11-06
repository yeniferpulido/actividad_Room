package com.example.crud_room

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity indica que esta clase será una tabla en la base de datos.
@Entity(tableName = "contactos")

data class Contacto(

    // autoGenerate = true permite que se genere automáticamente.
    // El valor inicial es 0 porque todavía no se ha guardado.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Guarda el nombre del contacto.
    val nombre: String,

    // Guarda el apellido del contacto.
    val apellido: String,

    // Guarda el número de teléfono del contacto.
    val telefono: String,

    // Guarda el correo electrónico del contacto.
    val correo: String
)