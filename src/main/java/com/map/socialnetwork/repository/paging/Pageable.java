package com.map.socialnetwork.repository.paging;

public interface Pageable<E> {
    int getPageNumber();
    int getPageSize();
}
