resource "aws_route_table" "tfer--rtb-002D-0386fe60136475ad9" {
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "igw-0f307b1f59331e0bc"
  }

  vpc_id = "${data.terraform_remote_state.vpc.outputs.aws_vpc_tfer--vpc-002D-0d2d203fbb6adec0c_id}"
}
