# Schema integration experiment 3

Here, we describe the steps needed to reproduce this experiment. This experiment is coded in IntegrationExp3.java file. This experiment can be executed using the jar and the data sources provided.

## Jar

The jar for all experiments is the same. In case you have not download it. You can find it [here](https://mydisk.cs.upc.edu/s/dfKQ35yafqWo577).

## Parameters

The class IntegrationExp3 uses the following parameters:

| Parameter | Required | Description                                                                    |
|-----------|----------|--------------------------------------------------------------------------------|
| -d        | true     | Data sources directory.                                                        |
| -o        | true     | Output directory.                                                              |
| -t        | false    | Dataset used. Default is anatomy. Possible options are "anatomy" or "largeBio" |
| -r        | false    | Number of repetitions. Default is 1.                                           |

We present an example of how to run it with java jar command in the following:

```
java -cp NextiaDI_experiments.jar integration3.IntegrationExp3 -o "/Users/jflores/output/" -d "/Users/jflores/input/" 
```
Note that for largeBio will be required to increase entity expansion limit parameter in java due to the large amount of entities contain in dataset largeBio. To this end, add the following VM argument:

```
-DentityExpansionLimit=2500000
```
