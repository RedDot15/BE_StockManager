import boto3
import csv
from decimal import Decimal
import json

DYNAMODB_LOCAL_ENDPOINT = "http://localhost:8000"
AWS_REGION = "ap-southeast-1"
AWS_ACCESS_KEY_ID = "fakeAccessKey"
AWS_SECRET_ACCESS_KEY = "fakeSecretKey"

MASTER_DATA_TABLE_NAME = "MasterData"

def get_dynamodb_resource():
    return boto3.resource(
        "dynamodb",
        region_name=AWS_REGION,
        endpoint_url=DYNAMODB_LOCAL_ENDPOINT,
        aws_access_key_id=AWS_ACCESS_KEY_ID,
        aws_secret_access_key=AWS_SECRET_ACCESS_KEY
    )

def import_master_data_from_csv(file_path):
    """
    Reading data from CSV file then import into table MasterData.
    Handling datatype converter with List/JSON.
    """
    dynamodb = get_dynamodb_resource()
    table = dynamodb.Table(MASTER_DATA_TABLE_NAME)

    with open(file_path, mode='r', encoding='utf-8-sig') as file:
        csv_reader = csv.DictReader(file)

        line_num = 1 # Tracking CSV line-number
        for row in csv_reader:
            line_num += 1
            item = {}
            for key, value in row.items():
                # Ignore empty line
                if value is None or value.strip() == '':
                    continue

                # Data convertion
                if key in ['import_price', 'sale_price', 'amount', 'vat', 'total', 'tax']:
                    try:
                        item[key] = Decimal(value)
                    except Exception as e:
                        print(f"Warning: Could not convert '{key}': '{value}' to Decimal on line {line_num}. Skipping. Error: {e}")
                        continue
                elif key in ['role_ids', 'permission_ids']:
                    try:
                        item[key] = json.loads(value)
                    except json.JSONDecodeError as e:
                        print(f"Warning: Could not parse JSON for '{key}': '{value}' on line {line_num}. Storing as string. Error: {e}")
                        item[key] = value
                elif key == 'sales': # Handling sales field (List of Objects)
                    try:
                        sales_list = json.loads(value)
                        # Ensure numbers in sale_item is also Decimal
                        processed_sales_list = []
                        for sale_item in sales_list:
                            processed_sale_item = {}
                            for skey, sval in sale_item.items():
                                if skey in ['amount', 'price', 'vat']:
                                    try:
                                        processed_sale_item[skey] = Decimal(str(sval)) # Convert to string first for Decimal
                                    except Exception as e:
                                        print(f"Warning: Could not convert sales item '{skey}': '{sval}' to Decimal on line {line_num}. Error: {e}")
                                        processed_sale_item[skey] = sval
                                else:
                                    processed_sale_item[skey] = sval
                            processed_sales_list.append(processed_sale_item)
                        item[key] = processed_sales_list
                    except json.JSONDecodeError as e:
                        print(f"Warning: Could not parse JSON for 'sales': '{value}' on line {line_num}. Storing as string. Error: {e}")
                        item[key] = value
                else:
                    item[key] = value.copy() if isinstance(value, dict) else value

            if 'pk' not in item or 'entity_id' not in item:
                print(f"Skipping line {line_num}: Missing 'pk' or 'entity_id' (required keys). Row: {row}")
                continue

            try:
                table.put_item(Item=item)
                print(f"Inserted item ({item.get('pk', 'N/A')}, {item.get('entity_id', 'N/A')}): {item.get('name', 'No Name')}")
            except Exception as e:
                print(f"Error inserting item on line {line_num} ({item.get('pk', 'N/A')}, {item.get('entity_id', 'N/A')}): {e}")

if __name__ == "__main__":
    csv_file_path = 'data.csv'
    print(f"Starting data import from {csv_file_path} to {MASTER_DATA_TABLE_NAME} table...")
    import_master_data_from_csv(csv_file_path)
    print("Data import finished.")