package br.com.test.teste.Util;

import br.com.test.teste.Repository.LogConsultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdvancedCepValidator extends CepValidator {

    private final LogConsultaRepository logRepository;

    @Autowired
    public AdvancedCepValidator(LogConsultaRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public boolean validar(String cep) {
        // Validação básica de 8 caracteres numéricos
        boolean baseValidation = super.validar(cep);

        // Verificação adicional no banco de dados
        return baseValidation && verificarCepNoBanco(cep);
    }

    public boolean verificarCepNoBanco(String cep) {
        // Verificar se o CEP existe no banco de dados
        return logRepository.findByCepConsultado(cep).isPresent();
    }
}
