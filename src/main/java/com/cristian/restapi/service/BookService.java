package com.cristian.restapi.service;

import com.cristian.restapi.controller.BookController;
import com.cristian.restapi.data.vo.v1.BookVO;
import com.cristian.restapi.exception.ResourceNotFoundException;
import com.cristian.restapi.mapper.DozerMapper;
import com.cristian.restapi.model.Book;
import com.cristian.restapi.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class BookService {

    private final Logger logger = Logger.getLogger(BookService.class.getName());

    @Autowired
    private BookRepository repository;

    @Autowired
    PagedResourcesAssembler<BookVO> assembler;


    public PagedModel<EntityModel<BookVO>> findall(Pageable pageable) {
        logger.info("Listando todos os livros");


        var bookPage = repository.findAll(pageable);

        var bookVOPage = bookPage.map(b -> DozerMapper.parseObject(b, BookVO.class));
        bookVOPage.map(b -> b.add(linkTo(methodOn(BookController.class).findById(b.getKey())).withSelfRel()));

        Link link = linkTo(methodOn(BookController.class).findAll(pageable.getPageNumber(), pageable.getPageSize(), "Asc"

        )).withSelfRel();
        return assembler.toModel(bookVOPage, link);
    }

    public BookVO findById(Long id) {
        logger.info("Encontrando um livro");
        var entity = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));
        var vo = DozerMapper.parseObject(entity, BookVO.class);
        vo.add(linkTo(methodOn(BookController.class).findById(id)).withSelfRel());
        // vo.add(linkTo(methodOn(PersonController.class).findById(id)).withSelfRel());
        return vo;
    }

    public BookVO create(BookVO book) {
        logger.info("Registrando um livro");
        var entity = DozerMapper.parseObject(book, Book.class);
        var vo = DozerMapper.parseObject(repository.save(entity), BookVO.class);
        vo.add(linkTo(methodOn(BookController.class).findById(vo.getKey())).withSelfRel());
        return vo;
    }

    public BookVO update(BookVO book) {
        logger.info("Atualizando um livro");
        var entity = repository.findById(book.getKey()).orElseThrow(() -> new ResourceNotFoundException("No records found for this ID!"));

        entity.setTitle(book.getTitle());
        entity.setAuthor(book.getAuthor());
        entity.setPrice(book.getPrice());
        entity.setLaunchDate(book.getLaunchDate());

        var vo = DozerMapper.parseObject(repository.save(entity), BookVO.class);
        vo.add(linkTo(methodOn(BookController.class).findById(vo.getKey())).withSelfRel());

        return vo;
    }

    public void delete(Long id) {
        logger.info("REmovendo um livro");
        var entity = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No records found for this ID to delete"));
        repository.delete(entity);
    }
}
