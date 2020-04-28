# VideoProcessing
Frame by frame processing of video on Android using OpenGL
# Frameworks used
MediaCodec, MediaExtractor
# Description
The application extracts the first 10 frames of the first video track of a video file, applies some custom processing on each frame and saves it in DCIM/VideoProcessing.

Frame extraction, decoding and encoding are done asynchronously.

The frame processing is done with OpenGL.

The number of frames extracted is a integer resource and can easily be changed.

The OpenGL shaders are in the assets folder. Currently, the use of the negative fragment shader is hardcoded. You can easily add your own shaders.

