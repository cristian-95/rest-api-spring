package com.cristian.restapi.serialization.converter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

public class YamlJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
        // TODO: revisar a classe de conversão de yaml para http message
    public YamlJackson2HttpMessageConverter() {
        super(
                new YAMLMapper()
                        .setSerializationInclusion(
                                JsonInclude.Include.NON_NULL),
                MediaType.parseMediaType("application/x-yaml")
        );
    }

}