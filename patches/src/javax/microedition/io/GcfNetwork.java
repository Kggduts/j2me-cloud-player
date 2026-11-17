/*
 * J2ME Cloud Player GCF transport adapters.
 * The surrounding FreeJ2ME Web project is GPL-3.0-or-later; this adapter is
 * distributed under the same license. Network transports use Java SE APIs
 * supplied by the selected runtime (CheerpJ in the browser build).
 */
package javax.microedition.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/** Package-private factory and Java ME GCF transport implementations. */
final class GcfNetwork {
    private static final int DEFAULT_TIMEOUT_MS = 30000;

    private GcfNetwork() { }

    static Connection open(String name, int mode, boolean timeouts) throws IOException {
        if (name == null) throw new IllegalArgumentException("Connection name is null");
        if (mode != Connector.READ && mode != Connector.WRITE && mode != Connector.READ_WRITE) {
            throw new IllegalArgumentException("Invalid connection mode: " + mode);
        }
        String lower = name.toLowerCase();
        if (lower.startsWith("http://")) return new GcfHttpConnection(name, mode, timeouts);
        if (lower.startsWith("https://")) return new GcfHttpsConnection(name, mode, timeouts);
        if (lower.startsWith("socket://")) return new GcfSocketConnection(name, mode, timeouts);
        if (lower.startsWith("datagram://")) return new GcfDatagramConnection(name);
        throw new ConnectionNotFoundException("Unsupported protocol: " + name);
    }

    private static Endpoint endpoint(String value, String scheme) throws IOException {
        String rest = value.substring(scheme.length() + 3);
        int slash = rest.indexOf('/');
        if (slash >= 0) rest = rest.substring(0, slash);
        int semi = rest.indexOf(';');
        if (semi >= 0) rest = rest.substring(0, semi);

        String host;
        String portText;
        if (rest.startsWith("[")) {
            int end = rest.indexOf(']');
            if (end < 0 || end + 1 >= rest.length() || rest.charAt(end + 1) != ':') {
                throw new IOException("Invalid IPv6 endpoint: " + value);
            }
            host = rest.substring(1, end);
            portText = rest.substring(end + 2);
        } else {
            int colon = rest.lastIndexOf(':');
            if (colon < 0) throw new IOException("Missing port: " + value);
            host = rest.substring(0, colon);
            portText = rest.substring(colon + 1);
        }

        int port;
        try { port = Integer.parseInt(portText); }
        catch (NumberFormatException e) { throw new IOException("Invalid port: " + portText); }
        if (port < 1 || port > 65535) throw new IOException("Port out of range: " + port);
        return new Endpoint(host, port);
    }

    private static final class Endpoint {
        final String host;
        final int port;
        Endpoint(String host, int port) { this.host = host; this.port = port; }
        InetSocketAddress socketAddress() throws IOException {
            if (host.length() == 0) throw new IOException("A destination host is required");
            return new InetSocketAddress(host, port);
        }
        String asDatagramAddress() {
            String h = host;
            if (h.indexOf(':') >= 0 && !h.startsWith("[")) h = "[" + h + "]";
            return "datagram://" + h + ":" + port;
        }
    }

    private static class GcfHttpConnection implements HttpConnection {
        final URL url;
        final HttpURLConnection connection;
        final int mode;
        InputStream input;
        OutputStream output;
        boolean closed;

        GcfHttpConnection(String address, int mode, boolean timeouts) throws IOException {
            this.url = new URL(address);
            URLConnection raw = url.openConnection();
            if (!(raw instanceof HttpURLConnection)) throw new IOException("HTTP transport unavailable");
            this.connection = (HttpURLConnection) raw;
            this.mode = mode;
            int timeout = timeouts ? DEFAULT_TIMEOUT_MS : 0;
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoInput((mode & Connector.READ) != 0);
        }

        private void ensureOpen() throws IOException {
            if (closed) throw new IOException("Connection is closed");
        }

        public synchronized InputStream openInputStream() throws IOException {
            ensureOpen();
            if ((mode & Connector.READ) == 0) throw new IOException("Connection is not readable");
            if (input == null) input = connection.getInputStream();
            return input;
        }

        public synchronized DataInputStream openDataInputStream() throws IOException {
            return new DataInputStream(openInputStream());
        }

        public synchronized OutputStream openOutputStream() throws IOException {
            ensureOpen();
            if ((mode & Connector.WRITE) == 0) throw new IOException("Connection is not writable");
            connection.setDoOutput(true);
            if (output == null) output = connection.getOutputStream();
            return output;
        }

