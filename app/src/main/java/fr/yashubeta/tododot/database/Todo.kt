package fr.yashubeta.tododot.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val todoId: Int,
    var position: Int = 0,
    var title: String,
    var note: String?,
    var isChecked: Boolean = false,
    var parentId: Int? = null
)