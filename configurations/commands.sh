#!/bin/bash
    aws cloudformation create-stack --region us-west-2 --stack-name kindlepublishingservice-createtables --template-body file://tables.template.yml --capabilities CAPABILITY_IAM
#    aws dynamodb batch-write-item --request-items file://configurations/PublishingStatusData.json
    aws dynamodb batch-write-item --request-items file://configurations/CatalogItemVersionsData.json