        public synchronized DataOutputStream openDataOutputStream() throws IOException {
            return new DataOutputStream(openOutputStream());
        }

        public synchronized void close() {
            if (closed) return;
            closed = true;
            try { if (input != null) input.close(); } catch (IOException ignored) { }
            try { if (output != null) output.close(); } catch (IOException ignored) { }
            connection.disconnect();
        }

        public String getEncoding() { return connection.getContentEncoding(); }
        public long getLength() { return connection.getContentLengthLong(); }
        public String getType() { return connection.getContentType(); }
        public long getDate() { return connection.getDate(); }
        public long getExpiration() { return connection.getExpiration(); }
        public String getFile() { return url.getFile(); }
        public String getHeaderField(int n) { return connection.getHeaderField(n); }
        public String getHeaderField(String name) { return connection.getHeaderField(name); }
        public long getHeaderFieldDate(String name, long def) { return connection.getHeaderFieldDate(name, def); }
        public int getHeaderFieldInt(String name, int def) { return connection.getHeaderFieldInt(name, def); }
        public String getHeaderFieldKey(int n) { return connection.getHeaderFieldKey(n); }
        public String getHost() { return url.getHost(); }
        public long getLastModified() { return connection.getLastModified(); }
        public int getPort() { return url.getPort() >= 0 ? url.getPort() : 80; }
        public String getProtocol() { return url.getProtocol(); }
        public String getQuery() { return url.getQuery(); }
        public String getRef() { return url.getRef(); }
        public String getRequestMethod() { return connection.getRequestMethod(); }
        public String getRequestProperty(String key) { return connection.getRequestProperty(key); }
        public int getResponseCode() throws IOException { ensureOpen(); return connection.getResponseCode(); }
        public String getResponseMessage() throws IOException { ensureOpen(); return connection.getResponseMessage(); }
        public String getURL() { return url.toExternalForm(); }
        public void setRequestMethod(String method) throws IOException { ensureOpen(); connection.setRequestMethod(method); }
        public void setRequestProperty(String key, String value) { connection.setRequestProperty(key, value); }
    }

    private static final class GcfHttpsConnection extends GcfHttpConnection implements HttpsConnection {
        GcfHttpsConnection(String address, int mode, boolean timeouts) throws IOException {
            super(address, mode, timeouts);
            if (!(connection instanceof javax.net.ssl.HttpsURLConnection)) {
                throw new IOException("HTTPS transport unavailable");
            }
        }
        public SecurityInfo getSecurityInfo() {
            // Browser TLS is authoritative under CheerpJ; Java ME certificate
            // objects are not synthesized from browser-managed certificates.
            return null;
        }
        public int getPort() { return url.getPort() >= 0 ? url.getPort() : 443; }
    }

    private static final class GcfSocketConnection implements SocketConnection {
        private final Socket socket;
        private final int mode;
        private InputStream input;
        private OutputStream output;
        private boolean closed;

        GcfSocketConnection(String address, int mode, boolean timeouts) throws IOException {
            Endpoint endpoint = endpoint(address, "socket");
            if (endpoint.host.length() == 0) throw new IOException("Socket host is required");
            socket = new Socket();
            socket.connect(endpoint.socketAddress(), timeouts ? DEFAULT_TIMEOUT_MS : 0);
            this.mode = mode;
        }

