package com.example.crud_room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Contacto::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    // Esta función permite acceder a las operaciones de los contactos.
    abstract fun contactoDao(): ContactoDao

    // Companion object permite crear funciones y variables
    // que podemos utilizar sin crear un objeto de esta clase.
    companion object {

        // Evita que se creen varias bases de datos al mismo tiempo.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Esta función permite obtener la base de datos.
        // Context nos da acceso a la información de la aplicación.
        fun getDatabase(context: Context): AppDatabase {

            // Si la base de datos ya existe, la utilizamos.
            // Si todavía no existe, creamos una nueva.
            return INSTANCE ?: synchronized(this) {

                // Creamos la base de datos usando Room.
                val instance = Room.databaseBuilder(

                    // Utilizamos el contexto general de la aplicación.
                    context.applicationContext,

                    // Indicamos cuál es la clase de nuestra base de datos.
                    AppDatabase::class.java,


                    "contactos_db"


                ).fallbackToDestructiveMigration(dropAllTables = true).build()

                // Guardamos la base de datos para volver a utilizarla.
                INSTANCE = instance

                // Devolvemos la base de datos creada.
                instance
            }
        }
    }
}