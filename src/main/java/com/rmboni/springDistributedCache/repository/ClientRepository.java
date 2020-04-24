package com.rmboni.springDistributedCache.repository;

import com.rmboni.springDistributedCache.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
