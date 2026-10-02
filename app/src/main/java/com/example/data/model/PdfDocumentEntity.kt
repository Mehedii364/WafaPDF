package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_documents")
data class PdfDocumentEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val filePath: String,
  val fileSize: Long,
  val pageCount: Int,
  val lastOpenedTimestamp: Long = System.currentTimeMillis(),
  val lastPageRead: Int = 1,
  val isFavorite: Boolean = false,
  val folder: String = "Documents",
  val tags: String = "",
  val isVault: Boolean = false,
  val isTrash: Boolean = false,
  val thumbnailPath: String? = null,
  val extractedText: String? = null,
  val createdTimestamp: Long = System.currentTimeMillis()
)
