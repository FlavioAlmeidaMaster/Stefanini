package br.com.test.teste.Util;

import br.com.test.teste.DTO.CepResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ApiClient {

    private final RestTemplate restTemplate;
    private final String urlMockoon;

    public ApiClient(RestTemplate restTemplate, @Value("${mockoon.base-url}") String urlMockoon) {
        this.restTemplate = restTemplate;
        this.urlMockoon = urlMockoon;
    }

    public CepResponseDTO consultarCep(String cep) {
        String url = urlMockoon + cep;
        // Chama a API mockada e mapeia a resposta diretamente para CepResponseDTO
        return restTemplate.getForObject(url, CepResponseDTO.class);
    }
}

