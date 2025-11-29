resource "aws_main_route_table_association" "tfer--vpc-002D-0d2d203fbb6adec0c" {
  route_table_id = "${data.terraform_remote_state.route_table.outputs.aws_route_table_tfer--rtb-002D-0386fe60136475ad9_id}"
  vpc_id         = "${data.terraform_remote_state.vpc.outputs.aws_vpc_tfer--vpc-002D-0d2d203fbb6adec0c_id}"
}
