package cz.petrf.prani.service;

import cz.petrf.prani.db.entity.Photo;
import cz.petrf.prani.db.entity.User;
import cz.petrf.prani.db.repo.PhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class PhotoService {

  private final PhotoRepository photoRepository;

  public PhotoService(PhotoRepository photoRepository) {
    this.photoRepository = photoRepository;
  }

  private Photo createPhotoFromMultipartFile(MultipartFile file, User dbUser) {
    Photo photo = new Photo();

    photo.setUser(dbUser);

    return updatePhotoFromMultipartFile(file, photo);
  }

  private Photo updatePhotoFromMultipartFile(MultipartFile file, Photo photo) {
    try {
      photo.setFileName(file.getOriginalFilename());
      photo.setContentType(file.getContentType());
      photo.setData(new SerialBlob(file.getBytes()));
      photo.setFileSize(file.getSize());

      return photo;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Transactional
  public Photo createOrUpdatePhoto(MultipartFile file, User dbUser) {
    validateFile(file);

    Photo photo = photoRepository.findByUser(dbUser)
        .map(photoDb -> updatePhotoFromMultipartFile(file, photoDb))
        .orElseGet(() -> createPhotoFromMultipartFile(file, dbUser));

    return photoRepository.save(photo);
  }

  @Transactional(readOnly = true)
  public Optional<Photo> getPhoto(User dbUser) {
    return photoRepository.findByUser(dbUser);
  }

  @Transactional
  public boolean deletePhoto(User dbUser) {
    if (photoRepository.existsByUser(dbUser)) {
      photoRepository.deleteAllByUser(dbUser);
      return true;
    }
    return false;
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Soubor nesmí být prázdný");
    }

    String contentType = file.getContentType();
    if (contentType==null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException("Soubor musí být obrázek");
    }

    // Limit 10MB
    if (file.getSize() > 10 * 1024 * 1024) {
      throw new IllegalArgumentException("Soubor je příliš velký (max 10MB)");
    }
  }
}