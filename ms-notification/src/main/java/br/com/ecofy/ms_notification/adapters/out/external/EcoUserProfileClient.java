package br.com.ecofy.ms_notification.adapters.out.external;

import br.com.ecofy.ms_notification.config.NotificationProperties;
import br.com.ecofy.ms_notification.core.domain.valueobject.UserId;
import br.com.ecofy.ms_notification.core.port.out.LoadUserContactInfoPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class EcoUserProfileClient implements LoadUserContactInfoPort {

    private final NotificationProperties props;

    public EcoUserProfileClient(NotificationProperties props) {
        this.props = props;
    }

    @Override
    public Optional<UserContactInfo> load(UserId userId) {

        // Placeholder senior: não quebra build; pronto para trocar por WebClient/Feign.
        if (!props.getClients().getUserProfile().isEnabled()) {
            log.debug("[EcoUserProfileClient] stub mode enabled=false. Returning synthetic contacts.");
            return Optional.of(new UserContactInfo(
                    "user+" + userId.value() + "@example.com",
                    "+5511999999999",
                    "device-token-" + UUID.randomUUID()
            ));
        }

        // no futuro: chamar ms-users (ou serviço externo) e mapear contatos
        log.warn("[EcoUserProfileClient] enabled=true but still stub. Returning empty.");
        return Optional.empty();
    }
}