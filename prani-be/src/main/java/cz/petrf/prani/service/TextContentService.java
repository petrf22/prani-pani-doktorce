package cz.petrf.prani.service;

import cz.petrf.prani.db.entity.TextContent;
import cz.petrf.prani.db.repo.TextContentRepository;
import cz.petrf.prani.security.AppUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TextContentService {

  @Value("${app.validate.prani.max.len:350}")
  private int praniMaxLen;

  private final TextContentRepository textContentRepository;

  public TextContentService(TextContentRepository textContentRepository) {
    this.textContentRepository = textContentRepository;
  }

  @Transactional
  public TextContent createTextContent(String content, AppUser appUser) {
    validateContent(content);

    textContentRepository.deleteAllByUser(appUser.getDbUser());

    TextContent textContent = new TextContent();
    textContent.setUser(appUser.getDbUser());
    textContent.setContent(content);

    return textContentRepository.save(textContent);
  }

  @Transactional
  public TextContent updateTextContent(Long id, String content, AppUser appUser) {
    validateContent(content);

    TextContent textContent = textContentRepository.findByIdAndUser(id, appUser.getDbUser())
        .orElseThrow(() -> new IllegalArgumentException("Textový záznam s ID " + id + " nenalezen"));

    textContent.setContent(content);

    return textContentRepository.save(textContent);
  }

  @Transactional(readOnly = true)
  public Optional<TextContent> getTextContent(Long id, AppUser appUser) {
    return textContentRepository.findByIdAndUser(id, appUser.getDbUser());
  }

  @Transactional(readOnly = true)
  public Optional<TextContent> findByUser(AppUser appUser) {
    return textContentRepository.findByUser(appUser.getDbUser());
  }

  @Transactional
  public boolean deleteTextContent(Long id, AppUser appUser) {
    if (textContentRepository.existsByIdAndUser(id, appUser.getDbUser())) {
      textContentRepository.deleteByIdAndUser(id, appUser.getDbUser());
      return true;
    }
    return false;
  }

  private void validateContent(String content) {
    if (StringUtils.isBlank(content)) {
      throw new IllegalArgumentException("Obsah nesmí být prázdný");
    }

    if (StringUtils.length(content) > praniMaxLen) {
      throw new IllegalArgumentException("Text může být maximálně %d znaků dlouhý.".formatted(praniMaxLen));
    }
  }
}