package com.ecommerce.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Compression Configuration for API Response Optimization
 * 
 * This configuration enables GZIP compression for API responses to reduce
 * bandwidth usage and improve performance.
 */
@Configuration
public class CompressionConfig {

    /**
     * GZIP Compression Filter
     */
    @Bean
    public FilterRegistrationBean<GzipCompressionFilter> gzipCompressionFilter() {
        FilterRegistrationBean<GzipCompressionFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new GzipCompressionFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    /**
     * Custom GZIP Compression Filter
     */
    public static class GzipCompressionFilter extends OncePerRequestFilter {

        private static final List<String> COMPRESSIBLE_CONTENT_TYPES = Arrays.asList(
            "application/json",
            "application/xml",
            "text/html",
            "text/plain",
            "text/css",
            "text/javascript",
            "application/javascript",
            "application/x-javascript",
            "text/xml",
            "application/xhtml+xml",
            "image/svg+xml"
        );

        private static final int MIN_COMPRESSION_SIZE = 1024; // 1KB
        private static final int MAX_COMPRESSION_SIZE = 10 * 1024 * 1024; // 10MB

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            // Check if client supports GZIP
            String acceptEncoding = request.getHeader("Accept-Encoding");
            boolean supportsGzip = acceptEncoding != null && acceptEncoding.contains("gzip");

            if (!supportsGzip) {
                filterChain.doFilter(request, response);
                return;
            }

            // Check if response should be compressed
            if (!shouldCompress(request, response)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Wrap response with compression
            GzipResponseWrapper wrappedResponse = new GzipResponseWrapper(response);
            
            try {
                filterChain.doFilter(request, wrappedResponse);
            } finally {
                wrappedResponse.finish();
            }
        }

        private boolean shouldCompress(HttpServletRequest request, HttpServletResponse response) {
            // Don't compress if already compressed
            if (response.getHeader("Content-Encoding") != null) {
                return false;
            }

            // Don't compress if content type is not compressible
            String contentType = response.getContentType();
            if (contentType == null || !isCompressibleContentType(contentType)) {
                return false;
            }

            // Don't compress small responses
            String contentLength = response.getHeader("Content-Length");
            if (contentLength != null) {
                try {
                    int length = Integer.parseInt(contentLength);
                    if (length < MIN_COMPRESSION_SIZE) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // Continue with compression
                }
            }

            return true;
        }

        private boolean isCompressibleContentType(String contentType) {
            return COMPRESSIBLE_CONTENT_TYPES.stream()
                .anyMatch(type -> contentType.toLowerCase().contains(type.toLowerCase()));
        }
    }

    /**
     * GZIP Response Wrapper
     */
    public static class GzipResponseWrapper extends HttpServletResponseWrapper {
        private final HttpServletResponse response;
        private GzipOutputStream gzipOutputStream;
        private ByteArrayOutputStream byteArrayOutputStream;

        public GzipResponseWrapper(HttpServletResponse response) {
            super(response);
            this.response = response;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (gzipOutputStream == null) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                gzipOutputStream = new GzipOutputStream(byteArrayOutputStream);
            }
            return gzipOutputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharacterEncoding()));
        }

        @Override
        public void setContentLength(int len) {
            // Don't set content length for compressed content
        }

        @Override
        public void setContentLengthLong(long len) {
            // Don't set content length for compressed content
        }

        public void finish() throws IOException {
            if (gzipOutputStream != null) {
                gzipOutputStream.close();
                
                byte[] compressedData = byteArrayOutputStream.toByteArray();
                
                // Only send compressed data if it's actually smaller
                if (compressedData.length < byteArrayOutputStream.size()) {
                    response.setHeader("Content-Encoding", "gzip");
                    response.setHeader("Content-Length", String.valueOf(compressedData.length));
                    response.setHeader("Vary", "Accept-Encoding");
                    
                    response.getOutputStream().write(compressedData);
                } else {
                    // Send original data if compression didn't help
                    response.getOutputStream().write(byteArrayOutputStream.toByteArray());
                }
            }
        }
    }

    /**
     * GZIP Output Stream
     */
    public static class GzipOutputStream extends ServletOutputStream {
        private final GZIPOutputStream gzipOutputStream;

        public GzipOutputStream(OutputStream outputStream) throws IOException {
            this.gzipOutputStream = new GZIPOutputStream(outputStream);
        }

        @Override
        public void write(int b) throws IOException {
            gzipOutputStream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            gzipOutputStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            gzipOutputStream.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            gzipOutputStream.flush();
        }

        @Override
        public void close() throws IOException {
            gzipOutputStream.close();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // Not implemented
        }
    }
}
