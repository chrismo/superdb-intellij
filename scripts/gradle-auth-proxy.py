#!/usr/bin/env python3
"""
HTTP proxy that adds Proxy-Authorization header for Gradle builds.

This script solves an issue where Gradle's HTTP client doesn't send
preemptive proxy authentication for HTTPS CONNECT tunnels. It runs
a local proxy on 127.0.0.1:3128 that forwards requests to the upstream
proxy with proper authentication headers.

Usage:
    python3 scripts/gradle-auth-proxy.py &

Then configure Gradle to use 127.0.0.1:3128 as proxy (see setup-gradle-proxy.sh)
"""
import socket
import threading
import base64
import sys
import os
import signal

UPSTREAM_HOST = "21.0.0.99"
UPSTREAM_PORT = 15004
LOCAL_PORT = 3128


def get_proxy_credentials():
    """Extract proxy credentials from environment variables."""
    proxy_url = os.environ.get('https_proxy', os.environ.get('HTTPS_PROXY', ''))
    if not proxy_url:
        return None, None

    if '@' not in proxy_url:
        return None, None

    # Extract user:pass from URL like http://user:pass@host:port
    auth_part = proxy_url.split('@')[0]
    auth_part = auth_part.replace('http://', '').replace('https://', '')

    if ':' not in auth_part:
        return None, None

    user, password = auth_part.split(':', 1)
    return user, password


def create_auth_header(user, password):
    """Create Basic auth header value."""
    credentials = f"{user}:{password}"
    encoded = base64.b64encode(credentials.encode()).decode()
    return f"Proxy-Authorization: Basic {encoded}\r\n"


def handle_client(client_socket, proxy_auth_header):
    """Handle a single client connection."""
    upstream = None
    try:
        # Read the initial request
        request = b""
        while True:
            chunk = client_socket.recv(4096)
            if not chunk:
                return
            request += chunk
            if b"\r\n\r\n" in request:
                break

        # Parse and modify the request - add auth header after first line
        request_str = request.decode('utf-8', errors='replace')
        lines = request_str.split('\r\n')

        new_lines = [lines[0]]
        new_lines.append(proxy_auth_header.strip())
        new_lines.extend(lines[1:])

        modified_request = '\r\n'.join(new_lines).encode()

        # Connect to upstream proxy
        upstream = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        upstream.settimeout(30)
        upstream.connect((UPSTREAM_HOST, UPSTREAM_PORT))
        upstream.sendall(modified_request)

        # Forward data bidirectionally
        def forward(src, dst, name):
            try:
                while True:
                    data = src.recv(8192)
                    if not data:
                        break
                    dst.sendall(data)
            except Exception:
                pass
            finally:
                try:
                    src.shutdown(socket.SHUT_RD)
                except Exception:
                    pass
                try:
                    dst.shutdown(socket.SHUT_WR)
                except Exception:
                    pass

        t1 = threading.Thread(target=forward, args=(upstream, client_socket, "upstream->client"))
        t2 = threading.Thread(target=forward, args=(client_socket, upstream, "client->upstream"))
        t1.daemon = True
        t2.daemon = True
        t1.start()
        t2.start()
        t1.join(timeout=300)
        t2.join(timeout=300)

    except Exception as e:
        print(f"Error handling client: {e}", file=sys.stderr)
    finally:
        try:
            client_socket.close()
        except Exception:
            pass
        if upstream:
            try:
                upstream.close()
            except Exception:
                pass


def main():
    # Get credentials from environment
    user, password = get_proxy_credentials()
    if not user or not password:
        print("Error: No proxy credentials found in https_proxy environment variable", file=sys.stderr)
        print("Expected format: http://user:password@host:port", file=sys.stderr)
        sys.exit(1)

    proxy_auth_header = create_auth_header(user, password)

    # Set up signal handler for clean shutdown
    def signal_handler(sig, frame):
        print("\nShutting down proxy...")
        sys.exit(0)

    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)

    # Start server
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    try:
        server.bind(('127.0.0.1', LOCAL_PORT))
    except OSError as e:
        if e.errno == 98:  # Address already in use
            print(f"Port {LOCAL_PORT} already in use - proxy may already be running", file=sys.stderr)
            sys.exit(0)
        raise

    server.listen(50)
    print(f"Gradle auth proxy listening on 127.0.0.1:{LOCAL_PORT}")
    print(f"Forwarding to {UPSTREAM_HOST}:{UPSTREAM_PORT} with authentication")

    while True:
        try:
            client, addr = server.accept()
            thread = threading.Thread(
                target=handle_client,
                args=(client, proxy_auth_header),
                daemon=True
            )
            thread.start()
        except Exception as e:
            print(f"Error accepting connection: {e}", file=sys.stderr)


if __name__ == '__main__':
    main()
