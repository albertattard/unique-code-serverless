use crate::clock::ClockService;
use crate::random_code::RandomCodeService;
use lambda_runtime::{handler_fn, Context, Error};
use rusoto_core::Region;
use rusoto_dynamodb::{AttributeValue, DynamoDb, DynamoDbClient, PutItemInput};
use serde_derive::{Deserialize, Serialize};
use serde_json::Value;
use std::collections::HashMap;
use crate::data_access::{CreateUniqueCode, DataAccessGateway};

#[derive(Deserialize, Debug)]
struct CreateUniqueCodeRequest {
    #[serde(rename = "usedBy")]
    used_by: Option<String>,
    length: Option<usize>,
    reference: Option<String>,
    description: Option<String>,
}

#[derive(Serialize)]
struct UniqueCode {
    code: String,
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    let func = handler_fn(handle_request);
    lambda_runtime::run(func).await?;
    Ok(())
}

async fn handle_request(event: Value, _: Context) -> Result<UniqueCode, Error> {
    let request: CreateUniqueCodeRequest = serde_json::from_value(event["body"].clone()).unwrap();

    let random_code_service = RandomCodeService::new();
    let clock_service = ClockService::new();
    let data_access_gateway = DataAccessGateway::new();
    create_random_code(random_code_service, clock_service, data_access_gateway, request).await
}

async fn create_random_code(
    random_code_service: RandomCodeService,
    clock_service: ClockService,
    data_access_gateway: DataAccessGateway,
    request: CreateUniqueCodeRequest,
) -> Result<UniqueCode, Error> {
    let code = random_code_service.random_string(request.length.unwrap_or(8));
    let created_on = clock_service.created_on();

    let create = CreateUniqueCode::new(code, created_on, request);
    let successful = data_access_gateway.save(create);
    if successful {
        return Ok(UniqueCode {
            code: code.to_owned(),
        });
    }

    Err(Error::from("Failed to create a unique code"))
}

mod data_access {
    use crate::CreateUniqueCodeRequest;
    use rusoto_dynamodb::{AttributeValue, DynamoDbClient, PutItemInput, DynamoDb};
    use std::collections::HashMap;
    use std::error::Error;
    use rusoto_core::Region;

    pub struct DataAccessGateway;

    pub struct CreateUniqueCode {
        code: String,
        created_on: String,
        used_by: Option<String>,
        reference: Option<String>,
        description: Option<String>,
    }

    impl CreateUniqueCode {
        pub fn new(
            code: String,
            created_on: String,
            request: CreateUniqueCodeRequest,
        ) -> CreateUniqueCode {
            CreateUniqueCode {
                code,
                created_on,
                used_by: request.used_by,
                reference: request.reference,
                description: request.description,
            }
        }

        // pub fn with_code(&self, code: String) -> CreateUniqueCode {
        //     CreateUniqueCode {
        //         code,
        //         created_on: &self.created_on,
        //         used_by: &self.used_by,
        //         reference: &self.reference,
        //         description: &self.description,
        //     }
        // }

        fn to_attributes_by_name(&self) -> HashMap<String, AttributeValue> {
            let mut item: HashMap<String, AttributeValue> = HashMap::new();
            item.insert(
                String::from("Code"),
                AttributeValue {
                    s: Some((&self.code).clone()),
                    ..Default::default()
                },
            );
            item.insert(
                String::from("CreatedOn"),
                AttributeValue {
                    s: Some((&self.created_on).clone()),
                    ..Default::default()
                },
            );
            if &self.used_by.is_some() {
                item.insert(
                    String::from("UsedBy"),
                    AttributeValue {
                        s: Some((&self.used_by).unwrap()),
                        ..Default::default()
                    },
                );
            }
            if &self.reference.is_some() {
                item.insert(
                    String::from("Reference"),
                    AttributeValue {
                        s: Some((&self.reference).unwrap()),
                        ..Default::default()
                    },
                );
            }
            if &self.description.is_some() {
                item.insert(
                    String::from("Description"),
                    AttributeValue {
                        s: Some((&self.description).unwrap()),
                        ..Default::default()
                    },
                );
            }

            item
        }
    }

    impl DataAccessGateway {
        pub fn new() -> DataAccessGateway {
            DataAccessGateway {}
        }

        pub async fn save(&self, create: CreateUniqueCode) -> Result<bool, Error> {
            let put_item = PutItemInput {
                table_name: "UniqueCodes".to_string(),
                return_consumed_capacity: None,
                item: create.to_attributes_by_name(),
                condition_expression: Some(String::from("attribute_not_exists(Code)")),
                ..Default::default()
            };

            let client: DynamoDbClient = DynamoDbClient::new(Region::EuCentral1);
            client.put_item(put_item).await?;

            Ok(true)
        }
    }
}

mod clock {
    use chrono::{DateTime, Utc};

    pub struct ClockService;

    impl ClockService {
        pub fn new() -> ClockService {
            ClockService {}
        }

        pub fn created_on(&self) -> String {
            let now: DateTime<Utc> = Utc::now();
            now.to_rfc3339()
        }
    }

    #[cfg(test)]
    mod tests {
        use super::*;

        #[test]
        fn test_created_on() {
            let service = ClockService::new();
            let created_on = service.created_on();
            let parsed = DateTime::parse_from_rfc3339(&created_on);
            assert!(parsed.is_ok());
        }
    }
}

mod random_code {
    use rand::distributions::Distribution;
    use rand::{thread_rng, Rng};
    use std::iter;

    pub struct RandomCodeService;

    impl RandomCodeService {
        pub fn new() -> RandomCodeService {
            RandomCodeService {}
        }

        pub fn random_string(&self, length: usize) -> String {
            let mut rng = thread_rng();
            let code: String = iter::repeat(())
                .map(|()| rng.sample(AsciiUpperCaseAndNumbers))
                .map(char::from)
                .take(length)
                .collect();
            code
        }
    }

    struct AsciiUpperCaseAndNumbers;

    impl Distribution<u8> for AsciiUpperCaseAndNumbers {
        // This function was copied from rand::distributions::Alphanumeric and removed the lower
        // case letters.  I am not sure why the bitshift is required.
        fn sample<R: Rng + ?Sized>(&self, rng: &mut R) -> u8 {
            const RANGE: u32 = 26 + 10;
            const GEN_ASCII_STR_CHARSET: &[u8] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZ\
                0123456789";
            loop {
                let var = rng.next_u32() >> (32 - 6);
                if var < RANGE {
                    return GEN_ASCII_STR_CHARSET[var as usize];
                }
            }
        }
    }

    #[cfg(test)]
    mod tests {
        use super::*;
        use regex::Regex;

        #[test]
        fn test_random_string() {
            let length = 12;
            let service = RandomCodeService::new();
            let code = service.random_string(length);
            assert_eq!(length, code.len());

            let code_pattern = Regex::new(r"^[A-Z0-9]{12}$").unwrap();
            assert!(code_pattern.is_match(&code));
        }
    }
}
