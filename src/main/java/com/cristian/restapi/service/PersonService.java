package com.cristian.restapi.service;

import com.cristian.restapi.controller.PersonController;
import com.cristian.restapi.data.vo.v1.PersonVO;
import com.cristian.restapi.exception.ResourceNotFoundException;
import com.cristian.restapi.mapper.DozerMapper;
import com.cristian.restapi.model.Person;
import com.cristian.restapi.repository.PersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class PersonService {

    private final Logger logger = Logger.getLogger(PersonService.class.getName());
    @Autowired
    PersonRepository repository;

    public Page<PersonVO> findAll(Pageable pageable) {
        logger.info("Listando todas as pessoas");

        var personPage = repository.findAll(pageable);
        var personVosPage = personPage.map(p -> DozerMapper.parseObject(p, PersonVO.class));
        personVosPage.map(
                p -> p.add(linkTo(methodOn(PersonController.class)
                        .findById(p.getKey()))
                        .withSelfRel()
                )
        );
        return personVosPage;

    }

    public PersonVO findById(Long id) {
        logger.info("Encontrando uma pessoa");

        var entity = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        var vo = DozerMapper.parseObject(entity, PersonVO.class);
        vo.add(linkTo(methodOn(PersonController.class).findById(id)).withSelfRel());
        return vo;
    }

    public PersonVO create(PersonVO person) {
//		if (person == null) throw new RequiredObjectIsNullException();

        logger.info("Registrando uma pessoa");
        var entity = DozerMapper.parseObject(person, Person.class);
        var vo = DozerMapper.parseObject(repository.save(entity), PersonVO.class);
        vo.add(linkTo(methodOn(PersonController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    public PersonVO update(PersonVO person) {
        logger.info("Atualizando um registro de pessoa");

        var entity = repository.findById(person.getKey()).orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        entity.setAddress(person.getAddress());
        entity.setGender(person.getGender());

        var vo = DozerMapper.parseObject(repository.save(entity), PersonVO.class);
        vo.add(linkTo(methodOn(PersonController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    @Transactional
    public PersonVO disablePerson(Long id) {
        logger.info("Disabling a person");
        repository.disablePerson(id);
        var entity = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        var vo = DozerMapper.parseObject(entity, PersonVO.class);
        vo.add(linkTo(methodOn(PersonController.class).findById(id)).withSelfRel());
        return vo;
    }

    public void delete(Long id) {
        logger.info("Deletando um registro de pessoa");

        var entity = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        repository.delete(entity);
    }
}
