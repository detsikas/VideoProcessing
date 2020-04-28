package net.peeknpoke.apps.videoprocessing;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

class FrameProcessor {
    private static final String TAG = FrameProcessor.class.getSimpleName();
    private CustomContext mRenderingContext;
    private Context mContext;
    private MediaCodec mMediaCodec;
    private MediaExtractor mMediaExtractor;
    private int mVideoTrackIndex;
    private int mOutputFrameIndex = 1; // Yes, 1!

    FrameProcessor(Context context, Uri uri) throws IOException {
        mContext = context;
        mMediaExtractor = new MediaExtractor();
        mMediaExtractor.setDataSource(context, uri, null);
        mVideoTrackIndex = getVideoTrackIndex(mMediaExtractor);
        if (mVideoTrackIndex<0)
        {
            Log.e(TAG, "No video track");
        }
        else
        {
            mMediaExtractor.selectTrack(mVideoTrackIndex);
            MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(mVideoTrackIndex);
            int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);

            mRenderingContext = new CustomContext(context, width, height);
            setupMediaCodec(mediaFormat);
        }
    }

    void start()
    {
        mMediaCodec.start();
    }

    private void setupMediaCodec(MediaFormat mediaFormat) throws IOException
    {
        String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
        if (mimeType==null)
        {
            Log.e(TAG, "Could not read mime type");
            return;
        }
        mMediaCodec = MediaCodec.createDecoderByType(mimeType);
        mMediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                ByteBuffer inputBuffer = codec.getInputBuffer(index);
                Log.d(TAG, "Filling buffer: "+index);
                fillInputBuffer(inputBuffer, index);
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                MediaFormat bufferFormat = codec.getOutputFormat(index);
                Log.d(TAG, "Processing output buffer "+index+" size: "+info.size);
                processOutputBuffer(info, index);
                mRenderingContext.setOutputFrameIndex(mOutputFrameIndex);
                if (mOutputFrameIndex==mContext.getResources().getInteger(R.integer.MAX_FRAMES))
                {
                    codec.stop();
                    codec.release();
                }
                mOutputFrameIndex++;
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                Log.e(TAG, "Media codec error - "+e.getMessage()+" - "+e.getErrorCode());
            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

            }
        });
        mMediaCodec.configure(mediaFormat, mRenderingContext.getSurface(), null, 0);
    }

    private void fillInputBuffer(ByteBuffer inputBuffer, int index)
    {
        int sampleSize = mMediaExtractor.readSampleData(inputBuffer, 0);
        Log.d(TAG, "sample size: "+sampleSize);
        if (sampleSize < 0)
        {
            // End of input data reached
            mMediaCodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            Log.d(TAG, "input EOS");
        }
        else
        {
            if (mMediaExtractor.getSampleTrackIndex() != mVideoTrackIndex) {
                Log.w(TAG, "WEIRD: got sample from track " +
                        mMediaExtractor.getSampleTrackIndex() + ", expected " + mVideoTrackIndex);
            }

            mMediaCodec.queueInputBuffer(index, 0, sampleSize,
                    mMediaExtractor.getSampleTime(), 0);

            mMediaExtractor.advance();
        }
    }

    private void processOutputBuffer(MediaCodec.BufferInfo info, int index)
    {
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG, "output EOS");
        }

        mMediaCodec.releaseOutputBuffer(index, info.size != 0);
    }

    private int getVideoTrackIndex(MediaExtractor extractor)
    {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime!=null && mime.startsWith("video/"))
                return i;
        }

        return -1;
    }

    void release()
    {
        mRenderingContext.release();
    }
}
