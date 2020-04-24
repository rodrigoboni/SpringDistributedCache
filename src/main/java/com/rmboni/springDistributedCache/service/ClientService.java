package com.rmboni.springDistributedCache.service;

import com.rmboni.springDistributedCache.exception.EntityNotFoundException;
import com.rmboni.springDistributedCache.model.Client;
import com.rmboni.springDistributedCache.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@CacheConfig(cacheNames = "client")
public class ClientService {
	
	@Autowired
	private ClientRepository clientRepository;
	
	@Cacheable(key="#root.method.name")
	public List<Client> findAll() {
		return clientRepository.findAll();
	}
	
	@Cacheable(key="#id")
	public Client findById(final Long id) {
		return clientRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Id not found: " + id));
	}
	
	@CacheEvict(allEntries = true)
	public Client create(final Client client) {
		return clientRepository.save(client);
	}
	
	@CachePut(key="#client.getId()")
	public Client update(final Client client) {
		if(client.getId() == null) {
			throw new EntityNotFoundException("Invalid id for update");
		}
		return clientRepository.save(client);
	}
	
	@CacheEvict(key="#id")
	public void delete(final Long id) {
		clientRepository.deleteById(id);
	}
}
