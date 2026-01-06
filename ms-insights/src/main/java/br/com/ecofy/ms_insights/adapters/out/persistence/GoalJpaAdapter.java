package br.com.ecofy.ms_insights.adapters.out.persistence;

import br.com.ecofy.ms_insights.adapters.out.persistence.mapper.GoalMapper;
import br.com.ecofy.ms_insights.adapters.out.persistence.repository.GoalRepository;
import br.com.ecofy.ms_insights.core.domain.Goal;
import br.com.ecofy.ms_insights.core.port.out.LoadGoalsPort;
import br.com.ecofy.ms_insights.core.port.out.SaveGoalPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoalJpaAdapter implements SaveGoalPort, LoadGoalsPort {

    private final GoalRepository repository;

    @Override
    public Goal save(Goal goal) {
        Objects.requireNonNull(goal, "goal must not be null");
        Objects.requireNonNull(goal.getId(), "goal.id must not be null");
        Objects.requireNonNull(goal.getUserId(), "goal.userId must not be null");

        log.debug(
                "[GoalJpaAdapter] - [save] -> Saving goal id={} userId={} status={}",
                goal.getId(),
                goal.getUserId().value(),
                goal.getStatus()
        );

        var saved = repository.save(GoalMapper.toEntity(goal));

        log.debug(
                "[GoalJpaAdapter] - [save] -> Saved goal id={} userId={}",
                saved.getId(),
                saved.getUserId()
        );

        return GoalMapper.toDomain(saved);
    }

    @Override
    public Goal findById(UUID goalId) {
        Objects.requireNonNull(goalId, "goalId must not be null");

        log.debug("[GoalJpaAdapter] - [findById] -> goalId={}", goalId);

        return repository.findById(goalId)
                .map(entity -> {
                    log.debug("[GoalJpaAdapter] - [findById] -> FOUND goalId={}", goalId);
                    return GoalMapper.toDomain(entity);
                })
                .orElseGet(() -> {
                    log.debug("[GoalJpaAdapter] - [findById] -> NOT FOUND goalId={}", goalId);
                    return null;
                });
    }

    @Override
    public List<Goal> findByUserId(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        log.debug("[GoalJpaAdapter] - [findByUserId] -> userId={}", userId);

        var result = repository.findTop200ByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(GoalMapper::toDomain)
                .toList();

        log.debug(
                "[GoalJpaAdapter] - [findByUserId] -> userId={} goalsReturned={}",
                userId,
                result.size()
        );

        return result;
    }
}
