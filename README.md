# VideoProcessing
Frame by frame processing of video on Android using OpenGL
# Frameworks used
MediaCodec, MediaExtractor
# Description
The application extracts the first 10 frames of the first video track of a video file, applies some custom processing on each frame and saves it in DCIM/VideoProcessing.

Frame extraction, decoding and editing are done asynchronously.

The frame processing is done with OpenGL.

The number of frames extracted is a integer resource and can easily be changed.

The OpenGL shaders are in the assets folder. Currently, the use of the negative fragment shader is hardcoded. You can easily add your own shaders.

In addition to that, the project is quite modular so that each functionality is as separated as possible from the rest, making it easier to read and extend.
## Asynchronous decoding
MediaCodec works in asynchronous mode, which is the suggested mode of operation. Still, synchronization with the OpenGL rendering thread is needed. The reason for this is that, the decoder will produce frames faster than OpenGL processing (including reading the pixels and saving to a file) can handle.

Without synchronization, frames will be dropped and the application will not decode the first specified number of frames. It will decode the specified number of frames but it is unpredictable which ones.

A significant speed up can come from saving the output bitmaps to a file offline, but it would need more memory for storing the bitmaps.

