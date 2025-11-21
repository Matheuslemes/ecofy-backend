package br.com.ecofy.auth.core.port.out;

import br.com.ecofy.auth.core.domain.ClientApplication;

public interface SaveClientApplicationPort {

    ClientApplication save(ClientApplication clientApplication);

}
