package br.com.test.teste.Repository;

import br.com.test.teste.Entity.LogConsulta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogConsultaRepository extends JpaRepository<LogConsulta, Long> {

    Optional<LogConsulta> findByCepConsultado(String cepConsultado);
}
