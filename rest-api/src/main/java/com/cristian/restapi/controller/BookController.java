package com.cristian.restapi.controller;

import com.cristian.restapi.data.vo.v1.BookVO;
import com.cristian.restapi.data.vo.v1.PersonVO;
import com.cristian.restapi.service.BookService;
import com.cristian.restapi.util.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books/v1")
@Tag(name = "Books", description = "Endpoint for managing books")
public class BookController {

    @Autowired
    private BookService service;

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_YML, MediaType.APPLICATION_XML})
    @Operation(summary = "Finds all books", description = "Finds all books",
            tags = {"Books"},
            responses = {
            @ApiResponse(description = "Success", responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PersonVO.class)))
            ),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<BookVO>>> findAll(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "10") Integer size, @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        var sortDirection = "DESC".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        org.springframework.data.domain.Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "title"));

        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_YML, MediaType.APPLICATION_XML})
    @Operation(summary = "Finds a book", description = "Finds a book",
            tags = {"Books"},
            responses = {
            @ApiResponse(description = "Success", responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PersonVO.class)))),
            @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
            @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
            @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
            @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
    })
    public ResponseEntity<BookVO> findById(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_YML, MediaType.APPLICATION_XML},
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_YML, MediaType.APPLICATION_XML})
    @Operation(summary = "Adds a new book", description = "Adds a new book", tags = {"Books"},
            responses = {
            @ApiResponse(description = "Created", responseCode = "201", content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = BookVO.class)))),
            @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
            @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
            @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
    })
    public ResponseEntity<BookVO> create(@RequestBody BookVO book) {
        return ResponseEntity.status(201).body(service.create(book));
    }

    @PutMapping(consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_YML, MediaType.APPLICATION_XML},
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_YML, MediaType.APPLICATION_XML})
    @Operation(summary = "Upgrades a book", description = "Upgrades a book", tags = {"Books"},
            responses = {
            @ApiResponse(description = "Success", responseCode = "200", content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = BookVO.class)))),
            @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
            @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
            @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
            @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)})
    public ResponseEntity<BookVO> update(@RequestBody BookVO book) {
        return ResponseEntity.ok(service.update(book));
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Deletes a book", description = "Deletes a book",
            tags = {"Books"},
            responses = {
                    @ApiResponse(description = "No Content", responseCode = "204", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)})
    public ResponseEntity<?> delete(@PathVariable(value = "id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
