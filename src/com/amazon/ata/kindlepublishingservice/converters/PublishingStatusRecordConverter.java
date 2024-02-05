package com.amazon.ata.kindlepublishingservice.converters;

import com.amazon.ata.coral.converter.CoralConverterUtil;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

import java.util.List;

/**
 * Converters for PublishingStatusRecord related objects.
 */
public class PublishingStatusRecordConverter {

    private PublishingStatusRecordConverter() {}

    /**
     * Converts the given {@link PublishingStatusItem} object to a {@link PublishingStatusRecord}. Generates
     * a publishing record id.
     *
     * @param request The PublishingStatusItem list to convert.
     * @return The converted PublishingStatusRecord list.
     */
    public static PublishingStatusRecord toPublishingStatusRecord(PublishingStatusItem request) {

        return PublishingStatusRecord.builder()
            .withStatus(request.getStatus().toString())
            .withStatusMessage(request.getStatusMessage())
            .withBookId(request.getBookId())
            .build();
    }

}
