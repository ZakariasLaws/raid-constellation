# Distributed Heterogeneous Inference on Edge Devices
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

## Configuration
For running this application, Constellation requires the following environment variables to be set on *ALL* devices. The port number must be the same on all devices, it is used to connect to the server:

```bash
export CONSTELLATION_PORT=<unique_port_nmr>
export EDGEINFERENCE_DIR=/build/path/edgeinference-constellation
```

The `EDGEINFERENCE_DIR` should correspond to the location of your distribution, which should be 
`build/install/edgeinference-constellation`.

The application use [TensorFlow Serving](https://www.tensorflow.org/tfx/guide/serving) in order to support different TensorFlow ML models. When starting a Predictor with the `run.bash` script, the TensorFlow serving API will start on in the background and run on local host. The configuration file is located at `tensorflow/tensorflow_serving/ModelServerConfig.conf`, it only supports *absolute paths* and **must** therefor be modified with the device system paths.

The config file should look something like this, (see [here](https://www.tensorflow.org/tfx/serving/serving_config) for more options):
```conf
model_config_list {
  config {
    name: 'mnist'
    base_path: '/path/to/model/dir/mnist'
    model_platform: 'tensorflow'
  }
  config {
  ....
}
```

## Running

For running this application, Constellation requires the following environment variables to be set on *ALL* devices. The port number must be the same on all devices, it is used to connect to the server:

```bash
export CONSTELLATION_PORT=<unique_port_nmr>
export EDGEINFERENCE_DIR=/build/path/edgeinference-constellation
```

The `EDGEINFERENCE_DIR` should correspond to the location of your distribution, which should be 
`build/install/edgeinference-constellation`.

## Inference Models

### TensorFlow on Arm-based devices
If running on a system architecture which Tensorflow does not [officially support](https://www.tensorflow.org/install/lang_java) (i.e Arm-based devices such as Raspberry Pi or Odroids), set
the `EDGEINFERENCE_TENSORFLOW_DIR` to point to the parent of the `bazel-bin` directory obtained when 
compiling TF from source. See [Build TensorFlow Java API for Odroid-N2](https://github.com/ZakariasLaws/TensorFlow-Java-Build-Odroid-N2) for instructions on how to do this on Odroid-N2. If using a different aarch64 based device, the procedure for building from source will still be similar.

```bash
EDGEINFERENCE_TENSORFLOW_DIR=/path/to/tensorflow/java/bindings
```

In case the `bin/distributed/run.sh` does not automatically identify your architecture, it will try to build using 
the TensorFlow Java Native Bindings and JAR from Maven. This will result in an error looking something like:

```bash
Exception in thread "main" java.lang.UnsatisfiedLinkError: Cannot find TensorFlow native library for OS: linux, architecture: aarch64
```

To solve this, uncomment the following line in the `scripts/distributed/run.sh` file:

```bash
# command="${command} -Djava.library.path=${EDGEINFERENCE_TENSORFLOW_DIR}/bazel-bin/tensorflow/java"
```