        private void ensureOpen() throws IOException {
            if (closed || socket.isClosed()) throw new IOException("Connection is closed");
        }
        public synchronized InputStream openInputStream() throws IOException {
            ensureOpen();
            if ((mode & Connector.READ) == 0) throw new IOException("Connection is not readable");
            if (input == null) input = socket.getInputStream();
            return input;
        }
        public synchronized DataInputStream openDataInputStream() throws IOException {
            return new DataInputStream(openInputStream());
        }
        public synchronized OutputStream openOutputStream() throws IOException {
            ensureOpen();
            if ((mode & Connector.WRITE) == 0) throw new IOException("Connection is not writable");
            if (output == null) output = socket.getOutputStream();
            return output;
        }
        public synchronized DataOutputStream openDataOutputStream() throws IOException {
            return new DataOutputStream(openOutputStream());
        }
        public synchronized void close() {
            if (closed) return;
            closed = true;
            try { socket.close(); } catch (IOException ignored) { }
        }
        public String getAddress() { return socket.getInetAddress().getHostAddress(); }
        public String getLocalAddress() { return socket.getLocalAddress().getHostAddress(); }
        public int getLocalPort() { return socket.getLocalPort(); }
        public int getPort() { return socket.getPort(); }
        public int getSocketOption(byte option) {
            try {
                switch (option) {
                    case DELAY: return socket.getTcpNoDelay() ? 0 : 1;
                    case LINGER: return socket.getSoLinger();
                    case KEEPALIVE: return socket.getKeepAlive() ? 1 : 0;
                    case RCVBUF: return socket.getReceiveBufferSize();
                    case SNDBUF: return socket.getSendBufferSize();
                    default: throw new IllegalArgumentException("Unknown socket option: " + option);
                }
            } catch (SocketException e) { throw new IllegalArgumentException(e.toString()); }
        }
        public void setSocketOption(byte option, int value) {
            try {
                switch (option) {
                    case DELAY: socket.setTcpNoDelay(value == 0); break;
                    case LINGER: socket.setSoLinger(value >= 0, value < 0 ? 0 : value); break;
                    case KEEPALIVE: socket.setKeepAlive(value != 0); break;
                    case RCVBUF: socket.setReceiveBufferSize(value); break;
                    case SNDBUF: socket.setSendBufferSize(value); break;
                    default: throw new IllegalArgumentException("Unknown socket option: " + option);
                }
            } catch (SocketException e) { throw new IllegalArgumentException(e.toString()); }
        }
    }

    private static final class GcfDatagramConnection implements DatagramConnection {
        private final DatagramSocket socket;
        private final String defaultAddress;
        private boolean closed;

        GcfDatagramConnection(String address) throws IOException {
            Endpoint endpoint = endpoint(address, "datagram");
            defaultAddress = endpoint.host.length() == 0 ? null : endpoint.asDatagramAddress();
            if (endpoint.host.length() == 0) socket = new DatagramSocket(endpoint.port);
            else socket = new DatagramSocket();
        }
        public int getMaximumLength() { return 65507; }
        public int getNominalLength() { return 1472; }
        public Datagram newDatagram(byte[] buffer, int size) {
            if (buffer == null || size < 0 || size > buffer.length) throw new IllegalArgumentException("Invalid datagram buffer");
            return new GcfDatagram(buffer, 0, size, defaultAddress);
        }
        public Datagram newDatagram(byte[] buffer, int size, String address) {
            GcfDatagram datagram = (GcfDatagram) newDatagram(buffer, size);
            datagram.setAddress(address);
            return datagram;
        }
        public Datagram newDatagram(int size) {
            if (size < 0 || size > getMaximumLength()) throw new IllegalArgumentException("Invalid datagram size");
            return new GcfDatagram(new byte[size], 0, size, defaultAddress);
        }
        public Datagram newDatagram(int size, String address) {
            GcfDatagram datagram = (GcfDatagram) newDatagram(size);
            datagram.setAddress(address);
            return datagram;
        }
        public synchronized void send(Datagram datagram) {
            try {
                ensureOpen();
                if (!(datagram instanceof GcfDatagram)) throw new IOException("Datagram was not created by this implementation");
                GcfDatagram data = (GcfDatagram) datagram;
                String address = data.getAddress();
                if (address == null) address = defaultAddress;
                if (address == null) throw new IOException("Datagram destination is not set");
                Endpoint endpoint = endpoint(address, "datagram");
                InetAddress host = InetAddress.getByName(endpoint.host);
                byte[] bytes = data.getData();
                DatagramPacket packet = new DatagramPacket(bytes, data.getOffset(), data.getLength(), host, endpoint.port);
                socket.send(packet);
            } catch (IOException e) { throw new IllegalStateException(e.toString()); }
        }
        public synchronized void receive(Datagram datagram) {
            try {
                ensureOpen();
                if (!(datagram instanceof GcfDatagram)) throw new IOException("Datagram was not created by this implementation");
                GcfDatagram data = (GcfDatagram) datagram;
                byte[] bytes = data.getData();
                int offset = data.getOffset();
                int capacity = data.getLength();
                if (capacity <= 0) throw new IOException("Receive datagram must have a positive buffer length");
                DatagramPacket packet = new DatagramPacket(bytes, offset, capacity);
                socket.receive(packet);
                data.setData(packet.getData(), packet.getOffset(), packet.getLength());
                String host = packet.getAddress().getHostAddress();
                if (host.indexOf(':') >= 0) host = "[" + host + "]";
                data.setAddress("datagram://" + host + ":" + packet.getPort());
            } catch (IOException e) { throw new IllegalStateException(e.toString()); }
        }
        public synchronized void close() {
            if (closed) return;
            closed = true;
            socket.close();
        }
        private void ensureOpen() throws IOException {
            if (closed || socket.isClosed()) throw new IOException("Connection is closed");
        }
    }

