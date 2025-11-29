resource "aws_s3_bucket" "tfer--fs-002D-billjohnathan8-002D-customer-002D-test" {
  bucket        = "fs-billjohnathan8-customer-test"
  force_destroy = "false"

  grant {
    id          = "be040dbfc0424184ddf8d69156a9885b1f9ac5be890e37834b71e3fbffc186e7"
    permissions = ["FULL_CONTROL"]
    type        = "CanonicalUser"
  }

  object_lock_enabled = "false"
  request_payer       = "BucketOwner"

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm = "AES256"
      }

      bucket_key_enabled = "true"
    }
  }

  versioning {
    enabled    = "false"
    mfa_delete = "false"
  }
}
