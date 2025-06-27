import boto3
from botocore.exceptions import ClientError

DYNAMODB_LOCAL_ENDPOINT = "http://localhost:8000"
AWS_REGION = "ap-southeast-1"
AWS_ACCESS_KEY_ID = "fakeAccessKey"
AWS_SECRET_ACCESS_KEY = "fakeSecretKey"

def create_dynamodb_client():
    return boto3.client(
        "dynamodb",
        region_name=AWS_REGION,
        endpoint_url=DYNAMODB_LOCAL_ENDPOINT,
        aws_access_key_id=AWS_ACCESS_KEY_ID,
        aws_secret_access_key=AWS_SECRET_ACCESS_KEY
    )

def create_table(client, table_name, attribute_definitions, key_schema,
                 provisioned_throughput, global_secondary_indexes=None, local_secondary_indexes=None):
    try:
        table_params = {
            "TableName": table_name,
            "AttributeDefinitions": attribute_definitions,
            "KeySchema": key_schema,
            "ProvisionedThroughput": provisioned_throughput
        }
        if global_secondary_indexes:
            table_params["GlobalSecondaryIndexes"] = global_secondary_indexes
        if local_secondary_indexes:
            table_params["LocalSecondaryIndexes"] = local_secondary_indexes

        print(f"Attempting to create table: {table_name}...")
        table = client.create_table(**table_params)
        client.get_waiter("table_exists").wait(TableName=table_name)
        print(f"Table '{table_name}' created successfully.")
    except ClientError as e:
        if e.response["Error"]["Code"] == "ResourceInUseException":
            print(f"Table '{table_name}' already exists.")
        else:
            print(f"Error creating table '{table_name}': {e}")
    except Exception as e:
        print(f"An unexpected error occurred while creating table '{table_name}': {e}")

def main():
    dynamodb = create_dynamodb_client()

    # --- Define MasterData Table ---
    master_data_table_name = "MasterData"
    master_data_attribute_definitions = [
        {"AttributeName": "pk", "AttributeType": "S"},         # General Partition Key
        {"AttributeName": "entity_id", "AttributeType": "S"},  # General Sort Key
        {"AttributeName": "created_at", "AttributeType": "S"}, # For Invoice's LSI
        {"AttributeName": "email", "AttributeType": "S"},       # For User's LSI
        {"AttributeName": "category_name", "AttributeType": "S"}, # For category_name-sale_price-gsi Partition Key
        {"AttributeName": "sale_price", "AttributeType": "N"} # For category_name-sale_price-gsi Sort Key
    ]
    master_data_key_schema = [
        {"AttributeName": "pk", "KeyType": "HASH"},
        {"AttributeName": "entity_id", "KeyType": "RANGE"}
    ]
    master_data_provisioned_throughput = {
        "ReadCapacityUnits": 1000,
        "WriteCapacityUnits": 500
    }

    master_data_local_secondary_indexes = [
        # Define Local Secondary Index (LSI) for Invoice
        {
            "IndexName": "pk-created_at-lsi", # LSI name
            "KeySchema": [
                {"AttributeName": "pk", "KeyType": "HASH"},
                {"AttributeName": "created_at", "KeyType": "RANGE"}
            ],
            "Projection": {"ProjectionType": "ALL"} # Project all
        },
        # Define Local Secondary Index (LSI) for Product
        {
            "IndexName": "pk-sale_price-lsi", # LSI name
            "KeySchema": [
                {"AttributeName": "pk", "KeyType": "HASH"},
                {"AttributeName": "sale_price", "KeyType": "RANGE"}
            ],
            "Projection": {"ProjectionType": "ALL"} # Project all
        }
    ]

    master_data_global_secondary_indexes = [
        # Define Global Secondary Index (GSI) for Product
        {
            "IndexName": "category_name-sale_price-gsi", # GSI name
            "KeySchema": [
                {"AttributeName": "category_name", "KeyType": "HASH"},
                {"AttributeName": "sale_price", "KeyType": "RANGE"},
            ],
            "Projection": {"ProjectionType": "ALL"}, # Project all attributes
            "ProvisionedThroughput": {
                "ReadCapacityUnits": 1000,
                "WriteCapacityUnits": 500
            }
        },
        # Define Global Secondary Index (GSI) for User
        {
            "IndexName": "email-gsi", # GSI name
            "KeySchema": [
                {"AttributeName": "email", "KeyType": "HASH"},
            ],
            "Projection": {"ProjectionType": "ALL"}, # Project all attributes
            "ProvisionedThroughput": {
                "ReadCapacityUnits": 200,
                "WriteCapacityUnits": 100
            }
        }
    ]

    create_table(
        dynamodb,
        master_data_table_name,
        master_data_attribute_definitions,
        master_data_key_schema,
        master_data_provisioned_throughput,
        local_secondary_indexes=master_data_local_secondary_indexes,
        global_secondary_indexes=master_data_global_secondary_indexes
    )

if __name__ == "__main__":
    main()