# Bootstrapping experiment

Here, we describe the steps needed to reproduce this experiment. This experiment is coded in BootstrappingExp1.java file. This experiment can be executed using the jar and the data sources provided.

## Jar

The jar for all experiments is the same. In case you have not download it. You can find it [here]().

## Parameters

The class BootstrappingExp1 uses the following parameters:

| Parameter | Required | Description                                            |
|-----------|----------|--------------------------------------------------------|
| -d        | true     | Datasources directory.                                 |
| -o        | true     | Output directory.                                      |
| -f        | false    | Base bootstrap file name. Default is bootstrap<X>.ttl  |
| -n        | false    | Number of datasets to bootstrap. Default is 100.       |
| -r        | false    | Number of repetitions. Default is 1.                   |

We present an example of how to run it with java jar command in the following:

```
java -jar
```