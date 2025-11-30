package com.sloyardms.stashbox.utils;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {

    private List<T> content;
    private PageMetadata page;

    @Data
    public static class PageMetadata {

        private int size;
        private long totalElements;
        private int totalPages;
        private int number;

    }
}
