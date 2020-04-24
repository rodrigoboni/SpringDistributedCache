package com.rmboni.springDistributedCache.resource;

import com.rmboni.springDistributedCache.model.Client;
import com.rmboni.springDistributedCache.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientResource {
	@Autowired
	private ClientService clientService;
	
	@GetMapping
	public List<Client> findAll() {
		return clientService.findAll();
	}
	
	@GetMapping("/{id}")
	public Client findById(@PathVariable("id") final Long id) {
		return clientService.findById(id);
	}
	
	@PostMapping
	public Client create(@RequestBody final Client client) {
		return clientService.create(client);
	}
	
	@PutMapping
	public Client update(@RequestBody final Client client) {
		return clientService.update(client);
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable("id") final Long id) {
		clientService.delete(id);
	}
}
