resource "aws_organizations_organization" "tfer--billjohnathan8" {
  aws_service_access_principals = ["sso.amazonaws.com"]
  feature_set                   = "ALL"
}
