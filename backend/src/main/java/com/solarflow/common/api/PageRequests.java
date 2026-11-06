package com.solarflow.common.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageRequests {

  private PageRequests() {
  }

  public static PageRequest of(int page, int size) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 100);
    return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
  }
}
