package com.hotel.management.service;

import java.util.List;

/**
 * Generic repository interface that defines standard CRUD operations.
 * All service classes implement this to enforce a consistent data-access contract.
 *
 * @param <T>  the entity type managed by this repository
 * @param <ID> the type of the entity's primary key
 */
public interface Repository<T, ID> {

    /**
     * Persist a new entity and return its generated ID (or a success flag).
     */
    ID save(T entity) throws Exception;

    /**
     * Retrieve all entities of type T.
     */
    List<T> findAll() throws Exception;

    /**
     * Delete the entity identified by {@code id}.
     *
     * @return true if the entity was deleted, false otherwise
     */
    boolean deleteById(ID id) throws Exception;
}
