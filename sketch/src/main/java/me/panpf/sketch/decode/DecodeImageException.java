package me.panpf.sketch.decode;

import androidx.annotation.NonNull;

import me.panpf.sketch.SketchException;
import me.panpf.sketch.request.LoadRequest;

public class DecodeImageException extends SketchException {
    @NonNull
    private LoadRequest request;
    private int outWidth;
    private int outHeight;
    @NonNull
    private String outMimeType;

    public DecodeImageException(@NonNull Throwable cause, @NonNull LoadRequest request, int outWidth, int outHeight, @NonNull String outMimeType) {
        super(cause);
        this.request = request;
        this.outWidth = outWidth;
        this.outHeight = outHeight;
        this.outMimeType = outMimeType;
    }

    @NonNull
    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }

    @NonNull
    public LoadRequest getRequest() {
        return request;
    }

    public int getOutWidth() {
        return outWidth;
    }

    public int getOutHeight() {
        return outHeight;
    }

    @NonNull
    public String getOutMimeType() {
        return outMimeType;
    }
}