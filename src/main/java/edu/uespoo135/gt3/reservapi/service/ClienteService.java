package edu.uespoo135.gt3.reservapi.service;

import edu.uespoo135.gt3.reservapi.dto.ClienteDTO;
import edu.uespoo135.gt3.reservapi.mapper.ClienteMapper;
import edu.uespoo135.gt3.reservapi.model.Cliente;
import edu.uespoo135.gt3.reservapi.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteDTO crearCliente(ClienteDTO clienteDTO) {
        Cliente cliente = ClienteMapper.toEntity(clienteDTO);
        Cliente guardado = clienteRepository.save(cliente);
        return ClienteMapper.toDTO(guardado);
    }

    public List<ClienteDTO> listarClientes() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteMapper::toDTO)
                .collect(Collectors.toList());
    }
}
