package ir.mabhas19.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val edition: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "project_contexts",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContextEntity(
    @PrimaryKey val projectId: String,
    val cityId: String?,
    val occupancyId: String?,
    val conditionedAreaM2: Double?,
    val floorCount: Int?
)

@Entity(
    tableName = "section_drafts",
    primaryKeys = ["projectId", "edition", "section"],
    indices = [Index("projectId")],
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DraftEntity(
    val projectId: String,
    val edition: String,
    val section: String,
    val payloadJson: String,
    val localRevision: Long
)
