package io.github.richardstartin.messagepack;

import java.nio.charset.Charset;

public final class StringBackedCharSequence implements CharSequence {

    private final String delegate;

    public StringBackedCharSequence(String delegate) {
        this.delegate = delegate;
    }

    @Override
    public int length() {
        return delegate.length();
    }

    @Override
    public char charAt(int i) {
        return delegate.charAt(i);
    }

    @Override
    public CharSequence subSequence(int i, int i1) {
        return null;
    }

    public byte[] getBytes(Charset charset) {
        return delegate.getBytes(charset);
    }
}
