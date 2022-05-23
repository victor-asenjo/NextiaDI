# Schema integration experiment 2

Here, we describe the steps needed to reproduce this experiment. This experiment is coded in IntegrationExp2.java file. This experiment can be executed using the jar and the data sources provided.

## Jar

The jar for all experiments is the same. In case you have not download it. You can find it [here](https://mydisk.cs.upc.edu/s/dfKQ35yafqWo577).

## Parameters

The class IntegrationExp2 uses the following parameters:

| Parameter | Required | Description                                      |
|-----------|----------|--------------------------------------------------|
| -d        | true     | Data sources directory.                          |
| -o        | true     | Output directory.                                |
| -n        | false    | Number of datasets to bootstrap. Default is 100. |
| -r        | false    | Number of repetitions. Default is 1.             |

We present an example of how to run it with java cp command in the following:

```
java -cp NextiaDI_experiments.jar integration2.IntegrationExp2 -o "/Users/jflores/output/" -d "/Users/jflores/input/"
```
