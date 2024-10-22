package br.com.test.teste.Service;

import br.com.test.teste.DTO.CepResponseDTO;
import br.com.test.teste.Entity.LogConsulta;
import br.com.test.teste.Repository.LogConsultaRepository;
import br.com.test.teste.Util.AdvancedCepValidator;
import br.com.test.teste.Util.ApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CepService {

    private final LogConsultaRepository logRepository;
    private final AdvancedCepValidator cepValidator;
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public CepService(LogConsultaRepository logRepository, AdvancedCepValidator cepValidator, ApiClient apiClient, ObjectMapper objectMapper) {
        this.logRepository = logRepository;
        this.cepValidator = cepValidator;
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    public CepResponseDTO buscaCep(String cep) {
        // Sanitizar o CEP usando AdvancedCepValidator
        String cepSanitizado = cepValidator.sanitizar(cep);

        // Validar o CEP
        if (!cepValidator.validar(cepSanitizado)) {
            // Retornar um CepResponseDTO com mensagem de erro
            return new CepResponseDTO("Erro: O CEP deve conter exatamente 8 caracteres numéricos.");
        }

        // Verificar se o CEP já foi consultado anteriormente
        Optional<LogConsulta> logExistente = logRepository.findByCepConsultado(cepSanitizado);
        if (logExistente.isPresent()) {
            try {
                // Converter a string armazenada no banco de dados (JSON) de volta para CepResponseDTO
                return objectMapper.readValue(logExistente.get().getResposta(), CepResponseDTO.class);
            } catch (JsonProcessingException e) {
                // Retornar um CepResponseDTO com a mensagem de erro
                return new CepResponseDTO("Erro ao processar os dados armazenados: " + e.getMessage());
            }
        }

        // Se o CEP não foi consultado, faz a consulta na API mockada
        CepResponseDTO resposta;
        try {
            resposta = apiClient.consultarCep(cepSanitizado);
        } catch (Exception e) {
            // Retornar um CepResponseDTO com a mensagem de erro
            return new CepResponseDTO("Erro ao consultar a API: " + e.getMessage());
        }

        // Salvar a nova consulta no banco de dados
        LogConsulta log = new LogConsulta();
        log.setCepConsultado(cepSanitizado);

        try {
            // Converter o objeto CepResponseDTO para JSON e armazenar no banco de dados
            String respostaJson = objectMapper.writeValueAsString(resposta);
            log.setResposta(respostaJson);  // Armazenar a resposta como JSON
            log.setHorarioConsulta(LocalDateTime.now());
            logRepository.save(log);
        } catch (JsonProcessingException e) {
            // Retornar um CepResponseDTO com a mensagem de erro
            return new CepResponseDTO("Erro ao salvar os dados no banco de dados: " + e.getMessage());
        }

        return resposta;
    }
}
