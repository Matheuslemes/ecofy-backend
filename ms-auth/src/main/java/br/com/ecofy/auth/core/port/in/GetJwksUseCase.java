package br.com.ecofy.auth.core.port.in;

import java.util.Map;

public interface GetJwksUseCase {

    // retorna o documento jwks para ser serializado em json
    Map<String, Object> getJwks();

}
