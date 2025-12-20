package cz.petrf.prani.db.repo;

import cz.petrf.prani.db.entity.MagicLinkToken;
import cz.petrf.prani.db.entity.Photo;
import cz.petrf.prani.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, String> {

}