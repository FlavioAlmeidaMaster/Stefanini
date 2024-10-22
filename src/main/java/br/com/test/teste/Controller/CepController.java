package br.com.test.teste.Controller;

import br.com.test.teste.DTO.CepRequestDTO;
import br.com.test.teste.DTO.CepResponseDTO;
import br.com.test.teste.Service.CepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CepController {

    private final CepService cepService;

    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @Operation(summary = "Busca informações de um CEP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "CEP inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    @PostMapping("/buscar-cep")
    public ResponseEntity<?> buscarCep(@RequestBody CepRequestDTO request) {
        // Pré-análise do CEP (validar e sanitizar)
        String cepSanitizado = request.getCep().replaceAll("[^0-9]", "");

        // Verificar se o CEP tem exatamente 8 dígitos
        if (cepSanitizado.length() != 8) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Erro: CEP inválido. O CEP deve conter exatamente 8 caracteres numéricos.");
        }

        // Delegar a chamada ao serviço e obter o objeto de resposta
        CepResponseDTO resposta = cepService.buscaCep(cepSanitizado);
        return ResponseEntity.ok(resposta);  // Retorna o payload completo
    }
}
