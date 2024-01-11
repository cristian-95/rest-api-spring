package com.cristian.restapi.integrationtests.vo.pagedmodels;

import com.cristian.restapi.integrationtests.vo.BookVO;

import java.util.List;

public class PagedModelBook {

    private List<BookVO> content;

    public PagedModelBook() {
    }

    public List<BookVO> getContent() {
        return content;
    }

    public void setContent(List<BookVO> content) {
        this.content = content;
    }
}
