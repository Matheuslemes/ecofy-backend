package br.com.ecofy.auth.adapters.out.messaging;

import br.com.ecofy.auth.core.domain.event.PasswordResetRequestedEvent;
import br.com.ecofy.auth.core.domain.event.UserAuthenticatedEvent;
import br.com.ecofy.auth.core.domain.event.UserEmailConfirmedEvent;
import br.com.ecofy.auth.core.domain.event.UserRegisteredEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthEventsKafkaAdapterTest {

    @Test
    @DisplayName("constructor: kafkaTemplate null -> NPE com mensagem")
    void constructor_kafkaTemplateNull_throwsNpe() {
        assertThatThrownBy(() -> new AuthEventsKafkaAdapter(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("kafkaTemplate must not be null");
    }

    @Test
    @DisplayName("publish(UserRegisteredEvent): event null -> NPE")
    void publish_userRegistered_null_throwsNpe() {
        AuthEventsKafkaAdapter a = new AuthEventsKafkaAdapter(mock(KafkaTemplate.class));
        assertThatThrownBy(() -> a.publish((UserRegisteredEvent) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("event must not be null");
    }

    @Test
    @DisplayName("publish(UserEmailConfirmedEvent): event null -> NPE")
    void publish_userEmailConfirmed_null_throwsNpe() {
        AuthEventsKafkaAdapter a = new AuthEventsKafkaAdapter(mock(KafkaTemplate.class));
        assertThatThrownBy(() -> a.publish((UserEmailConfirmedEvent) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("event must not be null");
    }

    @Test
    @DisplayName("publish(UserAuthenticatedEvent): event null -> NPE")
    void publish_userAuthenticated_null_throwsNpe() {
        AuthEventsKafkaAdapter a = new AuthEventsKafkaAdapter(mock(KafkaTemplate.class));
        assertThatThrownBy(() -> a.publish((UserAuthenticatedEvent) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("event must not be null");
    }

    @Test
    @DisplayName("publish(PasswordResetRequestedEvent): event null -> NPE")
    void publish_passwordResetRequested_null_throwsNpe() {
        AuthEventsKafkaAdapter a = new AuthEventsKafkaAdapter(mock(KafkaTemplate.class));
        assertThatThrownBy(() -> a.publish((PasswordResetRequestedEvent) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("event must not be null");
    }

    @Test
    @DisplayName("publish: envia para tópicos corretos com key = userId e executa callback success")
    void publish_allEvents_successPath() {
        KafkaTemplate<String, Object> kt = mock(KafkaTemplate.class);
        AuthEventsKafkaAdapter a = new AuthEventsKafkaAdapter(kt);

        UUID id = UUID.fromString("11111111-2222-3333-4444-555555555555");
        String key = id.toString();

        UserRegisteredEvent e1 = mock(UserRegisteredEvent.class, RETURNS_DEEP_STUBS);
        when(e1.user().id().value()).thenReturn(id);

        UserEmailConfirmedEvent e2 = mock(UserEmailConfirmedEvent.class, RETURNS_DEEP_STUBS);
        when(e2.user().id().value()).thenReturn(id);

        UserAuthenticatedEvent e3 = mock(UserAuthenticatedEvent.class, RETURNS_DEEP_STUBS);
        when(e3.user().id().value()).thenReturn(id);

        PasswordResetRequestedEvent e4 = mock(PasswordResetRequestedEvent.class, RETURNS_DEEP_STUBS);
        when(e4.user().id().value()).thenReturn(id);

        CompletableFuture<SendResult<String, Object>> f1 = CompletableFuture.completedFuture(sendResult("auth.user.registered", 0, 10L));
        CompletableFuture<SendResult<String, Object>> f2 = CompletableFuture.completedFuture(sendResult("auth.user.email-confirmed", 1, 11L));
        CompletableFuture<SendResult<String, Object>> f3 = CompletableFuture.completedFuture(sendResult("auth.user.authenticated", 2, 12L));
        CompletableFuture<SendResult<String, Object>> f4 = CompletableFuture.completedFuture(sendResult("auth.user.password-reset-requested", 3, 13L));

        when(kt.send("auth.user.registered", key, e1)).thenReturn(f1);
        when(kt.send("auth.user.email-confirmed", key, e2)).thenReturn(f2);
        when(kt.send("auth.user.authenticated", key, e3)).thenReturn(f3);
        when(kt.send("auth.user.password-reset-requested", key, e4)).thenReturn(f4);

        a.publish(e1);
        a.publish(e2);
        a.publish(e3);
        a.publish(e4);

        verify(kt).send("auth.user.registered", key, e1);
        verify(kt).send("auth.user.email-confirmed", key, e2);
        verify(kt).send("auth.user.authenticated", key, e3);
        verify(kt).send("auth.user.password-reset-requested", key, e4);
        verifyNoMoreInteractions(kt);
    }

    @Test
    @DisplayName("publish: callback com exception não lança e ainda chama send")
    void publish_whenFutureCompletesExceptionally_doesNotThrow() {
        KafkaTemplate<String, Object> kt = mock(KafkaTemplate.class);
        AuthEventsKafkaAdapter a = new AuthEventsKafkaAdapter(kt);

        UUID id = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        String key = id.toString();

        UserRegisteredEvent e = mock(UserRegisteredEvent.class, RETURNS_DEEP_STUBS);
        when(e.user().id().value()).thenReturn(id);

        CompletableFuture<SendResult<String, Object>> f = new CompletableFuture<>();
        when(kt.send("auth.user.registered", key, e)).thenReturn(f);

        assertThatCode(() -> a.publish(e)).doesNotThrowAnyException();

        f.completeExceptionally(new RuntimeException("boom"));

        verify(kt).send("auth.user.registered", key, e);
        verifyNoMoreInteractions(kt);
    }

    private static SendResult<String, Object> sendResult(String topic, int partition, long offset) {
        ProducerRecord<String, Object> pr = new ProducerRecord<>(topic, partition, "k", "v");
        RecordMetadata md = new RecordMetadata(new TopicPartition(topic, partition), 0, 0, offset, 0, 0);
        return new SendResult<>(pr, md);
    }
}
