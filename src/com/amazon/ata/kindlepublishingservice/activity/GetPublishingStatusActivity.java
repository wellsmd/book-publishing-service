package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.converters.BookPublishRequestConverter;
import com.amazon.ata.kindlepublishingservice.converters.PublishingStatusRecordConverter;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class GetPublishingStatusActivity {

    private PublishingStatusDao publishingStatusDao;
    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao) {
        this.publishingStatusDao = publishingStatusDao;
    }

    /**
     * Retrieves a list of the entries associated with the provided publishing record id.
     *
     * @param publishingStatusRequest Object containing the publishing record ID to get from the Catalog.
     * @return GetPublishingStatusResponse Response object containing the requested publishing records.
     */
    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {
        List<PublishingStatusItem> publishingStatusItems;
        List<PublishingStatusRecord> publishingStatusRecords = new ArrayList<>();
        publishingStatusItems = publishingStatusDao.getPublishingStatuses(publishingStatusRequest.getPublishingRecordId());
        System.out.println(publishingStatusItems);

        for (PublishingStatusItem item : publishingStatusItems) {
            PublishingStatusRecord record = PublishingStatusRecordConverter.toPublishingStatusRecord(item);
            publishingStatusRecords.add(record);
        }

        // When populating the response, convert the list of PublishingStatusItems to a list of PublishingStatusRecords.
        return GetPublishingStatusResponse.builder()
                .withPublishingStatusHistory(publishingStatusRecords)
                .build();
    }

}
