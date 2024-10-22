package br.com.test.teste.Service;

import br.com.test.teste.DTO.CepResponseDTO;
import br.com.test.teste.Entity.LogConsulta;
import br.com.test.teste.Repository.LogConsultaRepository;
import br.com.test.teste.Util.AdvancedCepValidator;
import br.com.test.teste.Util.ApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CepServiceTest {

    @Mock
    private LogConsultaRepository logRepository;

    @Mock
    private AdvancedCepValidator cepValidator;

    @Mock
    private ApiClient apiClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CepService cepService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBuscarCep_CepValido() throws JsonProcessingException {
        // Dados do teste
        String cep = "01001-000";
        String cepSanitizado = "01001000";
        CepResponseDTO mockResponse = new CepResponseDTO();
        mockResponse.setCep(cep);
        mockResponse.setLogradouro("Praça da Sé");

        // Mockando as chamadas de dependências
        when(cepValidator.sanitizar(cep)).thenReturn(cepSanitizado);
        when(cepValidator.validar(cepSanitizado)).thenReturn(true);
        when(logRepository.findByCepConsultado(cepSanitizado)).thenReturn(Optional.empty());
        when(apiClient.consultarCep(cepSanitizado)).thenReturn(mockResponse);

        // Executa o método de serviço
        CepResponseDTO resultado = cepService.buscaCep(cep);

        // Verificações
        assertNotNull(resultado);
        assertEquals(cep, resultado.getCep());
        assertEquals("Praça da Sé", resultado.getLogradouro());

        // Verificar se os mocks foram chamados corretamente
        verify(cepValidator).sanitizar(cep);
        verify(cepValidator).validar(cepSanitizado);
        verify(apiClient).consultarCep(cepSanitizado);
    }

    @Test
    void testBuscarCep_CepJaConsultado() throws JsonProcessingException {
        // Dados do teste
        String cep = "01001-000";
        String cepSanitizado = "01001000";
        LogConsulta logConsulta = new LogConsulta();
        logConsulta.setCepConsultado(cepSanitizado);
        logConsulta.setResposta("{\"cep\":\"01001-000\",\"logradouro\":\"Praça da Sé\"}");

        CepResponseDTO mockResponse = new CepResponseDTO();
        mockResponse.setCep(cep);
        mockResponse.setLogradouro("Praça da Sé");

        // Mockando as chamadas de dependências
        when(cepValidator.sanitizar(cep)).thenReturn(cepSanitizado);
        when(cepValidator.validar(cepSanitizado)).thenReturn(true);
        when(logRepository.findByCepConsultado(cepSanitizado)).thenReturn(Optional.of(logConsulta));
        when(objectMapper.readValue(logConsulta.getResposta(), CepResponseDTO.class)).thenReturn(mockResponse);

        // Executa o método de serviço
        CepResponseDTO resultado = cepService.buscaCep(cep);

        // Verificações
        assertNotNull(resultado);
        assertEquals(cep, resultado.getCep());
        assertEquals("Praça da Sé", resultado.getLogradouro());

        // Verificar se os mocks foram chamados corretamente
        verify(cepValidator).sanitizar(cep);
        verify(cepValidator).validar(cepSanitizado);
        verify(logRepository).findByCepConsultado(cepSanitizado);
        verify(objectMapper).readValue(logConsulta.getResposta(), CepResponseDTO.class);
    }

    @Test
    void testBuscarCep_CepInvalido() {
        // Dados do teste
        String cep = "01001-000";
        String cepSanitizado = "01001000";

        // Mockando as chamadas de dependências
        when(cepValidator.sanitizar(cep)).thenReturn(cepSanitizado);
        when(cepValidator.validar(cepSanitizado)).thenReturn(false);

        // Executa o método de serviço
        CepResponseDTO resultado = cepService.buscaCep(cep);

        // Verificações
        assertNotNull(resultado);
        assertEquals("Erro: O CEP deve conter exatamente 8 caracteres numéricos.", resultado.getMessage());

        // Verificar se os mocks foram chamados corretamente
        verify(cepValidator).sanitizar(cep);
        verify(cepValidator).validar(cepSanitizado);
        verifyNoInteractions(apiClient); // Certificar que a API externa não foi chamada
    }

    @Test
    void testSalvarLogConsultaFalha() throws JsonProcessingException {
        // Dados do teste
        String cep = "01001-000";
        String cepSanitizado = "01001000";
        CepResponseDTO mockResponse = new CepResponseDTO();
        mockResponse.setCep(cep);
        mockResponse.setLogradouro("Praça da Sé");

        // Mockando as chamadas de dependências
        when(cepValidator.sanitizar(cep)).thenReturn(cepSanitizado);
        when(cepValidator.validar(cepSanitizado)).thenReturn(true);
        when(logRepository.findByCepConsultado(cepSanitizado)).thenReturn(Optional.empty());
        when(apiClient.consultarCep(cepSanitizado)).thenReturn(mockResponse);
        doThrow(new RuntimeException("Erro ao salvar no banco")).when(logRepository).save(any(LogConsulta.class));

        // Executa o método de serviço
        CepResponseDTO resultado = cepService.buscaCep(cep);

        // Verificações
        assertNotNull(resultado);
        assertEquals("Erro ao salvar os dados no banco de dados: Erro ao salvar no banco", resultado.getMessage());

        // Verificar se os mocks foram chamados corretamente
        verify(logRepository).save(any(LogConsulta.class));
    }

    @Test
    void testDadosInvalidosNoBanco() throws JsonProcessingException {
        // Dados do teste
        String cep = "01001-000";
        String cepSanitizado = "01001000";
        LogConsulta logConsulta = new LogConsulta();
        logConsulta.setCepConsultado(cepSanitizado);
        logConsulta.setResposta("{invalid-json}");

        // Mockando as chamadas de dependências
        when(cepValidator.sanitizar(cep)).thenReturn(cepSanitizado);
        when(cepValidator.validar(cepSanitizado)).thenReturn(true);
        when(logRepository.findByCepConsultado(cepSanitizado)).thenReturn(Optional.of(logConsulta));
        when(objectMapper.readValue(logConsulta.getResposta(), CepResponseDTO.class)).thenThrow(new JsonProcessingException("Erro ao processar JSON") {});

        // Executa o método de serviço
        CepResponseDTO resultado = cepService.buscaCep(cep);

        // Verificações
        assertNotNull(resultado);
        assertEquals("Erro ao processar os dados armazenados: Erro ao processar JSON", resultado.getMessage());

        // Verificar se os mocks foram chamados corretamente
        verify(logRepository).findByCepConsultado(cepSanitizado);
        verify(objectMapper).readValue(logConsulta.getResposta(), CepResponseDTO.class);
    }

    @Test
    void testAtrasoNaApiExterna() throws JsonProcessingException {
        // Dados do teste
        String cep = "01001-000";
        String cepSanitizado = "01001000";
        CepResponseDTO mockResponse = new CepResponseDTO();
        mockResponse.setCep(cep);
        mockResponse.setLogradouro("Praça da Sé");

        // Mockando as chamadas de dependências
        when(cepValidator.sanitizar(cep)).thenReturn(cepSanitizado);
        when(cepValidator.validar(cepSanitizado)).thenReturn(true);
        when(logRepository.findByCepConsultado(cepSanitizado)).thenReturn(Optional.empty());

        // Simular atraso
        when(apiClient.consultarCep(cepSanitizado)).thenAnswer(invocation -> {
            Thread.sleep(2000); // Simular atraso de 2 segundos
            return mockResponse;
        });

        // Executa o método de serviço
        CepResponseDTO resultado = cepService.buscaCep(cep);

        // Verificações
        assertNotNull(resultado);
        assertEquals(cep, resultado.getCep());
        assertEquals("Praça da Sé", resultado.getLogradouro());
    }
}
