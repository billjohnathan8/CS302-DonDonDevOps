resource "aws_iam_user_group_membership" "tfer--amigoscode-002F-admin" {
  groups = ["admin"]
  user   = "amigoscode"
}
