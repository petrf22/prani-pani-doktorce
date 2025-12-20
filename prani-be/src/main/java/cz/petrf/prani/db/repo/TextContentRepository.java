package cz.petrf.prani.db.repo;

import cz.petrf.prani.db.entity.TextContent;
import cz.petrf.prani.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TextContentRepository extends JpaRepository<TextContent, Long> {
  Optional<TextContent> findByIdAndUser(Long id, User dbUser);

  boolean existsByIdAndUser(Long id, User dbUser);

  void deleteByIdAndUser(Long id, User dbUser);
}