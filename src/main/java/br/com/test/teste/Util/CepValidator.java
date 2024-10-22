package br.com.test.teste.Util;

import org.springframework.stereotype.Component;

@Component
public class CepValidator {

    public String sanitizar(String cep) {
        return cep.replaceAll("[^0-9]", "");
    }

    public boolean validar(String cep) {
        return cep.length() == 8;
    }
}