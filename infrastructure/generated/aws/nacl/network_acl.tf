resource "aws_network_acl" "tfer--acl-002D-014f5b90f90e33452" {
  egress {
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = "0"
    icmp_code  = "0"
    icmp_type  = "0"
    protocol   = "-1"
    rule_no    = "100"
    to_port    = "0"
  }

  ingress {
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = "0"
    icmp_code  = "0"
    icmp_type  = "0"
    protocol   = "-1"
    rule_no    = "100"
    to_port    = "0"
  }

  subnet_ids = ["${data.terraform_remote_state.subnet.outputs.aws_subnet_tfer--subnet-002D-044a156c809aefca5_id}", "${data.terraform_remote_state.subnet.outputs.aws_subnet_tfer--subnet-002D-0f1c4ddbf56ecac1b_id}", "${data.terraform_remote_state.subnet.outputs.aws_subnet_tfer--subnet-002D-0a83358f8e3124957_id}"]
  vpc_id     = "${data.terraform_remote_state.vpc.outputs.aws_vpc_tfer--vpc-002D-0d2d203fbb6adec0c_id}"
}
