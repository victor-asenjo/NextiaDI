# Reproducibility of experiments

Here, you can find the code and instructions to reproduce the experiments to evaluate NextiaDI. We evaluate the main NextiaDI's features: bootstrapping and schema integration to assess their complexity and runtime performance. All experiments code are provided in this [JAR](https://mydisk.cs.upc.edu/s/dfKQ35yafqWo577/download) for easy of use.


## Bootstrapping experiment
The goal of this experiment is to evaluate the bootstrapping of JSON and CSV data sources by measuring the impact of the schema size. The code and instructions for this experiment can be found [here](https://github.com/dtim-upc/NextiaDI/tree/main/experiments/src/main/java/bootstrapping#bootstrapping-experiment).

## Schema integration experiments
We evaluate the schema integration in the following three experiments.

### Experiment 1 — Increased alignments
The goal of this experiment consist in measure the performance of NextiaDI when the number of alignments increases in a continuous integration. The code and instructions for this experiment can be found [here](https://github.com/dtim-upc/NextiaDI/tree/main/experiments/src/main/java/integration1#schema-integration-experiment-1).


### Experiment 2 — Increased schema elements
This experiement measures the performance when the number of alignments remains the same but the schema size of new data sources increases. The code and instructions for this experiment can be found [here](https://github.com/dtim-upc/NextiaDI/tree/main/experiments/src/main/java/integration2#schema-integration-experiment-2).

### Experiment 3 — Real data

This experiment evaluates NextiaDI using real data obtained from the Ontology Alignment Evaluation Initiative ([OAEI](http://oaei.ontologymatching.org/)). The code and instructions for this experiment can be found [here](https://github.com/dtim-upc/NextiaDI/tree/main/experiments/src/main/java/integration3).


