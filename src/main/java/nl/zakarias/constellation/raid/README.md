## Adding New Models

Copy one of the existing model directories in `models` and rename it to match the model you are adding. 

1. Change the class names of the newly created files to match your model.
2. Add your model to the ModelName enum in `configuration/Configuration.java`
3. In the same file, make the `getModel` method also return an instance of your newly added model.
4. Modify the modelName variable in the main model file (modelName.java).
5. Further modify the file to log your new model name instead of the previouse one.
6. Make the same modifications to the other two files, to make sure it is logging corectly.
7. Make sure the input and output dimensions match the TensorFlow Serving model. 
    
    \- Use something like [postman](https://www.getpostman.com/) to query the metadata in 
  [TensorFlow Serving](https://www.tensorflow.org/tfx/serving/api_rest), this will show you the required dimensions.
  
    \- Input dimensions are in the main model file (modelName.java) and output dimensions are in the classifier file 
  (modelNameClassifier.java)

8. Finally, modify the `tensorflow/tensorflow_serving/ModelServerConfig.conf` in the root directory to supply your added model.
The model name **MUST** match the name added in step 2, except in lowercase.
