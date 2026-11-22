package com.writesphere.writesphere.post.application;

import java.util.Optional;
import java.util.UUID;

import com.writesphere.writesphere.post.domain.Work;

public interface WorkStore {
    Work insert(Work work);
    Optional<Work> find(UUID id);
    Work update(Work work);
}
