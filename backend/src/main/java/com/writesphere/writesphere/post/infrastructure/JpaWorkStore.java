package com.writesphere.writesphere.post.infrastructure;

import java.util.Optional;
import java.util.UUID;

import com.writesphere.writesphere.post.application.WorkStore;
import com.writesphere.writesphere.post.domain.Work;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class JpaWorkStore implements WorkStore {
    private final EntityManager entities;

    public JpaWorkStore(EntityManager entities) {
        this.entities = entities;
    }

    @Override
    public Work insert(Work work) {
        var row = new WorkRow(work);
        entities.persist(row);
        entities.flush();
        return row.toDomain();
    }

    @Override
    public Optional<Work> find(UUID id) {
        return Optional.ofNullable(entities.find(WorkRow.class, id)).map(WorkRow::toDomain);
    }

    @Override
    public Work update(Work work) {
        WorkRow row = entities.find(WorkRow.class, work.id());
        row.apply(work);
        entities.flush();
        return row.toDomain();
    }
}
