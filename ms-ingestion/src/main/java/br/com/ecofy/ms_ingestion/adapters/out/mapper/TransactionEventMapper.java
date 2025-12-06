package br.com.ecofy.ms_ingestion.adapters.out.mapper;

import br.com.ecofy.ms_ingestion.core.domain.RawTransaction;
import br.com.ecofy.ms_ingestion.core.domain.enums.TransactionSourceType;
import br.com.ecofy.ms_ingestion.core.domain.valueobject.Money;
import br.com.ecofy.ms_ingestion.core.domain.valueobject.TransactionDate;
import br.com.ecofy.ms_ingestion.core.port.in.IngestTransactionEventUseCase;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

@Slf4j
@Component
public class TransactionEventMapper {

    // Aqui você vai mapear do payload JSON real -> RawTransaction.
    // Para simplificar, vou criar um único registro dummy.

    public IngestTransactionEventUseCase.IngestEventCommand toCommand(ConsumerRecord<String, String> record,
                                                                      String payload) {

        log.debug("[TransactionEventMapper] - [toCommand] -> Transformando payload em comando key={} payloadLength={}",
                record.key(), payload != null ? payload.length() : 0);

        RawTransaction tx = RawTransaction.create(
                java.util.UUID.randomUUID(),
                record.key(),
                "TX from event: " + record.key(),
                new TransactionDate(LocalDate.now()),
                new Money(new BigDecimal("0.00"), Currency.getInstance("BRL")),
                TransactionSourceType.KAFKA_EVENT
        );

        return new IngestTransactionEventUseCase.IngestEventCommand(
                "unknown",
                record.key(),
                List.of(tx)
        );

    }

}