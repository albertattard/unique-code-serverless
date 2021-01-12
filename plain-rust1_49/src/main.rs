use std::error::Error;
use lambda_runtime::{error::HandlerError, lambda, Context};
use serde_derive::{Deserialize, Serialize};
use rusoto_core::{Region};
use rusoto_dynamodb::{DynamoDb, DynamoDbClient, PutItemInput, AttributeValue};
use std::collections::HashMap;
use tokio::runtime::Runtime;
use std::iter;
use rand::{Rng, thread_rng};
use rand::distributions::Alphanumeric;

#[derive(Deserialize)]
struct CreateUniqueCodeRequest {
    // #[serde(rename = "usedBy")]
    // used_by: String,
    // length: u8,
    // reference: String,
    // description: String,
}

#[derive(Serialize)]
struct UniqueCode {
    code: String,
}

fn main() -> Result<(), Box<dyn Error>> {
    lambda!(handle_request);
    Ok(())
}

fn handle_request(_request: CreateUniqueCodeRequest, _context: Context) -> Result<UniqueCode, HandlerError> {
    let mut rng = thread_rng();
    let code: String = iter::repeat(())
        .map(|()| rng.sample(Alphanumeric))
        .map(char::from)
        .take(8)
        .collect();
    println!("Random chars: {}", code);

    let mut item: HashMap<String, AttributeValue> = HashMap::new();
    item.insert(String::from("Code"), AttributeValue { s: Some(code.clone()), ..Default::default() });

    let put_item = PutItemInput {
        table_name: "UniqueCodes".to_string(),
        return_consumed_capacity: None,
        item,
        condition_expression: Some(String::from("attribute_not_exists(Code)")),
        ..Default::default()
    };

    let client: DynamoDbClient = DynamoDbClient::new(Region::EuCentral1);
    let result = Runtime::new()
        .expect("Failed to create Tokio runtime")
        .block_on(client.put_item(put_item));

    match result {
        Ok(_) => { Ok(UniqueCode { code: String::from(code) }) }
        Err(_) => { Err(HandlerError::from("Failed to save code")) }
    }
}
