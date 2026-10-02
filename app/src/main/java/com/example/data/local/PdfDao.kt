package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AnnotationEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.FolderEntity
import com.example.data.model.PdfDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDao {

  @Query("SELECT * FROM pdf_documents WHERE isVault = 0 AND isTrash = 0 ORDER BY lastOpenedTimestamp DESC")
  fun getRecentDocuments(): Flow<List<PdfDocumentEntity>>

  @Query("SELECT * FROM pdf_documents WHERE isVault = 0 AND isTrash = 0 ORDER BY title ASC")
  fun getAllDocuments(): Flow<List<PdfDocumentEntity>>

  @Query("SELECT * FROM pdf_documents WHERE isFavorite = 1 AND isVault = 0 AND isTrash = 0 ORDER BY lastOpenedTimestamp DESC")
  fun getFavoriteDocuments(): Flow<List<PdfDocumentEntity>>

  @Query("SELECT * FROM pdf_documents WHERE folder = :folderName AND isVault = 0 AND isTrash = 0 ORDER BY lastOpenedTimestamp DESC")
  fun getDocumentsByFolder(folderName: String): Flow<List<PdfDocumentEntity>>

  @Query("SELECT * FROM pdf_documents WHERE isVault = 1 AND isTrash = 0 ORDER BY lastOpenedTimestamp DESC")
  fun getVaultDocuments(): Flow<List<PdfDocumentEntity>>

  @Query("SELECT * FROM pdf_documents WHERE isTrash = 1 ORDER BY lastOpenedTimestamp DESC")
  fun getTrashDocuments(): Flow<List<PdfDocumentEntity>>

  @Query("SELECT * FROM pdf_documents WHERE id = :id LIMIT 1")
  fun getDocumentById(id: Long): Flow<PdfDocumentEntity?>

  @Query("SELECT * FROM pdf_documents WHERE id = :id LIMIT 1")
  suspend fun getDocumentByIdDirect(id: Long): PdfDocumentEntity?

  @Query("SELECT * FROM pdf_documents WHERE (title LIKE '%' || :query || '%' OR extractedText LIKE '%' || :query || '%') AND isVault = 0 AND isTrash = 0")
  fun searchDocuments(query: String): Flow<List<PdfDocumentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocument(document: PdfDocumentEntity): Long

  @Update
  suspend fun updateDocument(document: PdfDocumentEntity)

  @Delete
  suspend fun deleteDocument(document: PdfDocumentEntity)

  @Query("DELETE FROM pdf_documents WHERE id = :id")
  suspend fun deleteDocumentById(id: Long)

  // Folders
  @Query("SELECT * FROM folders ORDER BY name ASC")
  fun getAllFolders(): Flow<List<FolderEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFolder(folder: FolderEntity): Long

  @Query("DELETE FROM folders WHERE id = :id")
  suspend fun deleteFolder(id: Long)

  // Bookmarks
  @Query("SELECT * FROM bookmarks WHERE documentId = :documentId ORDER BY pageNumber ASC")
  fun getBookmarksForDocument(documentId: Long): Flow<List<BookmarkEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookmark(bookmark: BookmarkEntity): Long

  @Query("DELETE FROM bookmarks WHERE id = :id")
  suspend fun deleteBookmark(id: Long)

  // Annotations
  @Query("SELECT * FROM annotations WHERE documentId = :documentId AND pageNumber = :pageNumber")
  fun getAnnotationsForPage(documentId: Long, pageNumber: Int): Flow<List<AnnotationEntity>>

  @Query("SELECT * FROM annotations WHERE documentId = :documentId")
  fun getAllAnnotationsForDoc(documentId: Long): Flow<List<AnnotationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAnnotation(annotation: AnnotationEntity): Long

  @Query("DELETE FROM annotations WHERE id = :id")
  suspend fun deleteAnnotation(id: Long)

  @Query("DELETE FROM annotations WHERE documentId = :documentId")
  suspend fun clearAnnotationsForDocument(documentId: Long)
}
