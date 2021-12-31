package com.map.socialnetwork.repository.paging;

public record PageableImpl<E>(int pageNumber, int pageSize) implements Pageable<E> {
    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }
}
