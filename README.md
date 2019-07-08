# Distributed Heterogeneous Inference on Edge Devices
TODO

### <a name="source"></a> Source
TODO

### <a name="target"></a> Target
TODO

### <a name="predictor"></a> Predictor
TODO

## <a name="requirements"></a> Requirements

* Java JDK >= 8

## <a name="installation"></a> Installation

In order to install everything and compile a distribution run the following in the root directory:

```bash
./gradlew installDist
```

This will create the distribution in `build/install/edgeinference-constellation`.

## <a name="configuration"></a> Configuration 
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

The output of the `tensorflow_model_serving` is stored in `tensorflow_model_serving.log` in the bin directory. If one or more agents in charge of prediction for some reason do not work during run time, view this log to see if the error is related to TensorFlow Serving.

## <a name="running"></a> Running

Navigate to the bin directory and execute the appropriate agent, upon starting up a new execution, always startup the agents in the following order:

1. Constellation Server
2. Target (in order to get Activity ID)
3. Source(s) and Predictor(s)

It is possible to add another _target_ during runtime, but this new target cannot receive classifications from images produced by an already running _source_, a **new** _source_ must be started. _Predictors_ however, can process images from newly added _sources_ and send classifcations to any _target_ specified when starting up the _source_.

```bash
cd $EDGEINFERENCE_DIR
$ ./bin/distributed/constellation-server
Ibis server running on 172.17.0.1/10.72.152.146-4567#8a.a0.ee.40.52.7d.00.00.8f.dd.4e.46.8e.a9.36.23~zaklaw01+22
List of Services:
    Central Registry service on virtual port 302
    Management service on virtual port 304
    Bootstrap service on virtual port 303
Known hubs now: 172.17.0.1/10.72.152.146-4567#8a.a0.ee.40.52.7d.00.00.8f.dd.4e.46.8e.a9.36.23~zaklaw01+22
```

When executing the server, we see the IP and the port number on which it listens, from the example above the `IP=10.72.152.146` and `port=4567`. The port should be saved in an environment variable (see [configuration](#configuration)) and the IP should be provided as the _second_ argument when starting any agent.

When starting one of the agents, the first, second and third argument follows the same pattern for all of them. The first argument (s/t/p) specifies whether it should run the _source_, _target_ or _predictor_ respectively, the second is the IP and the third is the _pool name_. The pool name can be any name, used by the server to dinstinguish each Constellation execution in the case of multiple simultanious ones. 

```bash
./bin/distributed/run.bash <s/t/p> <IP> <Pool Name> [Contexts] [extras]
```

#### Target
When starting the _target_, the ID of the activity collecting the results will be printed to the screen. Use this ID when starting up a _source_ agent.
```bash
./bin/distributed/run.bash t 10.72.152.146 test.pool.name 

...
09:57:35,085 INFO  [CID:0:1] nl.zakarias.constellation.edgeinference.activites.CollectAndProcessEvents - In order to target this activity with classifications add the following as argument (exactly as printed) when initializing the new SOURCE: "0:1:0"
...
```



#### Predictor
Contexts used here are A and B, meaning that this agent will only steal jobs having context A or B.

```bash
./bin/distributed/run.bash p 10.72.152.146 test.pool.name A,B
```

#### Source
The source requires the following extra arguments:
* contexts: All submitted images will have _all_ of these contexts, meaning they can be stolen by predictors with _one or more_ matching contexts.
* target: The target activity identifier to send the result of the predictions to, printed to the screen when starting up a _target_ agent.
* dataDir: The directory of where the data to be transmitted is stored
* modelName: The tpe of model which should be used, see [Inference Models](#models) for availability.

```bash
./bin/distributed/run.bash s 10.72.152.146 test.pool.name A,B -target 0:1:0 -dataDir /home/zaklaw01/Projects/odroid-constellation/MNIST_data/ -modelName mnist
```

## <a name="models"></a> Inference Models

### <a name="arm-devices"></a> TensorFlow on Arm-based devices
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
