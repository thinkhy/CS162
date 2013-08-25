#!/usr/bin/perl 
#===============================================================================
#
#         FILE:  test.pl
#
#        USAGE:  ./test.pl  
#
#  DESCRIPTION:  Test for Proj1-Task4 in online course UCB CS162
#
#      OPTIONS:  ---
# REQUIREMENTS:  ---
#         BUGS:  ---
#        NOTES:  ---
#       AUTHOR:  thinkhy
#      COMPANY:  
#      VERSION:  1.0
#      CREATED:  8/25/2013 12:44:03 PM
#     REVISION:  ---
#===============================================================================

use strict;
use warnings;

while(1) {
    my $cmd = q(run.bat 2>&1);

    open my $pipe, '-|', $cmd;

    while(<$pipe>) {
        die "Test case failed\n" if (/(failure)|(error)/i);
    }
}