    private static final class GcfDatagram implements Datagram {
        private byte[] buffer;
        private int offset;
        private int length;
        private String address;
        private ByteArrayInputStream inputBytes;
        private DataInputStream input;
        private ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        private DataOutputStream output = new DataOutputStream(outputBytes);

        GcfDatagram(byte[] buffer, int offset, int length, String address) {
            setData(buffer, offset, length);
            this.address = address;
        }
        private void resetInput() {
            inputBytes = new ByteArrayInputStream(buffer, offset, length);
            input = new DataInputStream(inputBytes);
        }
        private void syncOutput() {
            if (outputBytes.size() > 0) {
                buffer = outputBytes.toByteArray();
                offset = 0;
                length = buffer.length;
            }
        }
        public String getAddress() { return address; }
        public byte[] getData() { syncOutput(); return buffer; }
        public int getLength() { return outputBytes.size() > 0 ? outputBytes.size() : length; }
        public int getOffset() { return outputBytes.size() > 0 ? 0 : offset; }
        public void reset() {
            buffer = new byte[0];
            offset = 0;
            length = 0;
            outputBytes = new ByteArrayOutputStream();
            output = new DataOutputStream(outputBytes);
            resetInput();
        }
        public void setAddress(Datagram reference) { setAddress(reference == null ? null : reference.getAddress()); }
        public void setAddress(String address) { this.address = address; }
        public void setData(byte[] buffer, int offset, int len) {
            if (buffer == null || offset < 0 || len < 0 || offset + len > buffer.length) {
                throw new IllegalArgumentException("Invalid datagram data range");
            }
            this.buffer = buffer;
            this.offset = offset;
            this.length = len;
            outputBytes = new ByteArrayOutputStream();
            output = new DataOutputStream(outputBytes);
            resetInput();
        }
        public void setLength(int len) {
            if (len < 0 || offset + len > buffer.length) throw new IllegalArgumentException("Invalid datagram length");
            length = len;
            outputBytes = new ByteArrayOutputStream();
            output = new DataOutputStream(outputBytes);
            resetInput();
        }
        public boolean readBoolean() throws IOException { return input.readBoolean(); }
        public byte readByte() throws IOException { return input.readByte(); }
        public char readChar() throws IOException { return input.readChar(); }
        public double readDouble() throws IOException { return input.readDouble(); }
        public float readFloat() throws IOException { return input.readFloat(); }
        public void readFully(byte[] b) throws IOException { input.readFully(b); }
        public void readFully(byte[] b, int off, int len) throws IOException { input.readFully(b, off, len); }
        public int readInt() throws IOException { return input.readInt(); }
        public String readLine() throws IOException { return input.readLine(); }
        public long readLong() throws IOException { return input.readLong(); }
        public short readShort() throws IOException { return input.readShort(); }
        public int readUnsignedByte() throws IOException { return input.readUnsignedByte(); }
        public int readUnsignedShort() throws IOException { return input.readUnsignedShort(); }
        public String readUTF() throws IOException { return input.readUTF(); }
        public int skipBytes(int n) throws IOException { return input.skipBytes(n); }
        public void write(int b) throws IOException { output.write(b); }
        public void write(byte[] b) throws IOException { output.write(b); }
        public void write(byte[] b, int off, int len) throws IOException { output.write(b, off, len); }
        public void writeBoolean(boolean v) throws IOException { output.writeBoolean(v); }
        public void writeByte(int v) throws IOException { output.writeByte(v); }
        public void writeBytes(String s) throws IOException { output.writeBytes(s); }
        public void writeChar(int v) throws IOException { output.writeChar(v); }
        public void writeChars(String s) throws IOException { output.writeChars(s); }
        public void writeDouble(double v) throws IOException { output.writeDouble(v); }
        public void writeFloat(float v) throws IOException { output.writeFloat(v); }
        public void writeInt(int v) throws IOException { output.writeInt(v); }
        public void writeLong(long v) throws IOException { output.writeLong(v); }
        public void writeShort(int v) throws IOException { output.writeShort(v); }
        public void writeUTF(String s) throws IOException { output.writeUTF(s); }
    }
}
