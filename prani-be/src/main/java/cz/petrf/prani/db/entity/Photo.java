package cz.petrf.prani.db.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Blob;
import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_photo_user"))
  private User user;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String contentType;

  @Lob
  @Basic(fetch = FetchType.LAZY)
  @Column(nullable = false, columnDefinition = "bytea")
  @JdbcTypeCode(SqlTypes.BINARY)
  private Blob data;

  @Column(nullable = false)
  private Long fileSize;

  @Column(nullable = false)
  private LocalDateTime uploadedAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    uploadedAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}