package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Queue;

@Singleton
public class BookPublishRequestManager {

    private Queue<BookPublishRequest> bookPublishRequests = new LinkedList<>();

    @Inject
    public BookPublishRequestManager() {
    }

    public void addBookPublishRequest (BookPublishRequest bookPublishRequest) {
        bookPublishRequests.add(bookPublishRequest);
    }

    public BookPublishRequest getBookPublishRequestToProcess () {
        if (!bookPublishRequests.isEmpty()) {
            return bookPublishRequests.poll();
        }
        return null;
    }

}
