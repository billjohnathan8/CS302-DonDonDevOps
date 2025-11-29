output "aws_iam_group_policy_attachment_tfer--admin_AdministratorAccess_id" {
  value = "${aws_iam_group_policy_attachment.tfer--admin_AdministratorAccess.id}"
}

output "aws_iam_group_tfer--admin_id" {
  value = "${aws_iam_group.tfer--admin.id}"
}

output "aws_iam_role_policy_attachment_tfer--AWSServiceRoleForOrganizations_AWSOrganizationsServiceTrustPolicy_id" {
  value = "${aws_iam_role_policy_attachment.tfer--AWSServiceRoleForOrganizations_AWSOrganizationsServiceTrustPolicy.id}"
}

output "aws_iam_role_policy_attachment_tfer--AWSServiceRoleForResourceExplorer_AWSResourceExplorerServiceRolePolicy_id" {
  value = "${aws_iam_role_policy_attachment.tfer--AWSServiceRoleForResourceExplorer_AWSResourceExplorerServiceRolePolicy.id}"
}

output "aws_iam_role_policy_attachment_tfer--AWSServiceRoleForSSO_AWSSSOServiceRolePolicy_id" {
  value = "${aws_iam_role_policy_attachment.tfer--AWSServiceRoleForSSO_AWSSSOServiceRolePolicy.id}"
}

output "aws_iam_role_policy_attachment_tfer--AWSServiceRoleForSupport_AWSSupportServiceRolePolicy_id" {
  value = "${aws_iam_role_policy_attachment.tfer--AWSServiceRoleForSupport_AWSSupportServiceRolePolicy.id}"
}

output "aws_iam_role_policy_attachment_tfer--AWSServiceRoleForTrustedAdvisor_AWSTrustedAdvisorServiceRolePolicy_id" {
  value = "${aws_iam_role_policy_attachment.tfer--AWSServiceRoleForTrustedAdvisor_AWSTrustedAdvisorServiceRolePolicy.id}"
}

output "aws_iam_role_tfer--AWSServiceRoleForOrganizations_id" {
  value = "${aws_iam_role.tfer--AWSServiceRoleForOrganizations.id}"
}

output "aws_iam_role_tfer--AWSServiceRoleForResourceExplorer_id" {
  value = "${aws_iam_role.tfer--AWSServiceRoleForResourceExplorer.id}"
}

output "aws_iam_role_tfer--AWSServiceRoleForSSO_id" {
  value = "${aws_iam_role.tfer--AWSServiceRoleForSSO.id}"
}

output "aws_iam_role_tfer--AWSServiceRoleForSupport_id" {
  value = "${aws_iam_role.tfer--AWSServiceRoleForSupport.id}"
}

output "aws_iam_role_tfer--AWSServiceRoleForTrustedAdvisor_id" {
  value = "${aws_iam_role.tfer--AWSServiceRoleForTrustedAdvisor.id}"
}

output "aws_iam_user_group_membership_tfer--amigoscode-002F-admin_id" {
  value = "${aws_iam_user_group_membership.tfer--amigoscode-002F-admin.id}"
}

output "aws_iam_user_tfer--AIDAQ2WS33GIVHAMDJV2Z_id" {
  value = "${aws_iam_user.tfer--AIDAQ2WS33GIVHAMDJV2Z.id}"
}
