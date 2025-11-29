resource "aws_iam_group_policy_attachment" "tfer--admin_AdministratorAccess" {
  group      = "admin"
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}
