package com.cristian.restapi.integrationtests.vo.wrappers;

import com.cristian.restapi.integrationtests.vo.BookVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@XmlRootElement
public class BookEmbeddedVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("bookVOList")
    private List<BookVO> bookVOList;


    public BookEmbeddedVO() {
    }

    public List<BookVO> getBookVOList() {
        return bookVOList;
    }

    public void setBookVOList(List<BookVO> bookVOList) {
        this.bookVOList = bookVOList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BookEmbeddedVO that = (BookEmbeddedVO) o;

        return Objects.equals(bookVOList, that.bookVOList);
    }

    @Override
    public int hashCode() {
        return bookVOList != null ? bookVOList.hashCode() : 0;
    }
}
