package com.hotel.management.service;

import java.util.List;

public interface Repository<T, ID> {

    ID save(T entity) throws Exception;

    List<T> findAll() throws Exception;

    boolean deleteById(ID id) throws Exception;
}
