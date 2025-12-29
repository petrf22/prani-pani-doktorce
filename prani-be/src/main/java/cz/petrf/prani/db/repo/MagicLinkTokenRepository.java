package cz.petrf.prani.db.repo;

import cz.petrf.prani.db.entity.MagicLinkToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, String> {

}