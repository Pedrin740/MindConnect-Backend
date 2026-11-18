
package br.com.mindconnect.controller;

import br.com.mindconnect.config.Acesso;
import br.com.mindconnect.config.Nivel;
import br.com.mindconnect.dto.ContatoEntradaDto;
import br.com.mindconnect.dto.ContatoSaidaDto;
import br.com.mindconnect.model.Contato;
import br.com.mindconnect.service.ContatoService;

import jakarta.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

// Endpoint público para recebimento de mensagens do formulário de contato.
@RestController
@RequestMapping("/api/contatos")
public class ContatoController {

    private final ContatoService contatoService;
    private final ModelMapper mapper;

    public ContatoController(
            ContatoService contatoService,
            ModelMapper mapper
    ) {
        this.contatoService = contatoService;
        this.mapper = mapper;
    }

    @Acesso(Nivel.PUBLICO)
    @PostMapping
    public ResponseEntity<ContatoSaidaDto> cadastrar(
            @Valid @RequestBody ContatoEntradaDto dto
    ) {

        Contato criado = contatoService.cadastrar(
                mapper.map(dto, Contato.class)
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/contatos/{id}")
                .buildAndExpand(criado.getIdContato())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(mapper.map(criado, ContatoSaidaDto.class));
    }
}
