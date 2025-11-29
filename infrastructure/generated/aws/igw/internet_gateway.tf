resource "aws_internet_gateway" "tfer--igw-002D-0f307b1f59331e0bc" {
  vpc_id = "${data.terraform_remote_state.vpc.outputs.aws_vpc_tfer--vpc-002D-0d2d203fbb6adec0c_id}"
}
