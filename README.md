# CAMP_block
This is the source code of CAMP and CAMP_block.

In the source folder:
The main folder contains the codes of index building, bitwise operations and IO operations with CAMP, CAMP_block and other competitors

The test folder contains the codes of testing code which is used to verify the correctness of the codes in the main folder

The performance_comparison folder contains some experiments which compares the performance between CAMP and other algorithms.

The Net_flow folder contains the bitmap indexes built on CAIDA 2013.


In the lib folder:
All the external libraries that are needed for the experiments are provided. It is recommended that all the experiments be done in Eclipse.

In the threshold.nb:
This files contain the detailed calculation process of the theoretical model proposed in the paper. In order to run it, you should download it into your computer and use Mathematica.

We consider the case when w equals 10000, which has been introduced in the paper.
When p = 0.045, Integer_list < Bit_sequence
when p = 0.046, Integer_list > Bit_sequence
So it can be expected that there exists a value between 0.045 and 0.046, which can make Integer_list equal Bit_sequence. In experiments, we set the value as 0.046.
