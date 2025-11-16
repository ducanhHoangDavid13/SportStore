package sd_04.datn_fstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sd_04.datn_fstore.model.Account;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByEmail(String email);

    long count();

    List<Account> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
