cos424project
=============

cos424 final project

###'g-force' computation

When a key is pressed, the accelerometer reads the x-, y- and z- direction
components of the acceleration of the vibration received. To get the
acceleration relative to the gravity, one needs to compute the following
**g-force** = ![equation](http://bit.ly/1nySUx2)

Here *g* = 9.81 m/s<sup>2</sup> is the acceleration due to gravity.

## Steps

1. Clean signal files, removing head and tail. 

2. Collect remaining features: ffts, mfccs

3. Breaking down of a recorded word signal into characters

4. L/R predictor (i.e. neural network)

5. Clustering on training set (distance, normalization?)

6. Clustering on test set

7. Dictionary, most likely word (Clustering returns top 5 characters)

