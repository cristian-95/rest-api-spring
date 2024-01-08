package com.cristian.restapi.integrationtests.vo.wrappers;


import com.cristian.restapi.integrationtests.vo.PersonVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@XmlRootElement
public class PersonEmbeddedVO implements Serializable {

    private static final long serialVersionUID = 1L;
    @JsonProperty("personVOList")
    private List<PersonVO> personVOList;

    public PersonEmbeddedVO() {
    }

    public List<PersonVO> getPersonVOList() {
        return personVOList;
    }

    public void setPersonVOList(List<PersonVO> personVOList) {
        this.personVOList = personVOList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersonEmbeddedVO that = (PersonEmbeddedVO) o;

        return Objects.equals(personVOList, that.personVOList);
    }

    @Override
    public int hashCode() {
        return personVOList != null ? personVOList.hashCode() : 0;
    }
}