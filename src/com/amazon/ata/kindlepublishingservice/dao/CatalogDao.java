package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }
        return book;
    }

    public void validateBookExists(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public CatalogItemVersion RemoveBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }
        book.setInactive(true);
        dynamoDbMapper.save(book);
        return book;
    }

//    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook kindleFormattedBook) throws BookNotFoundException {
//        CatalogItemVersion book = getLatestVersionOfBook(kindleFormattedBook.getBookId());
//        if (book == null) {
//            book = setCatalogItemVersion(KindlePublishingUtils.generateBookId(), 1, false, kindleFormattedBook);
//            return getLatestVersionOfBook(book.getBookId());
//        }
//        validateBookExists(book.getBookId());
//        RemoveBookFromCatalog(book.getBookId());
//        book.setVersion(book.getVersion() + 1);
//        dynamoDbMapper.save(book);
//
//        return getLatestVersionOfBook(book.getBookId());
//    }

    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook kindleFormattedBook) throws BookNotFoundException {
        if (kindleFormattedBook.getBookId() == null) {
            String bookId = KindlePublishingUtils.generateBookId();
            CatalogItemVersion newBook = new CatalogItemVersion();
            newBook.setBookId(bookId);
            newBook.setTitle(kindleFormattedBook.getTitle());
            newBook.setAuthor(kindleFormattedBook.getAuthor());
            newBook.setText(kindleFormattedBook.getText());
            newBook.setGenre(kindleFormattedBook.getGenre());
            newBook.setInactive(false);
            newBook.setVersion(1);
            dynamoDbMapper.save(newBook);
            return getLatestVersionOfBook(newBook.getBookId());
        }
        validateBookExists(kindleFormattedBook.getBookId());
        CatalogItemVersion updatedBook = getLatestVersionOfBook(kindleFormattedBook.getBookId());
        RemoveBookFromCatalog(updatedBook.getBookId());
        updatedBook.setVersion(updatedBook.getVersion() + 1);
        dynamoDbMapper.save(updatedBook);
        return getLatestVersionOfBook(kindleFormattedBook.getBookId());
    }

    public CatalogItemVersion setCatalogItemVersion(String bookId, int version, boolean isInactive,
                                                    KindleFormattedBook kindleFormattedBook) {
        CatalogItemVersion item = new CatalogItemVersion();
        item.setBookId(bookId);
        item.setTitle(kindleFormattedBook.getTitle());
        item.setAuthor(kindleFormattedBook.getAuthor());
        item.setText(kindleFormattedBook.getText());
        item.setGenre(kindleFormattedBook.getGenre());
        item.setInactive(isInactive);
        item.setVersion(version);
        dynamoDbMapper.save(item);
        return getLatestVersionOfBook(item.getBookId());
    }
}
