# Distributed Heterogeneous Inferencing on Edge Devices
TODO

### Source
TODO

### Target
TODO

### Predictor
TODO

## Requirements

* Java JDK >= 8

## Installation

In order to install everything and compile a distribution run the following in the root directory:

```bash
./gradlew installDist
```

This will create the distribution in `build/install/edgeinference-constellation`.

## Running

For running the application, Constellation requires the following environment variables to be set on *ALL* devices:

```bash
export CONSTELLATION_PORT=<unique_port_nmr>
export EDGEINFERENCE_DIR=/build/path/edgeinference-constellation
```

The `EDGEINFERENCE_DIR` should correspond to the location of your distribution, which should be 
`build/install/edgeinference-constellation`.

## Inference Models

### Tensorflow on Arm-based devices
If running on a system architecture which Tensorflow on Jcenter or Maven does not support, set
the `EDGEINFERENCE_TENSORFLOW_DIR` to point to the directory containing the bazel-bin dir obtained when 
compiling TF from source. 

```bash
EDGEINFERENCE_TENSORFLOW_DIR=/path/to/tensorflow/java/bindings
```

In case the `bin/distributed/run.sh` does not automatically identify you're architecture, it will try to build using 
the TF Java bindings from Maven. This will result in an error looking something like:

```bash
Exception in thread "main" java.lang.UnsatisfiedLinkError: Cannot find TensorFlow native library for OS: linux, architecture: aarch64
```

To solve this, uncomment the following line in the `scripts/distributed/run.sh` file:

```bash
# command="${command} -Djava.library.path=${EDGEINFERENCE_TENSORFLOW_DIR}/bazel-bin/tensorflow/java"
```

### TODO

* Build a later version of Tensorflow, currently build from branch r1.11