package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val iconName: String = "folder",
  val colorHex: String = "#D32F2F",
  val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val documentId: Long,
  val pageNumber: Int,
  val title: String,
  val snippet: String? = null,
  val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "annotations")
data class AnnotationEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val documentId: Long,
  val pageNumber: Int,
  val type: String, // HIGHLIGHT, UNDERLINE, STRIKETHROUGH, PEN, RECTANGLE, CIRCLE, TEXT_NOTE, SIGNATURE
  val colorHex: String,
  val strokeWidth: Float = 3f,
  val dataJson: String, // serialized points / text / rect bounds
  val textNote: String? = null,
  val timestamp: Long = System.currentTimeMillis()
)
