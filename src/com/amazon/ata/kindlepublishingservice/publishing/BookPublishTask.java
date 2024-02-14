package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import javax.inject.Inject;

public final class BookPublishTask implements Runnable {

    private final BookPublishRequestManager bookPublishRequestManager;
    private final PublishingStatusDao publishingStatusDao;
    private final CatalogDao catalogDao;

    @Inject
    public BookPublishTask(PublishingStatusDao publishingStatusDao, CatalogDao catalogDao,
                           BookPublishRequestManager bookPublishRequestManager) {
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
        this.bookPublishRequestManager = bookPublishRequestManager;
    }

    // 1. BookPublisher schedules BookPublishTask to execute repeatedly while the service runs. (This step is actually last.)
    // 2. BookPublishTask retrieves a BookPublishRequest from BookPublishRequestManager (call getBookPublishRequestToProcess).
    //    If the BookPublishRequestManager has no publishing requests, BookPublishTask should return immediately without taking action.
    // 3. For each BookPublishRequest in the queue, perform the following steps:
    //      1. Add an entry to the Publishing Status table with state IN_PROGRESS. (PublishingStatusDao.setPublishingStatus)
    //      2. Perform formatting and conversion of the book. (KindleFormatConverter.format)
    //      3. Add the book to the CatalogItemVersion table (update CatalogDao with new methods for BookPublishTask to call).
    //          a. If this request is updating an existing book:
    //              i.  The entry in CatalogItemVersion uses the same bookId but with the version incremented by 1.
    //              ii. The previously active version of the book is marked inactive.
    //          b. Otherwise, a new bookId is generated for the book and the book is stored in CatalogItemVersion as version 1.
    //      4. Add an item to the Publishing Status table with state SUCCESSFUL if all the processing steps succeed.
    //         If an exception is caught while processing, add an item into the Publishing Status table with state
    //         FAILED and include the exception message.

    // To switch the BookPublisher to start scheduling your new BookPublishTask instead of the NoOpTask,
    // update the Dagger code that passes a NoOpTask to the BookPublisher constructor. Once you’ve made
    // this switch, you can delete NoOpTask and its test class.

    // Called:
    //      BookPublishRequestManager.getBookPublishRequestToProcess
    //      PublishingStatusDao.setPublishingStatus
    //      CatalogDao.createOrUpdateBook (NEW!)
    //      KindleFormatConverter.format
    //
    // Received:
    //      BookPublishRequest from BookPublishRequestManager
    //      PublishingStatusItem from PublishingStatusDao
    //      CatalogItemVersion from CatalogDao
    //      KindleFormattedBook from KindleFormatConverter
    //
    //      CatalogDao throws BookNotFoundException

    @Override
    public void run() {
        KindleFormattedBook kindleFormattedBook;
        CatalogItemVersion catalogItemVersion;
        BookPublishRequest request = bookPublishRequestManager.getBookPublishRequestToProcess();
        if (request == null) {
            return;
        }
        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                PublishingRecordStatus.IN_PROGRESS, request.getBookId());
        try {
            kindleFormattedBook = KindleFormatConverter.format(request);
            catalogItemVersion = catalogDao.createOrUpdateBook(kindleFormattedBook);
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                    PublishingRecordStatus.SUCCESSFUL, catalogItemVersion.getBookId());
        } catch (BookNotFoundException e) {
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                    PublishingRecordStatus.FAILED, request.getBookId());
        }
    }
}
