package br.com.ecofy.auth.core.port.in;

import java.util.Map;

public interface ValidateTokenUseCase {

    // valida token e retorna claims se valido
    Map<String, Object> validate(String token);

}
